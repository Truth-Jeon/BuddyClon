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

import android.bluetooth.BluetoothDevice;
import android.net.Uri;

import com.diasemi.codelesslib.command.AdcReadCommand;
import com.diasemi.codelesslib.command.AdvertisingDataCommand;
import com.diasemi.codelesslib.command.AdvertisingResponseCommand;
import com.diasemi.codelesslib.command.BasicCommand;
import com.diasemi.codelesslib.command.BatteryLevelCommand;
import com.diasemi.codelesslib.command.BaudRateCommand;
import com.diasemi.codelesslib.command.BinEscCommand;
import com.diasemi.codelesslib.command.BluetoothAddressCommand;
import com.diasemi.codelesslib.command.BondingEntryClearCommand;
import com.diasemi.codelesslib.command.BondingEntryStatusCommand;
import com.diasemi.codelesslib.command.BondingEntryTransferCommand;
import com.diasemi.codelesslib.command.CmdGetCommand;
import com.diasemi.codelesslib.command.CodelessCommand;
import com.diasemi.codelesslib.command.ConnectionParametersCommand;
import com.diasemi.codelesslib.command.DataLengthEnableCommand;
import com.diasemi.codelesslib.command.DeviceInformationCommand;
import com.diasemi.codelesslib.command.EventConfigCommand;
import com.diasemi.codelesslib.command.EventHandlerCommand;
import com.diasemi.codelesslib.command.FlowControlCommand;
import com.diasemi.codelesslib.command.GapConnectCommand;
import com.diasemi.codelesslib.command.GapDisconnectCommand;
import com.diasemi.codelesslib.command.GapScanCommand;
import com.diasemi.codelesslib.command.GapStatusCommand;
import com.diasemi.codelesslib.command.HeartbeatCommand;
import com.diasemi.codelesslib.command.HostSleepCommand;
import com.diasemi.codelesslib.command.I2cConfigCommand;
import com.diasemi.codelesslib.command.I2cReadCommand;
import com.diasemi.codelesslib.command.I2cScanCommand;
import com.diasemi.codelesslib.command.IoConfigCommand;
import com.diasemi.codelesslib.command.IoStatusCommand;
import com.diasemi.codelesslib.command.MaxMtuCommand;
import com.diasemi.codelesslib.command.MemStoreCommand;
import com.diasemi.codelesslib.command.PinCodeCommand;
import com.diasemi.codelesslib.command.PowerLevelConfigCommand;
import com.diasemi.codelesslib.command.PulseGenerationCommand;
import com.diasemi.codelesslib.command.RandomNumberCommand;
import com.diasemi.codelesslib.command.RssiCommand;
import com.diasemi.codelesslib.command.SecurityModeCommand;
import com.diasemi.codelesslib.command.SpiConfigCommand;
import com.diasemi.codelesslib.command.SpiReadCommand;
import com.diasemi.codelesslib.command.SpiTransferCommand;
import com.diasemi.codelesslib.command.UartEchoCommand;
import com.diasemi.codelesslib.command.UartPrintCommand;
import com.diasemi.codelesslib.dsps.DspsFileReceive;
import com.diasemi.codelesslib.dsps.DspsFileSend;
import com.diasemi.codelesslib.dsps.DspsPeriodicSend;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class CodelessEvent {

    private static class ScanEvent {
        public CodelessScanner scanner;

        public ScanEvent(CodelessScanner scanner) {
            this.scanner = scanner;
        }
    }

    public static class ScanStart extends ScanEvent {
        public ScanStart(CodelessScanner scanner) {
            super(scanner);
        }
    }

    public static class ScanStop extends ScanEvent {
        public ScanStop(CodelessScanner scanner) {
            super(scanner);
        }
    }

    public static class ScanRestart extends ScanEvent {
        public ScanRestart(CodelessScanner scanner) {
            super(scanner);
        }
    }

    public static class ScanResult extends ScanEvent {
        public BluetoothDevice device;
        public CodelessScanner.AdvData advData;
        public int rssi;

        public ScanResult(CodelessScanner scanner, BluetoothDevice device, CodelessScanner.AdvData advData, int rssi) {
            super(scanner);
            this.device = device;
            this.advData = advData;
            this.rssi = rssi;
        }
    }

    private static class Event {
        public CodelessManager manager;

        public Event(CodelessManager manager) {
            this.manager = manager;
        }
    }

    public static class Connection extends Event {
        public Connection(CodelessManager manager) {
            super(manager);
        }
    }

    public static class ServiceDiscovery extends Event {
        public boolean complete;

        public ServiceDiscovery(CodelessManager manager, boolean complete) {
            super(manager);
            this.complete = complete;
        }
    }

    public static class Ready extends Event {
        public Ready(CodelessManager manager) {
            super(manager);
        }
    }

    // Error
    public static final int ERROR_NOT_READY = 0;
    public static final int ERROR_INIT_SERVICES = 1;
    public static final int ERROR_GATT_OPERATION = 2;
    public static final int ERROR_OPERATION_NOT_ALLOWED = 3;
    public static final int ERROR_INVALID_PREFIX = 4;
    public static final int ERROR_INVALID_COMMAND = 5;

    public static class Error extends Event {
        public int error;

        public Error(CodelessManager manager, int error) {
            super(manager);
            this.error = error;
        }
    }

    public static class DeviceName extends Event {
        public String name;

        public DeviceName(CodelessManager manager, String name) {
            super(manager);
            this.name = name;
        }
    }

    public static class DeviceInfo extends Event {
        public UUID uuid;
        public byte[] value;
        public String info;

        public DeviceInfo(CodelessManager manager, UUID uuid, byte[] value, String info) {
            super(manager);
            this.uuid = uuid;
            this.value = value;
            this.info = info;
        }
    }

    public static class Rssi extends Event {
        public int rssi;

        public Rssi(CodelessManager manager, int rssi) {
            super(manager);
            this.rssi = rssi;
        }
    }

    public static class BinaryModeRequest extends Event {
        public BinaryModeRequest(CodelessManager manager) {
            super(manager);
        }
    }

    public static class Mode extends Event {
        public boolean command;

        public Mode(CodelessManager manager, boolean command) {
            super(manager);
            this.command = command;
        }
    }

    public static class CodelessLine extends Event {
        public CodelessProfile.Line line;

        public CodelessLine(CodelessManager manager, CodelessProfile.Line line) {
            super(manager);
            this.line = line;
        }
    }

    public static class ScriptStartEvent extends Event {
        public CodelessScript script;

        public ScriptStartEvent(CodelessScript script) {
            super(script.getManager());
            this.script = script;
        }
    }

    public static class ScriptEndEvent extends Event {
        public CodelessScript script;
        public boolean error;

        public ScriptEndEvent(CodelessScript script, boolean error) {
            super(script.getManager());
            this.script = script;
            this.error = error;
        }
    }

    public static class ScriptCommandEvent extends Event {
        public CodelessScript script;
        public CodelessCommand command;

        public ScriptCommandEvent(CodelessScript script, CodelessCommand command) {
            super(script.getManager());
            this.script = script;
            this.command = command;
        }
    }

    private static class CommandEvent extends Event {
        public CodelessCommand command;

        public CommandEvent(CodelessCommand command) {
            super(command.getManager());
            this.command = command;
        }
    }

    public static class CommandSuccess extends CommandEvent {
        public CommandSuccess(CodelessCommand command) {
            super(command);
        }
    }

    public static class CommandError extends CommandEvent {
        public String msg;

        public CommandError(CodelessCommand command, String msg) {
            super(command);
            this.msg = msg;
        }
    }

    public static class Ping extends CommandEvent {
        public Ping(BasicCommand command) {
            super(command);
        }
    }

    public static class DeviceInformation extends CommandEvent {
        public String info;

        public DeviceInformation(DeviceInformationCommand command) {
            super(command);
            info = command.getInfo();
        }
    }

    public static class UartEcho extends CommandEvent {
        public boolean echo;

        public UartEcho(UartEchoCommand command) {
            super(command);
            echo = command.echo();
        }
    }

    public static class BinEsc extends CommandEvent {
        public int sequence;
        public int timePrior;
        public int timeAfter;

        public BinEsc(BinEscCommand command) {
            super(command);
            this.sequence = command.getSequence();
            this.timePrior = command.getTimePrior();
            this.timeAfter = command.getTimeAfter();
        }
    }

    public static class RandomNumber extends CommandEvent {
        public long number;

        public RandomNumber(RandomNumberCommand command) {
            super(command);
            number = command.getNumber();
        }
    }

    public static class BatteryLevel extends CommandEvent {
        public int level;

        public BatteryLevel(BatteryLevelCommand command) {
            super(command);
            level = command.getLevel();
        }
    }

    public static class BluetoothAddress extends CommandEvent {
        public String address;
        public boolean random;

        public BluetoothAddress(BluetoothAddressCommand command) {
            super(command);
            address = command.getAddress();
            random = command.isRandom();
        }
    }

    public static class PeerRssi extends CommandEvent {
        public int rssi;

        public PeerRssi(RssiCommand command) {
            super(command);
            rssi = command.getRssi();
        }
    }

    public static class IoConfig extends CommandEvent {
        public ArrayList<CodelessProfile.GPIO> configuration;

        public IoConfig(IoConfigCommand command) {
            super(command);
            configuration = command.getConfiguration();
        }
    }

    public static class IoConfigSet extends CommandEvent {
        public CodelessProfile.GPIO gpio;

        public IoConfigSet(IoConfigCommand command) {
            super(command);
            gpio = command.getGpio();
        }
    }

    public static class IoStatus extends CommandEvent {
        public CodelessProfile.GPIO gpio;
        public boolean status;

        public IoStatus(IoStatusCommand command) {
            super(command);
            gpio = command.getGpio();
            status = command.getStatus();
        }
    }

    public static class AnalogRead extends CommandEvent {
        public CodelessProfile.GPIO gpio;
        public int state;

        public AnalogRead(AdcReadCommand command) {
            super(command);
            gpio = command.getGpio();
            state = command.getState();
        }
    }

    public static class PwmStatus extends CommandEvent {
        public int frequency;
        public int dutyCycle;
        public int duration;

        public PwmStatus(PulseGenerationCommand command) {
            super(command);
            frequency = command.getFrequency();
            dutyCycle = command.getDutyCycle();
            duration = command.getDuration();
        }
    }

    public static class PwmStart extends CommandEvent {
        public int frequency;
        public int dutyCycle;
        public int duration;

        public PwmStart(PulseGenerationCommand command) {
            super(command);
            frequency = command.getFrequency();
            dutyCycle = command.getDutyCycle();
            duration = command.getDuration();
        }
    }

    public static class I2cConfig extends CommandEvent {
        public int addressSize;
        public int bitrate;
        public int registerSize;

        public I2cConfig(I2cConfigCommand command) {
            super(command);
            addressSize = command.getBitCount();
            bitrate = command.getBitRate();
            registerSize = command.getRegisterWidth();
        }
    }

    public static class I2cScan extends CommandEvent {
        public ArrayList<I2cScanCommand.I2cDevice> devices;

        public I2cScan(I2cScanCommand command) {
            super(command);
            devices = command.getDevices();
        }
    }

    public static class I2cRead extends CommandEvent {
        public int[] data;

        public I2cRead(I2cReadCommand command) {
            super(command);
            data = command.getData();
        }
    }

    public static class MemoryTextContent extends CommandEvent {
        public int index;
        public String text;

        public MemoryTextContent(MemStoreCommand command) {
            super(command);
            index = command.getMemIndex();
            text = command.getText();
        }
    }

    public static class PinCode extends CommandEvent {
        public int pinCode;

        public PinCode(PinCodeCommand command) {
            super(command);
            pinCode = command.getPinCode();
        }
    }

    public static class StoredCommands extends CommandEvent {
        public int index;
        public ArrayList<CodelessCommand> commands;

        public StoredCommands(CmdGetCommand command) {
            super(command);
            index = command.getIndex();
            commands = command.getCommands();
        }
    }

    public static class AdvertisingData extends CommandEvent {
        public byte[] data;

        public AdvertisingData(AdvertisingDataCommand command) {
            super(command);
            data = command.getData();
        }
    }

    public static class ScanResponseData extends CommandEvent {
        public byte[] data;

        public ScanResponseData(AdvertisingResponseCommand command) {
            super(command);
            data = command.getData();
        }
    }

    public static class GapStatus extends CommandEvent {
        public int gapRole;
        public boolean connected;

        public GapStatus(GapStatusCommand command) {
            super(command);
            gapRole = command.getGapRole();
            connected = command.connected();
        }
    }

    public static class GapScanResult extends CommandEvent {
        public ArrayList<CodelessProfile.GapScannedDevice> devices;

        public GapScanResult(GapScanCommand command) {
            super(command);
            devices = command.getDevices();
        }
    }

    public static class DeviceConnected extends CommandEvent {
        public String deviceAddress;

        public DeviceConnected(GapConnectCommand command) {
            super(command);
            deviceAddress = command.getAddress();
        }
    }

    public static class DeviceDisconnected extends CommandEvent {
        public DeviceDisconnected(GapDisconnectCommand command) {
            super(command);
        }
    }

    public static class ConnectionParameters extends CommandEvent {
        public int interval;
        public int latency;
        public int timeout;
        public int action;

        public ConnectionParameters(ConnectionParametersCommand command) {
            super(command);
            interval = command.getInterval();
            latency = command.getLatency();
            timeout = command.getTimeout();
            action = command.getAction();
        }
    }

    public static class MaxMtu extends CommandEvent {
        public int mtu;

        public MaxMtu(MaxMtuCommand command) {
            super(command);
            mtu = command.getMtu();
        }
    }

    public static class FlowControl extends CommandEvent {
        public boolean enabled;
        public CodelessProfile.GPIO rtsGpio;
        public CodelessProfile.GPIO ctsGpio;

        public FlowControl(FlowControlCommand command) {
            super(command);
            enabled = command.isEnabled();
            rtsGpio = command.getRtsGpio();
            ctsGpio = command.getCtsGpio();
        }
    }

    public static class HostSleep extends CommandEvent {
        public int hostSleepMode;
        public int wakeupByte;
        public int wakeupRetryInterval;
        public int wakeupRetryTimes;

        public HostSleep(HostSleepCommand command) {
            super(command);
            hostSleepMode = command.getHostSleepMode();
            wakeupByte = command.getWakeupByte();
            wakeupRetryInterval = command.getWakeupRetryInterval();
            wakeupRetryTimes = command.getWakeupRetryTimes();
        }
    }

    public static class SpiConfig extends CommandEvent {
        public int speed;
        public int mode;
        public int size;

        public SpiConfig(SpiConfigCommand command) {
            super(command);
            speed = command.getSpeed();
            mode = command.getMode();
            size = command.getSize();
        }
    }

    public static class SpiRead extends CommandEvent {
        public int[] data;

        public SpiRead(SpiReadCommand command) {
            super(command);
            data = command.getData();
        }
    }

    public static class SpiTransfer extends CommandEvent {
        public int[] data;

        public SpiTransfer(SpiTransferCommand command) {
            super(command);
            data = command.getData();
        }
    }

    public static class BaudRate extends CommandEvent {
        public int baudRate;

        public BaudRate(BaudRateCommand command) {
            super(command);
            baudRate = command.getBaudRate();
        }
    }

    public static class DataLengthEnable extends CommandEvent {
        public boolean enabled;
        public int txPacketLength;
        public int rxPacketLength;

        public DataLengthEnable(DataLengthEnableCommand command) {
            super(command);
            enabled = command.enabled();
            txPacketLength = command.getTxPacketLength();
            rxPacketLength = command.getRxPacketLength();
        }
    }

    public static class EventStatus extends CommandEvent {
        public CodelessProfile.EventConfig eventConfig;

        public EventStatus(EventConfigCommand command) {
            super(command);
            eventConfig = command.getEventConfig();
        }
    }

    public static class EventStatusTable extends CommandEvent {
        public ArrayList<CodelessProfile.EventConfig> eventStatusTable;

        public EventStatusTable(EventConfigCommand command) {
            super(command);
            eventStatusTable = command.getEventStatusTable();
        }
    }

    public static class BondingEntryClear extends CommandEvent {
        public int index;

        public BondingEntryClear(BondingEntryClearCommand command) {
            super(command);
            index = command.getIndex();
        }
    }

    public static class BondingEntryPersistenceStatusSet extends CommandEvent {
        public int index;
        public boolean persistent;

        public BondingEntryPersistenceStatusSet(BondingEntryStatusCommand command) {
            super(command);
            index = command.getIndex();
            persistent = command.persistent();
        }
    }

    public static class BondingEntryPersistenceTableStatus extends CommandEvent {
        public ArrayList<Boolean> persistenceStatusTable;

        public BondingEntryPersistenceTableStatus(BondingEntryStatusCommand command) {
            super(command);
            persistenceStatusTable = command.getTablePersistenceStatus();
        }
    }

    public static class BondingEntryEvent extends CommandEvent {
        public int index;
        public CodelessProfile.BondingEntry entry;

        public BondingEntryEvent(BondingEntryTransferCommand command) {
            super(command);
            index = command.getIndex();
            entry = command.getBondingEntry();
        }
    }

    public static class InboundCommand extends CommandEvent {
        public InboundCommand(CodelessCommand command) {
            super(command);
        }
    }

    public static class HostCommand extends CommandEvent {
        public HostCommand(CodelessCommand command) {
            super(command);
        }
    }

    public static class Print extends CommandEvent {
        public String text;

        public Print(UartPrintCommand command) {
            super(command);
            this.text = command.getText();
        }
    }

    public static class EventCommands extends CommandEvent {
        public CodelessProfile.EventHandler eventHandler;

        public EventCommands(EventHandlerCommand command) {
            super(command);
            eventHandler = command.getEventHandler();
        }
    }

    public static class EventCommandsTable extends CommandEvent {
        public ArrayList<CodelessProfile.EventHandler> eventHandlerTable;

        public EventCommandsTable(EventHandlerCommand command) {
            super(command);
            eventHandlerTable = command.getEventHandlerTable();
        }
    }

    public static class SecurityMode extends CommandEvent {
        public int mode;

        public SecurityMode(SecurityModeCommand command) {
            super(command);
            mode = command.getMode();
        }
    }

    public static class Heartbeat extends CommandEvent {
        public boolean enabled;

        public Heartbeat(HeartbeatCommand command) {
            super(command);
            enabled = command.enabled();
        }
    }

    public static class PowerLevel extends CommandEvent {
        public int powerLevel;
        public boolean notSupported;

        public PowerLevel(PowerLevelConfigCommand command) {
            super(command);
            powerLevel = command.getPowerLevel();
            notSupported = command.notSupported();
        }
    }

    public static class DspsRxData extends Event {
        public byte[] data;

        public DspsRxData(CodelessManager manager, byte[] data) {
            super(manager);
            this.data = data;
        }
    }

    public static class DspsRxFlowControl extends Event {
        public boolean flowOn;

        public DspsRxFlowControl(CodelessManager manager, boolean flowOn) {
            super(manager);
            this.flowOn = flowOn;
        }
    }

    public static class DspsTxFlowControl extends Event {
        public boolean flowOn;

        public DspsTxFlowControl(CodelessManager manager, boolean flowOn) {
            super(manager);
            this.flowOn = flowOn;
        }
    }

    public static class DspsFileChunk extends Event {
        public DspsFileSend operation;
        public int chunk;

        public DspsFileChunk(CodelessManager manager, DspsFileSend operation, int chunk) {
            super(manager);
            this.operation = operation;
            this.chunk = chunk;
        }
    }

    public static class DspsFileError extends Event {
        public DspsFileSend operation;

        public DspsFileError(CodelessManager manager, DspsFileSend operation) {
            super(manager);
            this.operation = operation;
        }
    }

    public static class DspsRxFileData extends Event {
        public DspsFileReceive operation;
        public int size;
        public int bytesReceived;

        public DspsRxFileData(CodelessManager manager, DspsFileReceive operation, int size, int bytesReceived) {
            super(manager);
            this.operation = operation;
            this.size = size;
            this.bytesReceived = bytesReceived;
        }
    }

    public static class DspsRxFileCrc extends Event {
        public DspsFileReceive operation;
        public boolean ok;

        public DspsRxFileCrc(CodelessManager manager, DspsFileReceive operation, boolean ok) {
            super(manager);
            this.operation = operation;
            this.ok = ok;
        }
    }

    public static class DspsPatternChunk extends Event {
        public DspsPeriodicSend operation;
        public int count;

        public DspsPatternChunk(CodelessManager manager, DspsPeriodicSend operation, int count) {
            super(manager);
            this.operation = operation;
            this.count = count;
        }
    }

    public static class DspsPatternFileError extends Event {
        public DspsPeriodicSend operation;
        public File file;
        public Uri uri;

        public DspsPatternFileError(CodelessManager manager, DspsPeriodicSend operation, File file, Uri uri) {
            super(manager);
            this.operation = operation;
            this.file = file;
            this.uri = uri;
        }
    }

    public static class DspsStats extends Event {
        public Object operation;
        public int currentSpeed;
        public int averageSpeed;

        public DspsStats(CodelessManager manager, Object operation, int currentSpeed, int averageSpeed) {
            super(manager);
            this.operation = operation;
            this.currentSpeed = currentSpeed;
            this.averageSpeed = averageSpeed;
        }
    }
}
