/*
 *******************************************************************************
 *
 * Copyright (C) 2020 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.diasemi.codelesslib;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.diasemi.codelesslib.CodelessProfile.LineType;
import com.diasemi.codelesslib.CodelessProfile.Uuid;
import com.diasemi.codelesslib.command.BinExitAckCommand;
import com.diasemi.codelesslib.command.BinExitCommand;
import com.diasemi.codelesslib.command.BinRequestAckCommand;
import com.diasemi.codelesslib.command.BinRequestCommand;
import com.diasemi.codelesslib.command.CodelessCommand;
import com.diasemi.codelesslib.command.CustomCommand;
import com.diasemi.codelesslib.dsps.DspsFileReceive;
import com.diasemi.codelesslib.dsps.DspsFileSend;
import com.diasemi.codelesslib.dsps.DspsPeriodicSend;
import com.diasemi.codelesslib.log.CodelessLogFile;
import com.diasemi.codelesslib.log.DspsRxLogFile;
import com.diasemi.codelesslib.misc.RuntimePermissionChecker;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static com.diasemi.codelesslib.CodelessEvent.ERROR_GATT_OPERATION;
import static com.diasemi.codelesslib.CodelessEvent.ERROR_INIT_SERVICES;
import static com.diasemi.codelesslib.CodelessEvent.ERROR_INVALID_COMMAND;
import static com.diasemi.codelesslib.CodelessEvent.ERROR_INVALID_PREFIX;
import static com.diasemi.codelesslib.CodelessEvent.ERROR_NOT_READY;

public class CodelessManager {
    private final static String TAG = "CodelessManager";

    // State
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int SERVICE_DISCOVERY = 3;
    public static final int READY = 4;

    public static final int SPEED_INVALID = -1;

    private Context context;
    private BluetoothDevice device;
    private BluetoothGatt gatt;
    private int state = DISCONNECTED;
    private int mtu = CodelessProfile.MTU_DEFAULT;
    private Handler handler;
    private LinkedList<GattOperation> gattQueue = new LinkedList<>();
    private GattOperation gattOperationPending;
    private boolean commandMode;
    private boolean binaryRequestPending;
    private boolean binaryExitRequestPending;
    // Codeless
    private CodelessCommands commandFactory;
    private ArrayDeque<CodelessCommand> commandQueue = new ArrayDeque<>();
    private CodelessCommand commandPending;
    private CodelessCommand commandInbound;
    private int inboundPending;
    private int outboundResponseLines;
    private ArrayList<String> parsePending = new ArrayList<>();
    private CodelessLogFile codelessLogFile;
    // DSPS
    private int dspsChunkSize = CodelessLibConfig.DEFAULT_DSPS_CHUNK_SIZE;
    private boolean dspsRxFlowOn = CodelessLibConfig.DEFAULT_DSPS_RX_FLOW_CONTROL;
    private boolean dspsTxFlowOn = CodelessLibConfig.DEFAULT_DSPS_TX_FLOW_CONTROL;
    private boolean dspsEcho;
    private ArrayList<GattOperation> dspsPending = new ArrayList<>();
    private ArrayList<DspsPeriodicSend> dspsPeriodic = new ArrayList<>();
    private ArrayList<DspsFileSend> dspsFiles = new ArrayList<>();
    private DspsFileReceive dspsFileReceive;
    private DspsRxLogFile dspsRxLogFile;
    private Handler dspsStatsHandler;
    private long dspsLastInterval;
    private int dspsRxBytesInterval;
    private int dspsRxSpeed = SPEED_INVALID;
    // Service database
    private boolean servicesDiscovered;
    private boolean codelessSupport;
    private boolean dspsSupport;
    private BluetoothGattService codelessService;
    private BluetoothGattCharacteristic codelessInbound;
    private BluetoothGattCharacteristic codelessOutbound;
    private BluetoothGattCharacteristic codelessFlowControl;
    private BluetoothGattService dspsService;
    private BluetoothGattCharacteristic dspsServerTx;
    private BluetoothGattCharacteristic dspsServerRx;
    private BluetoothGattCharacteristic dspsFlowControl;
    private BluetoothGattService deviceInfoService;
    private ArrayList<BluetoothGattCharacteristic> pendingEnableNotifications;
    private String logPrefix;

    public CodelessManager(Context context, BluetoothDevice device) {
        this.context = context.getApplicationContext();
        this.device = device;
        commandFactory = new CodelessCommands(this);
        logPrefix = "[" + device.getAddress() + "] ";
    }

    public Context getContext() {
        return context;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getState() {
        return state;
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    public void requestMtu(int mtu) {
        if (Build.VERSION.SDK_INT < 21)
            return;
        enqueueGattOperation(new GattOperation(mtu));
    }

    public Handler getHandler() {
        return handler;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public static boolean checkPermissions(RuntimePermissionChecker permissionChecker, RuntimePermissionChecker.PermissionRequestCallback callback) {
        if (Build.VERSION.SDK_INT < 23)
            return true;
        String[] permissions;
        int rationale;
        if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.DSPS_RX_LOG) {
            permissions = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
            rationale = R.string.codeless_storage_permission_rationale;
        } else {
            permissions = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
            rationale = R.string.codeless_storage_permission_rationale_no_log;
        }
        return permissionChecker.checkPermissions(permissions, rationale, callback);
    }

    synchronized public void connect() {
        Log.d(TAG, logPrefix + "Connect");
        if (state != DISCONNECTED)
            return;
        state = CONNECTING;
        EventBus.getDefault().post(new CodelessEvent.Connection(this));
        if (CodelessLibConfig.BLUETOOTH_STATE_MONITOR)
            context.registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        if (Build.VERSION.SDK_INT < 23) {
            gatt = device.connectGatt(context, false, gattCallback);
        } else {
            gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
        }
    }

    synchronized public void disconnect() {
        Log.d(TAG, logPrefix + "Disconnect");
        if (gatt == null)
            return;
        gatt.disconnect();
        if (state == CONNECTING) {
            state = DISCONNECTED;
            gatt.close();
            gatt = null;
            if (CodelessLibConfig.BLUETOOTH_STATE_MONITOR)
                context.unregisterReceiver(bluetoothStateReceiver);
            EventBus.getDefault().post(new CodelessEvent.Connection(this));
        }
    }

    public boolean isConnected() {
        return state >= CONNECTED;
    }

    public boolean isConnecting() {
        return state == CONNECTING;
    }

    public boolean isDisconnected() {
        return state == DISCONNECTED;
    }

    public boolean servicesDiscovered() {
        return servicesDiscovered;
    }

    public boolean codelessSupport() {
        return codelessSupport;
    }

    public boolean dspsSupport() {
        return dspsSupport;
    }

    public boolean isReady() {
        return state == READY;
    }

    private boolean checkReady() {
        if (!isReady()) {
            Log.e(TAG, logPrefix + "Device not ready. Operation not allowed.");
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_NOT_READY));
            return false;
        } else {
            return true;
        }
    }

    public boolean hasDeviceName() {
        return gatt.getService(Uuid.GAP_SERVICE) != null && gatt.getService(Uuid.GAP_SERVICE).getCharacteristic(Uuid.GAP_DEVICE_NAME) != null;
    }

    public void readDeviceName() {
        if (!hasDeviceName()) {
            Log.e(TAG, logPrefix + "Device name not available");
            return;
        }
        readCharacteristic(gatt.getService(Uuid.GAP_SERVICE).getCharacteristic(Uuid.GAP_DEVICE_NAME));
    }

    private void onDeviceNameRead(BluetoothGattCharacteristic characteristic) {
        String name = new String(characteristic.getValue(), StandardCharsets.UTF_8);
        Log.d(TAG, logPrefix + "Device name : " + name);
        EventBus.getDefault().post(new CodelessEvent.DeviceName(this, name));
    }

    public boolean hasDeviceInfo(UUID uuid) {
        return deviceInfoService != null && (uuid == null || deviceInfoService.getCharacteristic(uuid) != null);
    }

    public void readDeviceInfo(UUID uuid) {
        if (!hasDeviceInfo(uuid)) {
            Log.e(TAG, logPrefix + "Device information not available: " + uuid);
            return;
        }
        readCharacteristic(deviceInfoService.getCharacteristic(uuid));
    }

    private void onDeviceInfoRead(BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        byte[] value = characteristic.getValue();
        String info = new String(value, StandardCharsets.UTF_8);
        Log.d(TAG, logPrefix + "Device information ["+ uuid + "]: " + info);
        EventBus.getDefault().post(new CodelessEvent.DeviceInfo(this, uuid, value, info));
    }

    public void getRssi() {
        if (isConnected())
            gatt.readRemoteRssi();
    }

    private void onDeviceReady() {
        Log.d(TAG, logPrefix + "Device ready");
        state = READY;
        if (codelessSupport) {
            commandMode = CodelessLibConfig.START_IN_COMMAND_MODE;
        }
        if (dspsSupport) {
            if (CodelessLibConfig.SET_FLOW_CONTROL_ON_CONNECTION)
                setDspsRxFlowOn(dspsRxFlowOn);
            if (CodelessLibConfig.DSPS_STATS) {
                if (!commandMode) {
                    dspsRxBytesInterval = 0;
                    dspsLastInterval = new Date().getTime();
                    dspsStatsHandler.postDelayed(dspsUpdateStats, CodelessLibConfig.DSPS_STATS_INTERVAL);
                }
            }
        }
        EventBus.getDefault().post(new CodelessEvent.Ready(this));
    }

    public boolean commandMode() {
        return commandMode;
    }

    public boolean binaryMode() {
        return !commandMode;
    }

    private boolean checkBinaryMode(boolean outbound) {
        if (outbound) {
            if (!dspsSupport) {
                Log.e(TAG, logPrefix + "DSPS not supported");
                EventBus.getDefault().post(new CodelessEvent.Error(this, CodelessEvent.ERROR_OPERATION_NOT_ALLOWED));
                return false;
            }
            if (!CodelessLibConfig.ALLOW_OUTBOUND_BINARY_IN_COMMAND_MODE) {
                if (commandMode) {
                    Log.e(TAG, logPrefix + "Binary data not allowed in command mode");
                    EventBus.getDefault().post(new CodelessEvent.Error(this, CodelessEvent.ERROR_OPERATION_NOT_ALLOWED));
                    return false;
                }
            }
        } else {
            if (!CodelessLibConfig.ALLOW_INBOUND_BINARY_IN_COMMAND_MODE) {
                if (commandMode) {
                    Log.e(TAG, logPrefix + "Received binary data in command mode");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkCommandMode(boolean outbound, CodelessCommand command) {
        if (outbound) {
            if (!codelessSupport) {
                Log.e(TAG, logPrefix + "Codeless not supported");
                EventBus.getDefault().post(new CodelessEvent.Error(this, CodelessEvent.ERROR_OPERATION_NOT_ALLOWED));
                return false;
            }
            if (!CodelessLibConfig.ALLOW_OUTBOUND_COMMAND_IN_BINARY_MODE) {
                if (!commandMode && (command == null || !CodelessProfile.Command.isModeCommand(command))) {
                    Log.e(TAG, logPrefix + "Commands not allowed in binary mode");
                    EventBus.getDefault().post(new CodelessEvent.Error(this, CodelessEvent.ERROR_OPERATION_NOT_ALLOWED));
                    return false;
                }
            }
        } else {
            if (!CodelessLibConfig.ALLOW_INBOUND_COMMAND_IN_BINARY_MODE) {
                if (!commandMode && (command == null || !CodelessProfile.Command.isModeCommand(command))) {
                    Log.e(TAG, logPrefix + "Received command in binary mode");
                    return false;
                }
            }
        }
        return true;
    }

    public void setMode(boolean command) {
        if (commandMode == command)
            return;
        Log.d(TAG, logPrefix + "Change to "+ (command ? "command": "binary") + " mode");
        if (!command) {
            sendCommand(CodelessLibConfig.MODE_CHANGE_SEND_BINARY_REQUEST ? new BinRequestCommand(this) : new BinRequestAckCommand(this));
        } else {
            sendCommand(new BinExitCommand(this));
        }
    }

    public void acceptBinaryModeRequest() {
        if (!binaryRequestPending) {
            Log.e(TAG, logPrefix + "No binary mode request pending");
            return;
        }
        binaryRequestPending = false;
        Log.d(TAG, logPrefix + "Binary mode request accepted");
        sendCommand(new BinRequestAckCommand(this));
    }

    public void acceptBinaryModeExitRequest() {
        if (!binaryExitRequestPending) {
            Log.e(TAG, logPrefix + "No binary mode exit request pending");
            return;
        }
        binaryExitRequestPending = false;
        Log.d(TAG, logPrefix + "Binary mode exit request accepted");
        sendCommand(new BinExitAckCommand(this));
    }

    synchronized private void enterBinaryMode() {
        if (!commandMode)
            return;
        Log.d(TAG, logPrefix + "Enter binary mode");
        commandMode = false;
        EventBus.getDefault().post(new CodelessEvent.Mode(this, commandMode));

        // Remove pending commands
        Iterator<CodelessCommand> i = commandQueue.iterator();
        while (i.hasNext()) {
            if (!CodelessProfile.Command.isModeCommand(i.next()))
                i.remove();
        }

        if (CodelessLibConfig.CODELESS_LOG)
            codelessLogFile.log("=========== BINARY MODE ==========");

        if (CodelessLibConfig.DSPS_STATS) {
            dspsRxBytesInterval = 0;
            dspsLastInterval = new Date().getTime();
            dspsStatsHandler.postDelayed(dspsUpdateStats, CodelessLibConfig.DSPS_STATS_INTERVAL);
        }
        resumeDspsOperations();
    }

    synchronized private void exitBinaryMode() {
        if (commandMode)
            return;
        Log.d(TAG, logPrefix + "Exit binary mode");
        commandMode = true;
        EventBus.getDefault().post(new CodelessEvent.Mode(this, commandMode));

        if (CodelessLibConfig.DSPS_STATS) {
            dspsStatsHandler.removeCallbacks(dspsUpdateStats);
        }
        pauseDspsOperations(false);

        if (CodelessLibConfig.CODELESS_LOG)
            codelessLogFile.log("=========== COMMAND MODE ==========");
    }

    public void onBinRequestSent() {
    }

    public void onBinRequestReceived() {
        if (!commandMode) {
            Log.d(TAG, logPrefix + "Already in binary mode");
            sendCommand(new BinRequestAckCommand(this));
            return;
        }
        if (CodelessLibConfig.HOST_BINARY_REQUEST) {
            Log.d(TAG, logPrefix + "Pass binary mode request to host");
            binaryRequestPending = true;
            EventBus.getDefault().post(new CodelessEvent.BinaryModeRequest(this));
        } else {
            sendCommand(new BinRequestAckCommand(this));
        }
    }

    public void onBinAckSent() {
        enterBinaryMode();
    }

    public void onBinAckReceived() {
        enterBinaryMode();
    }

    public void onBinExitSent() {
        exitBinaryMode();
    }

    public void onBinExitReceived() {
        if (commandMode) {
            Log.d(TAG, logPrefix + "Already in command mode");
            sendCommand(new BinExitAckCommand(this));
            return;
        }
        exitBinaryMode();
        sendCommand(new BinExitAckCommand(this));
    }

    public void onBinExitAckSent() {
    }

    public void onBinExitAckReceived() {
    }

    public boolean isCommandPending() {
        return commandPending != null;
    }

    public CodelessCommand getCommandPending() {
        return commandPending;
    }

    public boolean isInboundPending() {
        return inboundPending > 0;
    }

    public int getInboundPending() {
        return inboundPending;
    }

    public CodelessCommands getCommandFactory() {
        return commandFactory;
    }

    public void sendCommand(String line) {
        if (!line.trim().isEmpty())
            sendCommand(parseTextCommand(line));
    }

    public void sendCommandScript(Collection<String> script) {
        ArrayList<CodelessCommand> commands = new ArrayList<>();
        for (String line : script) {
            if (!line.trim().isEmpty())
                commands.add(parseTextCommand(line));
        }
        sendCommands(commands);
    }

    public CodelessCommand parseTextCommand(String line) {
        line = line.trim();
        if (CodelessLibConfig.AUTO_ADD_PREFIX && !CodelessProfile.hasPrefix(line))
            line = CodelessProfile.PREFIX + line;

        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Text command: " + line);

        if (CodelessLibConfig.CODELESS_LOG)
            codelessLogFile.logText(line);

        CodelessCommand command;
        String id = CodelessProfile.getCommand(line);
        if (id == null) {
            command = new CustomCommand(this, line, true);
        } else {
            Class<? extends CodelessCommand> commandClass = CodelessProfile.Command.commandMap.get(id);
            if (commandClass == null) {
                command = new CustomCommand(this, line, true);
            } else {
                String prefix = CodelessProfile.getPrefix(line);
                line = CodelessProfile.removeCommandPrefix(line);
                command = CodelessProfile.createCommand(this, commandClass, line);
                command.setPrefix(prefix);
            }
        }

        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Text command identified: " + command + (command.isValid() ? "" : " (invalid)"));
        return command;
    }

    public void sendCommand(CodelessCommand command) {
        if (!checkReady() || !checkCommandMode(true, command))
            return;
        enqueueCommand(command);
    }

    public void sendCommands(Collection<CodelessCommand> commands) {
        if (!checkReady() || !checkCommandMode(true, null))
            return;
        enqueueCommands(commands);
    }

    synchronized private void enqueueCommand(CodelessCommand command) {
        if (commandPending != null || commandInbound != null || inboundPending > 0) {
            commandQueue.add(command);
        } else {
            commandPending = command;
            executeCommand(command);
        }
    }

    synchronized private void enqueueCommands(Collection<CodelessCommand> commands) {
        commandQueue.addAll(commands);
        if (commandPending == null) {
            dequeueCommand();
        }
    }

    synchronized private void dequeueCommand() {
        if (commandQueue.isEmpty() || commandInbound != null || inboundPending > 0)
            return;
        commandPending = commandQueue.poll();
        executeCommand(commandPending);
    }

    synchronized private void commandComplete(boolean dequeue) {
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Command complete: " + commandPending);
        parsePending.clear();
        commandPending = null;
        if (dequeue)
            dequeueCommand();
    }

    synchronized private void inboundCommandComplete() {
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Inbound command complete: " + commandInbound);
        if (CodelessLibConfig.SINGLE_WRITE_RESPONSE)
            parsePending.clear();
        else
            outboundResponseLines = 0;
        commandInbound = null;
        dequeueCommand();
    }

    private void executeCommand(CodelessCommand command) {
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Send codeless command: " + command);
        if (!checkReady()) {
            command.setComplete();
            commandComplete(true);
            return;
        }

        if (!command.isParsed())
            command.packCommand();
        String text = command.getCommand();
        if (command.getCommandID() != CodelessProfile.CommandID.CUSTOM) {
            String prefix = !CodelessProfile.Command.isModeCommand(command) ? CodelessProfile.PREFIX_REMOTE : CodelessProfile.PREFIX_LOCAL;
            text = prefix + text.replaceFirst(CodelessProfile.PREFIX_PATTERN_STRING, "");
        } else if (CodelessLibConfig.DISALLOW_INVALID_PREFIX && !CodelessProfile.hasPrefix(text)) {
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Invalid prefix: " + text);
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_INVALID_PREFIX));
            command.setComplete();
            commandComplete(true);
            return;
        }

        if (CodelessLibConfig.DISALLOW_INVALID_COMMAND && !command.isParsed() && !command.isValid()) {
            Log.e(TAG, logPrefix + "Invalid command: " + text);
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_INVALID_COMMAND));
            command.setComplete();
            commandComplete(true);
            return;
        }

        if (CodelessLibConfig.DISALLOW_INVALID_PARSED_COMMAND && command.isParsed() && !command.isValid()) {
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Invalid command: " + text);
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_INVALID_COMMAND));
            command.setComplete();
            commandComplete(true);
            return;
        }

        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Codeless command text: " + text);
        sendText(text, LineType.OutboundCommand);
    }

    public void completePendingCommand(CodelessCommand command) {
        if (commandPending != command) {
            Log.e(TAG, logPrefix + "Not current pending command: " + command);
            return;
        }
        commandPending.setComplete();
        commandComplete(true);
    }

    private void processCodelessLine(String line, LineType type) {
        CodelessProfile.Line codelessLine = new CodelessProfile.Line(line, type);
        if (CodelessLibConfig.CODELESS_LOG)
            codelessLogFile.log(codelessLine);
        if (CodelessLibConfig.LINE_EVENTS)
            EventBus.getDefault().post(new CodelessEvent.CodelessLine(this, codelessLine));
    }

    public void sendSuccess() {
        if (commandInbound == null) {
            Log.e(TAG, logPrefix + "No inbound command pending");
            return;
        }
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Send success: " + commandInbound);
        if (CodelessLibConfig.SINGLE_WRITE_RESPONSE) {
            sendText(createSingleWriteResponse(true, null), LineType.OutboundResponse);
        } else {
            sendText(!CodelessLibConfig.EMPTY_LINE_BEFORE_OK || outboundResponseLines > 0 ? CodelessProfile.OK : "\n" + CodelessProfile.OK, LineType.OutboundOK);
        }
        if (commandInbound.isComplete())
            inboundCommandComplete();
    }

    public void sendSuccess(String response) {
        if (commandInbound == null) {
            Log.e(TAG, logPrefix + "No inbound command pending");
            return;
        }
        if (CodelessLibConfig.SINGLE_WRITE_RESPONSE) {
            if (CodelessLibLog.CODELESS) {
                Log.d(TAG, logPrefix + "Send response: " + commandInbound + " " + response);
                Log.d(TAG, logPrefix + "Send success: " + commandInbound);
            }
            sendText(response + "\n" + CodelessProfile.OK, LineType.OutboundResponse);
            if (commandInbound.isComplete())
                inboundCommandComplete();
        } else {
            sendResponse(response);
            sendSuccess();
        }
    }

    public void sendError(String error) {
        if (commandInbound == null) {
            Log.e(TAG, logPrefix + "No inbound command pending");
            return;
        }
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Send error: " + commandInbound + " " + error);
        if (CodelessLibConfig.SINGLE_WRITE_RESPONSE) {
            sendText(createSingleWriteResponse(false, error), LineType.OutboundError);
        } else {
            sendText(!CodelessLibConfig.EMPTY_LINE_BEFORE_ERROR || outboundResponseLines > 0 ? error : "\n" + error, LineType.OutboundError);
        }
        if (commandInbound.isComplete())
            inboundCommandComplete();
    }

    public void sendResponse(String response) {
        if (commandInbound == null) {
            Log.e(TAG, logPrefix + "No inbound command pending");
            return;
        }
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Send response: " + commandInbound + " " + response);
        if (CodelessLibConfig.SINGLE_WRITE_RESPONSE) {
            parsePending.add(response);
        } else {
            sendText(response, LineType.OutboundResponse);
        }
    }

    public void completeInboundCommand(CodelessCommand command) {
        if (commandInbound != command) {
            Log.e(TAG, logPrefix + "Not current inbound command: " + command);
            return;
        }
        commandInbound.setComplete();
        inboundCommandComplete();
    }

    private void sendParseError(String error) {
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Send error: " + error);
        error = CodelessProfile.ERROR_PREFIX + error;
        if (CodelessLibConfig.SINGLE_WRITE_RESPONSE) {
            sendText(error + "\n" + CodelessProfile.ERROR, LineType.OutboundError);
        } else {
            sendText(error, LineType.OutboundError);
            sendText(CodelessProfile.ERROR, LineType.OutboundError);
            outboundResponseLines = 0;
        }
    }

    private String createSingleWriteResponse(boolean success, String message) {
        StringBuilder text = new StringBuilder();
        for (String line : parsePending) {
            text.append(line).append("\n");
        }
        parsePending.clear();
        if (message != null)
            text.append(message).append("\n");
        if (success) {
            if (CodelessLibConfig.EMPTY_LINE_BEFORE_OK && text.length() == 0)
                text.append("\n");
            text.append(CodelessProfile.OK);
        } else {
            if (CodelessLibConfig.EMPTY_LINE_BEFORE_ERROR && text.length() == 0)
                text.append("\n");
            text.append(CodelessProfile.ERROR);
        }
        return text.toString();
    }

    private void sendText(String text, LineType type) {
        if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS) {
            for (String line : text.split("\n", -1)) {
                LineType lineType = type;
                if (line.isEmpty())
                    lineType = LineType.OutboundEmpty;
                else if (CodelessProfile.isSuccess(line))
                    lineType = LineType.OutboundOK;
                processCodelessLine(line, lineType);
            }
        }

        if (!CodelessLibConfig.SINGLE_WRITE_RESPONSE && type != LineType.OutboundCommand)
            outboundResponseLines++;

        if (!text.endsWith("\n") && CodelessLibConfig.APPEND_END_OF_LINE && (CodelessLibConfig.END_OF_LINE_AFTER_COMMAND || type != LineType.OutboundCommand))
            text += "\n";
        if (!CodelessLibConfig.END_OF_LINE.equals("\n"))
            text = text.replace("\n", CodelessLibConfig.END_OF_LINE);

        byte[] data = text.getBytes(CodelessLibConfig.CHARSET);
        if (CodelessLibConfig.TRAILING_ZERO || !CodelessLibConfig.APPEND_END_OF_LINE || type == LineType.OutboundCommand && !CodelessLibConfig.END_OF_LINE_AFTER_COMMAND)
            data = Arrays.copyOf(data, data.length + 1);
        writeCharacteristic(codelessInbound, data);
    }

    private void parseCommandResponse(String line) {
        if (line.isEmpty()) {
            if (parsePending.isEmpty()) {
                if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                    processCodelessLine(line, LineType.InboundEmpty);
            } else {
                parsePending.add(line);
            }
            return;
        }
        if (CodelessProfile.isSuccess(line)) {
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Received OK");
            for (String response : parsePending) {
                if (response.isEmpty()) {
                    if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                        processCodelessLine(response, LineType.InboundEmpty);
                    continue;
                }
                if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                    processCodelessLine(response, LineType.InboundResponse);
                commandPending.parseResponse(response);
            }
            parsePending.clear();
            if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                processCodelessLine(line, LineType.InboundOK);
            commandPending.onSuccess();
        } else if (CodelessProfile.isError(line)) {
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Received ERROR");
            StringBuilder error = new StringBuilder();
            for (String msg : parsePending) {
                if (msg.isEmpty()) {
                    if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                        processCodelessLine(msg, LineType.InboundEmpty);
                    continue;
                }
                if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                    processCodelessLine(msg, LineType.InboundError);
                if (CodelessProfile.isPeerInvalidCommand(msg))
                    commandPending.setPeerInvalid();
                if (CodelessProfile.isErrorCodeMessage(msg)) {
                    CodelessProfile.ErrorCodeMessage ec = CodelessProfile.parseErrorCodeMessage(msg);
                    commandPending.setErrorCode(ec.code, ec.message);
                }
                if (error.length() > 0)
                    error.append("\n");
                error.append(msg);
            }
            parsePending.clear();
            if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                processCodelessLine(line, LineType.InboundError);
            commandPending.onError(error.length() > 0 ? error.toString() : line);
        } else if (CodelessProfile.isErrorMessage(line)) {
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Received potential error: " + line);
            parsePending.add(line);
        } else {
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Received response: " + line);
            if (parsePending.isEmpty() && commandPending.parsePartialResponse()) {
                if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                    processCodelessLine(line, LineType.InboundResponse);
                commandPending.parseResponse(line);
            } else {
                parsePending.add(line);
            }
        }
        if (commandPending != null && commandPending.isComplete())
            commandComplete(false);
    }

    private void parseInboundCommand(String line) {
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Received command: " + line);
        if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
            processCodelessLine(line, LineType.InboundCommand);

        if (commandInbound != null) {
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Inbound command in progress. Ignore inbound data.");
            return;
        }

        CodelessCommand hostCommand = null;
        String id = CodelessProfile.getCommand(line);
        if (id == null) {
            if (CodelessLibConfig.HOST_INVALID_COMMANDS) {
                hostCommand = new CustomCommand(this, line, true);
            } else {
                sendParseError(CodelessProfile.INVALID_COMMAND);
            }
        } else {
            Class<? extends CodelessCommand> commandClass = CodelessProfile.Command.commandMap.get(id);
            if (commandClass == null) {
                if (CodelessLibConfig.HOST_UNSUPPORTED_COMMANDS) {
                    hostCommand = new CustomCommand(this, line, true);
                } else {
                    sendParseError(CodelessProfile.COMMAND_NOT_SUPPORTED);
                }
            } else {
                line = CodelessProfile.removeCommandPrefix(line);
                CodelessCommand command = CodelessProfile.createCommand(this, commandClass, line);
                if (CodelessLibConfig.hostCommands.contains(command.getCommandID())) {
                    hostCommand = command;
                } else if (CodelessLibConfig.supportedCommands.contains(command.getCommandID())) {
                    if (!checkCommandMode(false, command))
                        return;
                    if (CodelessLibLog.CODELESS)
                        Log.d(TAG, logPrefix + "Library command: " + command);
                    commandInbound = command;
                    commandInbound.setInbound();
                    EventBus.getDefault().post(new CodelessEvent.InboundCommand(commandInbound));
                    if (!command.isValid()) {
                        if (CodelessLibLog.CODELESS)
                            Log.d(TAG, logPrefix + "Invalid command: " + command + " " + command.getError());
                        commandInbound.setComplete();
                        sendError(CodelessProfile.ERROR_PREFIX + commandInbound.getError());
                    } else {
                        commandInbound.processInbound();
                    }
                } else {
                    sendParseError(CodelessProfile.COMMAND_NOT_SUPPORTED);
                }
            }
        }

        if (hostCommand != null) {
            if (!checkCommandMode(false, hostCommand))
                return;
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Host command: " + hostCommand);
            commandInbound = hostCommand;
            commandInbound.setInbound();
            EventBus.getDefault().post(new CodelessEvent.HostCommand(commandInbound));
        }
    }

    private void onCodelessFlowControl(byte[] data) {
        if (data.length > 0 && data[0] == CodelessProfile.CODELESS_DATA_PENDING) {
            inboundPending++;
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Pending codeless inbound data: " + inboundPending);
            readCharacteristic(codelessOutbound);
        } else {
            Log.e(TAG, logPrefix + "Invalid codeless flow control value: " + CodelessUtil.hexArrayLog(data));
        }
    }

    private void onCodelessInbound(byte[] data) {
        if (CodelessLibLog.CODELESS)
            Log.d(TAG, logPrefix + "Codeless inbound data: " + CodelessUtil.hexArrayLog(data));

        // Remove trailing zero
        if (data.length > 0 && data[data.length - 1] == 0)
            data = Arrays.copyOf(data, data.length - 1);

        if (data.length == 0) {
            if (CodelessLibLog.CODELESS)
                Log.d(TAG, logPrefix + "Received empty buffer");
        }

        String inbound = new String(data, CodelessLibConfig.CHARSET);
        inbound = inbound.replace("\r\n", "\n");
        inbound = inbound.replace("\n\r", "\n");
        inbound = inbound.replace("\r", "\n");
        if (inbound.endsWith("\n"))
            inbound = inbound.substring(0, inbound.length() - 1);
        String[] lines = inbound.split("\n", -1);

        synchronized (this) {
            inboundPending--;

            for (String line : lines) {
                line = line.trim();
                if (commandPending != null) {
                    parseCommandResponse(line);
                } else {
                    if (line.isEmpty()) {
                        if (CodelessLibConfig.CODELESS_LOG || CodelessLibConfig.LINE_EVENTS)
                            processCodelessLine(line, LineType.InboundEmpty);
                        continue;
                    }
                    parseInboundCommand(line);
                }
            }

            if (commandPending == null || commandPending.isComplete())
                dequeueCommand();
        }
    }

    public int getDspsChunkSize() {
        return dspsChunkSize;
    }

    public void setDspsChunkSize(int dspsChunkSize) {
        this.dspsChunkSize = dspsChunkSize;
    }

    public boolean getDspsEcho() {
        return dspsEcho;
    }

    public void setDspsEcho(boolean dspsEcho) {
        this.dspsEcho = dspsEcho;
    }

    public void sendBinaryData(String data) {
        sendDspsData(data);
    }

    public void sendHexData(String hex) {
        sendDspsHexData(hex);
    }

    public void sendBinaryData(byte[] data) {
        sendDspsData(data, dspsChunkSize);
    }

    public void sendBinaryData(byte[] data, int chunkSize) {
        sendDspsData(data, chunkSize);
    }

    public void sendDspsData(String data) {
        if (CodelessLibLog.DSPS_DATA)
            Log.d(TAG, logPrefix + "DSPS TX text: " + data);
        sendDspsData(data.getBytes(CodelessLibConfig.CHARSET));
    }

    public void sendDspsHexData(String hex) {
        if (CodelessLibLog.DSPS_DATA)
            Log.d(TAG, logPrefix + "DSPS TX hex: " + hex);
        byte[] data = CodelessUtil.hex2bytes(hex);
        if (data != null)
            sendDspsData(data);
        else
            Log.e(TAG, logPrefix + "Invalid hex data: " + hex);
    }

    public void sendDspsData(byte[] data) {
        sendDspsData(data, dspsChunkSize);
    }

    public void sendDspsData(byte[] data, int chunkSize) {
        if (!checkReady() || !checkBinaryMode(true))
            return;
        if (CodelessLibLog.DSPS_DATA)
            Log.d(TAG, logPrefix + "DSPS TX data: " + CodelessUtil.hexArrayLog(data));
        if (chunkSize > dspsChunkSize)
            chunkSize = dspsChunkSize;
        if (data.length <= chunkSize) {
            if (dspsTxFlowOn) {
                enqueueGattOperation(new DspsChunkOperation(data));
            } else if (dspsPending.size() <= CodelessLibConfig.DSPS_PENDING_MAX_SIZE) {
                synchronized (this) {
                    dspsPending.add(new DspsChunkOperation(data));
                }
            } else {
                if (CodelessLibLog.DSPS)
                    Log.d(TAG, "DSPS TX data dropped (flow off, queue full)");
            }
        } else {
            ArrayList<GattOperation> chunks = new ArrayList<>();
            for (int i = 0; i < data.length; i += chunkSize) {
                chunks.add(new DspsChunkOperation(Arrays.copyOfRange(data, i, Math.min(i + chunkSize, data.length))));
            }
            if (dspsTxFlowOn) {
                enqueueGattOperations(chunks);
            } else if (dspsPending.size() <= CodelessLibConfig.DSPS_PENDING_MAX_SIZE) {
                synchronized (this) {
                    dspsPending.addAll(chunks);
                }
            } else {
                if (CodelessLibLog.DSPS)
                    Log.d(TAG, "DSPS TX data dropped (flow off, queue full)");
            }
        }
    }

    private void onDspsData(byte[] data) {
        if (CodelessLibLog.DSPS_DATA)
            Log.d(TAG, logPrefix + "DSPS RX data: " + CodelessUtil.hexArrayLog(data));
        if (!checkBinaryMode(false))
            return;
        if (dspsEcho)
            sendDspsData(data);
        if (dspsFileReceive != null)
            dspsFileReceive.onDspsData(data);
        if (CodelessLibConfig.DSPS_RX_LOG && (dspsFileReceive == null || CodelessLibConfig.DSPS_RX_FILE_LOG_DATA))
            dspsRxLogFile.log(data);
        if (CodelessLibConfig.DSPS_STATS) {
            synchronized (this) {
                dspsRxBytesInterval += data.length;
            }
        }
        EventBus.getDefault().post(new CodelessEvent.DspsRxData(this, data));
    }

    public boolean isDspsRxFlowOn() {
        return dspsRxFlowOn;
    }

    public void setDspsRxFlowOn(boolean on) {
        dspsRxFlowOn = on;
        if (CodelessLibLog.DSPS)
            Log.d(TAG, logPrefix + "DSPS RX flow control: " + (dspsRxFlowOn ? "ON" : "OFF"));
        byte[] data = new byte[] { dspsRxFlowOn ? (byte) CodelessProfile.DSPS_XON : (byte) CodelessProfile.DSPS_XOFF };
        writeCharacteristic(dspsFlowControl, data, false);
        EventBus.getDefault().post(new CodelessEvent.DspsRxFlowControl(this, dspsRxFlowOn));
    }

    public boolean isDspsTxFlowOn() {
        return dspsTxFlowOn;
    }

    synchronized private void onDspsFlowControl(byte[] data) {
        int value = data.length > 0 ? data[0] : Integer.MIN_VALUE;
        boolean prev = dspsTxFlowOn;
        switch (value) {
            case CodelessProfile.DSPS_XON:
                dspsTxFlowOn = true;
                break;
            case CodelessProfile.DSPS_XOFF:
                dspsTxFlowOn = false;
                break;
            default:
                Log.d(TAG, logPrefix + "Invalid DSPS TX flow control value: " + value);
                return;
        }

        if (prev == dspsTxFlowOn)
            return;

        if (CodelessLibLog.DSPS)
            Log.d(TAG, logPrefix + "DSPS TX flow control: " + (dspsTxFlowOn ? "ON" : "OFF"));
        EventBus.getDefault().post(new CodelessEvent.DspsTxFlowControl(this, dspsTxFlowOn));

        if (dspsTxFlowOn) {
            resumeDspsOperations();
        } else {
            pauseDspsOperations(true);
        }
    }

    public DspsFileSend sendFile(File file, int chunkSize, int period) {
        DspsFileSend operation = new DspsFileSend(this, file, chunkSize, period);
        if (operation.isLoaded())
            operation.start();
        return operation;
    }

    public DspsFileSend sendFile(File file, int period) {
        return sendFile(file, dspsChunkSize, period);
    }

    public DspsFileSend sendFile(File file) {
        return sendFile(file, dspsChunkSize, 0);
    }

    public DspsFileSend sendFile(Uri uri, int chunkSize, int period) {
        DspsFileSend operation = new DspsFileSend(this, uri, chunkSize, period);
        if (operation.isLoaded())
            operation.start();
        return operation;
    }

    public DspsFileSend sendFile(Uri uri, int period) {
        return sendFile(uri, dspsChunkSize, period);
    }

    public DspsFileSend sendFile(Uri uri) {
        return sendFile(uri, dspsChunkSize, 0);
    }

    // INTERNAL
    synchronized public void start(DspsFileSend operation, boolean resume) {
        if (!checkReady() || !checkBinaryMode(true))
            return;
        if (!resume)
            dspsFiles.add(operation);
        if (!dspsTxFlowOn)
            return;
        if (operation.getPeriod() > 0) {
            handler.postDelayed(operation.getRunnable(), resume ? operation.getPeriod() : 0);
        } else {
            if (CodelessLibLog.DSPS_FILE_CHUNK)
                Log.d(TAG, logPrefix + "Queue all file chunks: " + operation);
            ArrayList<GattOperation> chunks = new ArrayList<>();
            for (int i = resume ? operation.getChunk() : 0; i < operation.getTotalChunks(); i++) {
                chunks.add(new DspsFileChunkOperation(operation, operation.getChunks()[i], i + 1));
            }
            enqueueGattOperations(chunks);
        }
    }

    // INTERNAL
    synchronized public void stop(final DspsFileSend operation) {
        dspsFiles.remove(operation);
        handler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(operation.getRunnable());
                removePendingDspsFileChunkOperations(operation);
            }
        });
    }

    // INTERNAL
    public void sendData(DspsFileSend operation) {
        enqueueGattOperation(new DspsFileChunkOperation(operation, operation.getCurrentChunk(), operation.getChunk() + 1));
    }

    public DspsPeriodicSend sendPattern(File file, int chunkSize, int period) {
        DspsPeriodicSend operation = new DspsPeriodicSend(this, file, chunkSize, period);
        if (operation.isLoaded())
            operation.start();
        return operation;
    }

    public DspsPeriodicSend sendPattern(File file, int period) {
        return sendPattern(file, dspsChunkSize, period);
    }

    public DspsPeriodicSend sendPattern(File file) {
        return sendPattern(file, dspsChunkSize, 0);
    }

    public DspsPeriodicSend sendPattern(Uri uri, int chunkSize, int period) {
        DspsPeriodicSend operation = new DspsPeriodicSend(this, uri, chunkSize, period);
        if (operation.isLoaded())
            operation.start();
        return operation;
    }

    public DspsPeriodicSend sendPattern(Uri uri, int period) {
        return sendPattern(uri, dspsChunkSize, period);
    }

    public DspsPeriodicSend sendPattern(Uri uri) {
        return sendPattern(uri, dspsChunkSize, 0);
    }

    // INTERNAL
    synchronized public void start(DspsPeriodicSend operation) {
        if (!checkReady() || !checkBinaryMode(true))
            return;
        dspsPeriodic.add(operation);
        if (dspsTxFlowOn)
            handler.post(operation.getRunnable());
    }

    // INTERNAL
    synchronized public void stop(final DspsPeriodicSend operation) {
        dspsPeriodic.remove(operation);
        handler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(operation.getRunnable());
                removePendingDspsPeriodicChunkOperations(operation);
            }
        });
    }

    // INTERNAL
    public void sendData(DspsPeriodicSend operation) {
        byte[] data = operation.getData();
        int chunkSize =  operation.getChunkSize();
        if (chunkSize > dspsChunkSize)
            chunkSize = dspsChunkSize;
        int totalChunks = data.length / chunkSize + (data.length % chunkSize != 0 ? 1 : 0);
        if (totalChunks == 1) {
            enqueueGattOperation(new DspsPeriodicChunkOperation(operation, operation.getCount(), operation.getData(), 1, 1));
        } else {
            ArrayList<GattOperation> chunks = new ArrayList<>();
            for (int i = 0; i < data.length; i += chunkSize) {
                chunks.add(new DspsPeriodicChunkOperation(operation, operation.getCount(), Arrays.copyOfRange(data, i, Math.min(i + chunkSize, data.length)), i / chunkSize + 1, totalChunks));
            }
            enqueueGattOperations(chunks);
        }
    }

    private void pauseDspsOperations(boolean keepPending) {
        // Remove pending operations
        handler.removeCallbacks(resumeDspsOperations);
        handler.postAtFrontOfQueue(pauseDspsOperations);
        removePendingDspsChunkOperations(keepPending);
        if (!keepPending)
            dspsPending.clear();
    }

    private Runnable pauseDspsOperations = new Runnable() {
        @Override
        public void run() {
            synchronized (CodelessManager.this) {
                handler.removeCallbacks(pauseDspsOperations);
                for (DspsPeriodicSend operation : dspsPeriodic) {
                    handler.removeCallbacks(operation.getRunnable());
                    int count = removePendingDspsPeriodicChunkOperations(operation);
                    if (count > 0)
                        operation.setResumeCount(count);
                }
                for (DspsFileSend operation : dspsFiles) {
                    handler.removeCallbacks(operation.getRunnable());
                    int chunk = removePendingDspsFileChunkOperations(operation);
                    if (chunk > 0)
                        operation.setResumeChunk(chunk);
                }
            }
        }
    };

    private void resumeDspsOperations() {
        // Send pending data
        enqueueGattOperations(dspsPending);
        dspsPending.clear();
        // Resume operations
        handler.post(resumeDspsOperations);
    }

    private Runnable resumeDspsOperations = new Runnable() {
        @Override
        public void run() {
            synchronized (CodelessManager.this) {
                for (DspsPeriodicSend operation : dspsPeriodic) {
                    handler.postDelayed(operation.getRunnable(), operation.getPeriod());
                }
                for (DspsFileSend operation : dspsFiles) {
                    start(operation, true);
                }
            }
        }
    };

    synchronized public void start(DspsFileReceive operation) {
        if (!checkReady() || !checkBinaryMode(true))
            return;
        if (dspsFileReceive != null)
            dspsFileReceive.stop();
        dspsFileReceive = operation;
    }

    synchronized public void stop(DspsFileReceive operation) {
        if (dspsFileReceive == operation)
            dspsFileReceive = null;
    }

    public DspsFileReceive receiveFile() {
        DspsFileReceive operation = new DspsFileReceive(this);
        operation.start();
        return operation;
    }

    public DspsFileReceive getDspsFileReceive() {
        return dspsFileReceive;
    }

    public Handler getDspsStatsHandler() {
        return dspsStatsHandler;
    }

    public int getDspsRxSpeed() {
        return dspsRxSpeed;
    }

    private Runnable dspsUpdateStats = new Runnable() {
        @Override
        public void run() {
            if (commandMode)
                return;
            synchronized (CodelessManager.this) {
                long now = new Date().getTime();
                if (now == dspsLastInterval)
                    now++;
                dspsRxSpeed = (int) (dspsRxBytesInterval * 1000L / (now - dspsLastInterval));
                dspsLastInterval = now;
                dspsRxBytesInterval = 0;
                dspsStatsHandler.postDelayed(this, CodelessLibConfig.DSPS_STATS_INTERVAL);
                EventBus.getDefault().post(new CodelessEvent.DspsStats(CodelessManager.this, null, dspsRxSpeed, SPEED_INVALID));
            }
        }
    };

    synchronized private void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        this.gatt = gatt;
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(TAG, logPrefix + "Connected");
            state = CONNECTED;
            EventBus.getDefault().post(new CodelessEvent.Connection(this));
            Log.d(TAG, logPrefix + "Discover services");
            state = SERVICE_DISCOVERY;
            EventBus.getDefault().post(new CodelessEvent.ServiceDiscovery(this, false));
            gatt.discoverServices();
            initialize();
        } else {
            Log.d(TAG, logPrefix + "Disconnected: status=" + status);
            state = DISCONNECTED;
            gatt.close();
            this.gatt = null;
            if (CodelessLibConfig.BLUETOOTH_STATE_MONITOR)
                context.unregisterReceiver(bluetoothStateReceiver);
            reset();
            EventBus.getDefault().post(new CodelessEvent.Connection(this));
        }
    }

    private void initialize() {
        HandlerThread handlerThread = new HandlerThread("CodelessManager");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        if (CodelessLibConfig.DSPS_STATS) {
            handlerThread = new HandlerThread("CodelessDspsStats");
            handlerThread.start();
            dspsStatsHandler = new Handler(handlerThread.getLooper());
        }

        if (CodelessLibConfig.CODELESS_LOG)
            codelessLogFile = new CodelessLogFile(this);
        if (CodelessLibConfig.DSPS_RX_LOG)
            dspsRxLogFile = new DspsRxLogFile(this);
    }

    private void reset() {
        mtu = CodelessProfile.MTU_DEFAULT;

        dspsPending.clear();
        for (DspsPeriodicSend operation : new ArrayList<>(dspsPeriodic))
            operation.stop();
        for (DspsFileSend operation : new ArrayList<>(dspsFiles))
            operation.stop();
        if (dspsFileReceive != null)
            dspsFileReceive.stop();

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler.getLooper().quit();
        }

        if (CodelessLibConfig.DSPS_STATS && dspsStatsHandler != null) {
            dspsStatsHandler.removeCallbacksAndMessages(null);
            dspsStatsHandler.getLooper().quit();
        }

        if (CodelessLibConfig.CODELESS_LOG && codelessLogFile != null)
            codelessLogFile.close();
        if (CodelessLibConfig.DSPS_RX_LOG && dspsRxLogFile != null)
            dspsRxLogFile.close();

        gattOperationPending = null;
        gattQueue.clear();

        commandMode = false;
        binaryRequestPending = false;
        binaryExitRequestPending = false;

        commandQueue.clear();
        commandPending = null;
        commandInbound = null;
        inboundPending = 0;
        outboundResponseLines = 0;
        parsePending.clear();

        dspsRxFlowOn = CodelessLibConfig.DEFAULT_DSPS_RX_FLOW_CONTROL;
        dspsTxFlowOn = CodelessLibConfig.DEFAULT_DSPS_TX_FLOW_CONTROL;

        servicesDiscovered = false;
        codelessSupport = false;
        dspsSupport = false;
        codelessService = null;
        codelessInbound = null;
        codelessOutbound = null;
        codelessFlowControl = null;
        dspsService = null;
        dspsServerTx = null;
        dspsServerRx = null;
        dspsFlowControl = null;
        deviceInfoService = null;
    }

    private void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, logPrefix + "Services discovered: status=" + status);

        BluetoothGattDescriptor codelessFlowControlClientConfig = null;
        BluetoothGattDescriptor dspsServerTxClientConfig = null;
        BluetoothGattDescriptor dspsFlowControlClientConfig = null;
        pendingEnableNotifications = new ArrayList<>();

        deviceInfoService = gatt.getService(Uuid.DEVICE_INFORMATION_SERVICE);

        codelessService = gatt.getService(Uuid.CODELESS_SERVICE_UUID);
        Log.d(TAG, logPrefix + "Codeless service " + (codelessService != null ? "found" : "not found"));
        if (codelessService != null) {
            codelessInbound = codelessService.getCharacteristic(Uuid.CODELESS_INBOUND_COMMAND_UUID);
            if (codelessInbound == null)
                Log.e(TAG, logPrefix + "Missing codeless inbound characteristic " + Uuid.CODELESS_INBOUND_COMMAND_UUID);

            codelessOutbound = codelessService.getCharacteristic(Uuid.CODELESS_OUTBOUND_COMMAND_UUID);
            if (codelessOutbound == null)
                Log.e(TAG, logPrefix + "Missing codeless outbound characteristic " + Uuid.CODELESS_OUTBOUND_COMMAND_UUID);

            codelessFlowControl = codelessService.getCharacteristic(Uuid.CODELESS_FLOW_CONTROL_UUID);
            if (codelessFlowControl == null) {
                Log.e(TAG, logPrefix + "Missing codeless flow control characteristic " + Uuid.CODELESS_FLOW_CONTROL_UUID);
            } else {
                codelessFlowControlClientConfig = codelessFlowControl.getDescriptor(Uuid.CLIENT_CONFIG_DESCRIPTOR);
                if (codelessFlowControlClientConfig == null)
                    Log.e(TAG, logPrefix + "Missing codeless flow control characteristic client configuration");
            }
        }
        codelessSupport = codelessService != null && codelessInbound != null && codelessOutbound != null && codelessFlowControl != null && codelessFlowControlClientConfig != null;
        if (codelessSupport)
            pendingEnableNotifications.add(codelessFlowControl);

        dspsService = gatt.getService(Uuid.DSPS_SERVICE_UUID);
        Log.d(TAG, logPrefix + "DSPS service " + (dspsService != null ? "found" : "not found"));
        if (dspsService != null) {
            dspsServerTx = dspsService.getCharacteristic(Uuid.DSPS_SERVER_TX_UUID);
            if (dspsServerTx == null) {
                Log.e(TAG, logPrefix + "Missing DSPS server TX characteristic " + Uuid.DSPS_SERVER_TX_UUID);
            } else {
                dspsServerTxClientConfig = dspsServerTx.getDescriptor(Uuid.CLIENT_CONFIG_DESCRIPTOR);
                if (dspsServerTxClientConfig == null)
                    Log.e(TAG, logPrefix + "Missing DSPS server TX characteristic client configuration");
            }

            dspsServerRx = dspsService.getCharacteristic(Uuid.DSPS_SERVER_RX_UUID);
            if (dspsServerRx == null)
                Log.e(TAG, logPrefix + "Missing DSPS server RX characteristic " + Uuid.DSPS_SERVER_RX_UUID);

            dspsFlowControl = dspsService.getCharacteristic(Uuid.DSPS_FLOW_CONTROL_UUID);
            if (dspsFlowControl == null) {
                Log.e(TAG, logPrefix + "Missing DSPS flow control characteristic " + Uuid.DSPS_FLOW_CONTROL_UUID);
            } else {
                dspsFlowControlClientConfig = dspsFlowControl.getDescriptor(Uuid.CLIENT_CONFIG_DESCRIPTOR);
                if (dspsFlowControlClientConfig == null)
                    Log.e(TAG, logPrefix + "Missing DSPS flow control characteristic client configuration");
            }
        }
        dspsSupport = dspsService != null && dspsServerTx != null && dspsServerRx != null && dspsFlowControl != null && dspsServerTxClientConfig != null && dspsFlowControlClientConfig != null;
        if (dspsSupport) {
            pendingEnableNotifications.add(dspsServerTx);
            pendingEnableNotifications.add(dspsFlowControl);
        }

        servicesDiscovered = true;
        state = CONNECTED;
        EventBus.getDefault().post(new CodelessEvent.ServiceDiscovery(this, true));

        if (CodelessLibConfig.REQUEST_MTU && (codelessSupport || dspsSupport)) {
            if (CodelessLibConfig.MTU == CodelessProfile.MTU_DEFAULT)
                dspsChunkSize = CodelessProfile.MTU_DEFAULT - 3;
            else if (mtu == CodelessProfile.MTU_DEFAULT)
                requestMtu(CodelessLibConfig.MTU);
        }

        if (!pendingEnableNotifications.isEmpty()) {
            for (BluetoothGattCharacteristic characteristic : pendingEnableNotifications) {
                enableNotifications(characteristic);
            }
        } else {
            pendingEnableNotifications = null;
        }
    }

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        enqueueGattOperation(new GattOperation(characteristic));
    }

    private void executeReadCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "Read characteristic: " + characteristic.getUuid());
        if (!gatt.readCharacteristic(characteristic)) {
            Log.e(TAG, logPrefix + "Error reading characteristic: " + characteristic.getUuid());
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_GATT_OPERATION));
            dequeueGattOperation();
        }
    }

    private void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "onCharacteristicRead: " + status + " " + characteristic.getUuid() + " " + CodelessUtil.hexArrayLog(characteristic.getValue()));
        if (CodelessLibConfig.GATT_DEQUEUE_BEFORE_PROCESSING)
            dequeueGattOperation();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (characteristic.equals(codelessOutbound)) {
                onCodelessInbound(characteristic.getValue());
            } else if (characteristic.getService().equals(deviceInfoService)) {
                onDeviceInfoRead(characteristic);
            } else if (characteristic.getUuid().equals(Uuid.GAP_DEVICE_NAME)) {
                onDeviceNameRead(characteristic);
            }
        } else {
            Log.e(TAG, logPrefix + "Failed to read characteristic: " + characteristic.getUuid());
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_GATT_OPERATION));
        }

        if (!CodelessLibConfig.GATT_DEQUEUE_BEFORE_PROCESSING)
            dequeueGattOperation();
    }

    private void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        enqueueGattOperation(new GattOperation(characteristic, value));
    }

    private void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value, boolean response) {
        enqueueGattOperation(new GattOperation(characteristic, value, response));
    }

    private void executeWriteCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value, boolean response) {
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "Write characteristic" + (!response ? " (no response): " : ": ") + characteristic.getUuid() + " " + CodelessUtil.hexArrayLog(value));
        characteristic.setWriteType(response ? BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT : BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(value);
        if (!gatt.writeCharacteristic(characteristic)) {
            Log.e(TAG, logPrefix + "Error writing characteristic: " + characteristic.getUuid());
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_GATT_OPERATION));
            onCharacteristicWriteError(characteristic);
            dequeueGattOperation();
        }
    }

    private void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "onCharacteristicWrite: " + status + " " + characteristic.getUuid());
        if (CodelessLibConfig.GATT_DEQUEUE_BEFORE_PROCESSING)
            dequeueGattOperation();

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, logPrefix + "Failed to write characteristic: " + characteristic.getUuid());
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_GATT_OPERATION));
            onCharacteristicWriteError(characteristic);
        }

        if (!CodelessLibConfig.GATT_DEQUEUE_BEFORE_PROCESSING)
            dequeueGattOperation();
    }

    private void onCharacteristicWriteError(BluetoothGattCharacteristic characteristic) {
        if (characteristic.equals(codelessInbound)) {
            if (commandPending != null) {
                commandPending.onError(CodelessProfile.GATT_OPERATION_ERROR);
                commandComplete(true);
            } else if (commandInbound != null) {
                commandInbound.setComplete();
                inboundCommandComplete();
            }
        }
    }

    private void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "onCharacteristicChanged: " + characteristic.getUuid() + " " + CodelessUtil.hexArrayLog(characteristic.getValue()));
        if (characteristic.equals(codelessFlowControl)) {
            onCodelessFlowControl(characteristic.getValue());
        } else if (characteristic.equals(dspsServerTx)) {
            onDspsData(characteristic.getValue());
        } else if (characteristic.equals(dspsFlowControl)){
            onDspsFlowControl(characteristic.getValue());
        }
    }

    private void readDescriptor(BluetoothGattDescriptor descriptor) {
        enqueueGattOperation(new GattOperation(descriptor));
    }

    private void executeReadDescriptor(BluetoothGattDescriptor descriptor) {
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "Read descriptor: " + characteristic.getUuid() + " " + descriptor.getUuid());
        if (!gatt.readDescriptor(descriptor)) {
            Log.e(TAG, logPrefix + "Error reading descriptor: " + characteristic.getUuid() + " " + descriptor.getUuid());
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_GATT_OPERATION));
            dequeueGattOperation();
        }
    }

    private void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "onDescriptorRead: " + status + " " + characteristic.getUuid() + " " + descriptor.getUuid() + " " + CodelessUtil.hexArrayLog(descriptor.getValue()));
        if (CodelessLibConfig.GATT_DEQUEUE_BEFORE_PROCESSING)
            dequeueGattOperation();

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, logPrefix + "Failed to read descriptor: " + characteristic.getUuid() + " " + descriptor.getUuid());
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_GATT_OPERATION));
        }

        if (!CodelessLibConfig.GATT_DEQUEUE_BEFORE_PROCESSING)
            dequeueGattOperation();
    }

    private void writeDescriptor(BluetoothGattDescriptor descriptor, byte[] value) {
        enqueueGattOperation(new GattOperation(descriptor, value));
    }

    private void executeWriteDescriptor(BluetoothGattDescriptor descriptor, byte[] value) {
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "Write descriptor: " + characteristic.getUuid() + " " + descriptor.getUuid() + " " + CodelessUtil.hexArrayLog(value));
        descriptor.setValue(value);
        if (!gatt.writeDescriptor(descriptor)) {
            Log.e(TAG, logPrefix + "Error writing descriptor: " + characteristic.getUuid() + " " + descriptor.getUuid());
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_GATT_OPERATION));
            if (pendingEnableNotifications.contains(descriptor.getCharacteristic()))
                EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_INIT_SERVICES));
            dequeueGattOperation();
        }
    }

    private void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        if (CodelessLibLog.GATT_OPERATION)
            Log.d(TAG, logPrefix + "onDescriptorWrite: " + status + " " + characteristic.getUuid() + " " + descriptor.getUuid());
        if (CodelessLibConfig.GATT_DEQUEUE_BEFORE_PROCESSING)
            dequeueGattOperation();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (pendingEnableNotifications != null) {
                pendingEnableNotifications.remove(descriptor.getCharacteristic());
                if (pendingEnableNotifications.isEmpty()) {
                    pendingEnableNotifications = null;
                    onDeviceReady();
                }
            }
        } else {
            Log.e(TAG, logPrefix + "Failed to write descriptor:" + characteristic.getUuid() + " " + descriptor.getUuid());
            EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_GATT_OPERATION));
            if (pendingEnableNotifications.contains(descriptor.getCharacteristic()))
                EventBus.getDefault().post(new CodelessEvent.Error(this, ERROR_INIT_SERVICES));
        }

        if (!CodelessLibConfig.GATT_DEQUEUE_BEFORE_PROCESSING)
            dequeueGattOperation();
    }

    private void enableNotifications(BluetoothGattCharacteristic characteristic) {
        BluetoothGattDescriptor ccc = characteristic.getDescriptor(Uuid.CLIENT_CONFIG_DESCRIPTOR);
        if (ccc == null) {
            Log.e(TAG, logPrefix + "Missing client configuration descriptor: " + characteristic.getUuid());
            return;
        }
        boolean notify = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
        byte[] value = notify ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        Log.d(TAG, logPrefix + "Enable " + (notify ? "notifications" : "indications") + ": " + characteristic.getUuid());
        gatt.setCharacteristicNotification(characteristic, true);
        writeDescriptor(ccc, value);
    }

    private void executeMtuRequest(int mtu) {
        if (Build.VERSION.SDK_INT >= 21) {
            Log.d(TAG, logPrefix + "MTU request: " + mtu);
            if (!gatt.requestMtu(mtu)) {
                Log.e(TAG, logPrefix + "MTU request error");
                dequeueGattOperation();
            }
        }
    }

    private void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        Log.d(TAG, logPrefix + "onMtuChanged: " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, logPrefix + "MTU changed to " + mtu);
            this.mtu = mtu;
            if (CodelessLibConfig.DSPS_CHUNK_SIZE_INCREASE_TO_MTU || dspsChunkSize > mtu - 3)
                dspsChunkSize = mtu - 3;
        } else {
            Log.e(TAG, logPrefix + "Failed to change MTU");
        }
        if (gattOperationPending.getType() == GattOperation.Type.MtuRequest)
            dequeueGattOperation();
    }

    private void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (CodelessLibLog.GATT_OPERATION)
                Log.d(TAG, logPrefix + "RSSI: " + rssi);
            EventBus.getDefault().post(new CodelessEvent.Rssi(this, rssi));
        } else {
            Log.e(TAG, logPrefix + "Failed to read RSSI");
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            CodelessManager.this.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            CodelessManager.this.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            CodelessManager.this.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            CodelessManager.this.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            CodelessManager.this.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            CodelessManager.this.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            CodelessManager.this.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            CodelessManager.this.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            CodelessManager.this.onMtuChanged(gatt, mtu, status);
        }
    };

    private BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                int prev = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
                Log.d(TAG, "Bluetooth state changed: " + prev + " -> " + state);
                if (state == BluetoothAdapter.STATE_TURNING_OFF)
                    disconnect();
            }
        }
    };

    private static class GattOperation {

        public enum Type {
            ReadCharacteristic,
            WriteCharacteristic,
            WriteCommand,
            ReadDescriptor,
            WriteDescriptor,
            MtuRequest
        }

        private Type type;
        private Object gattObject;
        private byte[] value;

        public GattOperation(Object gattObject) {
            this.gattObject = gattObject;
            type = gattObject instanceof BluetoothGattCharacteristic ? Type.ReadCharacteristic : Type.ReadDescriptor;
        }

        public GattOperation(Object gattObject, byte[] value) {
            this.gattObject = gattObject;
            type = gattObject instanceof BluetoothGattCharacteristic ? Type.WriteCharacteristic : Type.WriteDescriptor;
            this.value = value.clone();
        }

        public GattOperation(BluetoothGattCharacteristic gattObject, byte[] value, boolean response) {
            this.gattObject = gattObject;
            type = response ? Type.WriteCharacteristic : Type.WriteCommand;
            this.value = value.clone();
        }

        public GattOperation(int mtu) {
            type = Type.MtuRequest;
            value = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short)mtu).array();
        }

        public Type getType() {
            return type;
        }

        public Object getGattObject() {
            return gattObject;
        }

        public BluetoothGattCharacteristic getCharacteristic() {
            return (BluetoothGattCharacteristic) gattObject;
        }

        public BluetoothGattDescriptor getDescriptor() {
            return (BluetoothGattDescriptor) gattObject;
        }

        public byte[] getValue() {
            return value;
        }

        public boolean lowPriority() {
            return false;
        }

        protected void onExecute() {
        }
    }

    public boolean isGattOperationPending() {
        return gattOperationPending != null;
    }

    public GattOperation getGattOperationPending() {
        return gattOperationPending;
    }

    synchronized private void enqueueGattOperation(GattOperation operation) {
        if (gatt == null)
            return;
        if (gattOperationPending != null) {
            if (!CodelessLibConfig.GATT_QUEUE_PRIORITY)
                gattQueue.add(operation);
            else
                enqueueGattOperationWithPriority(operation);
        } else {
            executeGattOperation(operation);
        }
    }

    synchronized private void enqueueGattOperations(List<GattOperation> operations) {
        if (gatt == null || operations.isEmpty())
            return;
        if (!CodelessLibConfig.GATT_QUEUE_PRIORITY)
            gattQueue.addAll(operations);
        else
            enqueueGattOperationsWithPriority(operations);
        if (gattOperationPending == null) {
            dequeueGattOperation();
        }
    }

    private void enqueueGattOperationWithPriority(GattOperation operation) {
        if (gattQueue.isEmpty() || !gattQueue.peekLast().lowPriority() || operation.lowPriority()) {
            gattQueue.add(operation);
        } else if (gattQueue.peekFirst().lowPriority()) {
            gattQueue.addFirst(operation);
        } else {
            ListIterator<GattOperation> i = gattQueue.listIterator();
            while(i.hasNext()) {
                if (i.next().lowPriority()) {
                    i.previous();
                    i.add(operation);
                    break;
                }
            }
        }
    }

    private void enqueueGattOperationsWithPriority(List<GattOperation> operations) {
        if (gattQueue.isEmpty() || !gattQueue.peekLast().lowPriority() || operations.get(0).lowPriority()) {
            gattQueue.addAll(operations);
        } else {
            ListIterator<GattOperation> i = gattQueue.listIterator();
            while(i.hasNext()) {
                if (i.next().lowPriority()) {
                    i.previous();
                    for (GattOperation operation : operations) {
                        i.add(operation);
                    }
                    break;
                }
            }
        }
    }

    synchronized private void dequeueGattOperation() {
        gattOperationPending = null;
        if (gattQueue.isEmpty())
            return;
        executeGattOperation(gattQueue.poll());
    }

    private void executeGattOperation(GattOperation operation) {
        gattOperationPending = operation;
        operation.onExecute();
        switch (operation.getType()) {
            case ReadCharacteristic:
                executeReadCharacteristic(operation.getCharacteristic());
                break;
            case WriteCharacteristic:
            case WriteCommand:
                executeWriteCharacteristic(operation.getCharacteristic(), operation.getValue(), operation.getType() == GattOperation.Type.WriteCharacteristic);
                break;
            case ReadDescriptor:
                executeReadDescriptor(operation.getDescriptor());
                break;
            case WriteDescriptor:
                executeWriteDescriptor(operation.getDescriptor(), operation.getValue());
                break;
            case MtuRequest:
                executeMtuRequest(ByteBuffer.wrap(operation.getValue()).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xffff);
                break;
        }
    }

    private class DspsGattOperation extends GattOperation {

        public DspsGattOperation(byte[] data) {
            super(dspsServerRx, data, false);
        }
    }

    private class DspsChunkOperation extends DspsGattOperation {

        public DspsChunkOperation(byte[] data) {
            super(data);
        }

        @Override
        protected void onExecute() {
            if (CodelessLibLog.DSPS_CHUNK)
                Log.d(TAG, logPrefix + "Send DSPS chunk: " + CodelessUtil.hexArrayLog(getValue()));
        }
    }

    private class DspsPeriodicChunkOperation extends DspsGattOperation {

        private DspsPeriodicSend operation;
        private int count;
        private int chunk;
        private int totalChunks;

        public DspsPeriodicChunkOperation(DspsPeriodicSend operation, int count, byte[] data, int chunk, int totalChunks) {
            super(data);
            this.operation = operation;
            this.count = count;
            this.chunk = chunk;
            this.totalChunks = totalChunks;
        }

        public DspsPeriodicSend getOperation() {
            return operation;
        }

        public int getCount() {
            return count;
        }

        public int getChunk() {
            return chunk;
        }

        public int getTotalChunks() {
            return totalChunks;
        }

        @Override
        public boolean lowPriority() {
            return true;
        }

        @Override
        protected void onExecute() {
            if (CodelessLibLog.DSPS_PERIODIC_CHUNK)
                Log.d(TAG, logPrefix + "Send periodic DSPS chunk: count " + count + " (" + chunk + " of " + totalChunks + ") " + CodelessUtil.hexArrayLog(getValue()));
            if (CodelessLibConfig.DSPS_STATS)
                operation.updateBytesSent(getValue().length);
            if (operation.isPattern()) {
                operation.setPatternSentCount((count - 1) % operation.getPatternMaxCount());
                EventBus.getDefault().post(new CodelessEvent.DspsPatternChunk(CodelessManager.this, operation, operation.getPatternSentCount()));
            }
        }
    }

    private class DspsFileChunkOperation extends DspsGattOperation {

        private DspsFileSend operation;
        private int chunk;

        public DspsFileChunkOperation(DspsFileSend operation, byte[] data, int chunk) {
            super(data);
            this.operation = operation;
            this.chunk = chunk;
        }

        public DspsFileSend getOperation() {
            return operation;
        }

        public int getChunk() {
            return chunk;
        }

        @Override
        public boolean lowPriority() {
            return true;
        }

        @Override
        protected void onExecute() {
            if (CodelessLibLog.DSPS_FILE_CHUNK)
                Log.d(TAG, logPrefix + "Send file chunk: " + operation + " (" + chunk + " of " + operation.getTotalChunks() + ") " + CodelessUtil.hexArrayLog(getValue()));
            operation.setSentChunks(chunk);
            if (CodelessLibConfig.DSPS_STATS)
                operation.updateBytesSent(getValue().length);
            if (chunk == operation.getTotalChunks()) {
                if (CodelessLibLog.DSPS)
                    Log.d(TAG, logPrefix + "File sent: " + operation);
                operation.setComplete();
                dspsFiles.remove(operation);
            }
            EventBus.getDefault().post(new CodelessEvent.DspsFileChunk(CodelessManager.this, operation, chunk));
        }
    }

    synchronized private void removePendingDspsChunkOperations(boolean keep) {
        Iterator<GattOperation> i = gattQueue.iterator();
        while (i.hasNext()) {
            GattOperation gattOperation = i.next();
            if (gattOperation instanceof DspsChunkOperation) {
                if (keep)
                    dspsPending.add(gattOperation);
                i.remove();
            }
        }
    }

    synchronized private int removePendingDspsPeriodicChunkOperations(DspsPeriodicSend operation) {
        int count = -1;
        Iterator<GattOperation> i = gattQueue.iterator();
        while (i.hasNext()) {
            GattOperation gattOperation = i.next();
            if (gattOperation instanceof DspsPeriodicChunkOperation && ((DspsPeriodicChunkOperation)gattOperation).getOperation() == operation) {
                DspsPeriodicChunkOperation periodicChunkOperation = (DspsPeriodicChunkOperation) gattOperation;
                if (count == -1 && periodicChunkOperation.getChunk() == 1)
                    count = periodicChunkOperation.getCount();
                i.remove();
            }
        }
        return count;
    }

    synchronized private int removePendingDspsFileChunkOperations(DspsFileSend operation) {
        int chunk = -1;
        Iterator<GattOperation> i = gattQueue.iterator();
        while (i.hasNext()) {
            GattOperation gattOperation = i.next();
            if (gattOperation instanceof DspsFileChunkOperation && ((DspsFileChunkOperation)gattOperation).getOperation() == operation) {
                if (chunk == -1)
                    chunk = ((DspsFileChunkOperation)gattOperation).getChunk() - 1;
                i.remove();
            }
        }
        return chunk;
    }
}
