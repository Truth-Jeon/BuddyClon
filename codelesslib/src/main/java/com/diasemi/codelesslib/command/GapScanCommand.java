/*
 *******************************************************************************
 *
 * Copyright (C) 2020 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.diasemi.codelesslib.command;

import android.util.Log;

import com.diasemi.codelesslib.CodelessEvent;
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.Command;
import com.diasemi.codelesslib.CodelessProfile.CommandID;
import com.diasemi.codelesslib.CodelessProfile.GapScannedDevice;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GapScanCommand extends CodelessCommand {
    public static final String TAG = "GapScanCommand";

    public static final String COMMAND = "GAPSCAN";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.GAPSCAN;

    public static final String RESPONSE_PATTERN_STRING = "^\\( \\) ((?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}),([PR]), Type: (\\bADV\\b|\\bRSP\\b), RSSI:(-?\\d+)$"; // <address> <type> <typeScan> <rssi>
    public static final Pattern RESPONSE_PATTERN = Pattern.compile(RESPONSE_PATTERN_STRING);

    public static final String PATTERN_STRING = "^GAPSCAN$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private ArrayList<GapScannedDevice> devices = new ArrayList<>();

    public GapScanCommand(CodelessManager manager) {
        super(manager);
    }

    public GapScanCommand(CodelessManager manager, String command, boolean parse) {
        super(manager, command, parse);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public String getID() {
        return COMMAND;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CommandID getCommandID() {
        return ID;
    }

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        GapScannedDevice device = new GapScannedDevice();
        if (!response.contains("Scanning") && !response.contains("Scan Completed") && !RESPONSE_PATTERN.matcher(response).matches()) {
            invalid = true;
        } else if (!response.contains("Scanning") && !response.contains("Scan Completed")) {
            try {
                Matcher matcher = RESPONSE_PATTERN.matcher(response);
                device.address = matcher.group(1);
                device.addressType = matcher.group(2).equals(Command.GAP_ADDRESS_TYPE_PUBLIC_STRING) ? Command.GAP_ADDRESS_TYPE_PUBLIC : Command.GAP_ADDRESS_TYPE_RANDOM;
                device.type = matcher.group(3).equals(Command.GAP_SCAN_TYPE_ADV_STRING) ? Command.GAP_SCAN_TYPE_ADV : Command.GAP_SCAN_TYPE_RSP;
                device.rssi = Integer.parseInt(matcher.group(4));
                devices.add(device);
            } catch (NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
                invalid = true;
            }
        }
        if (invalid) {
            Log.i(TAG, "Received invalid scan response: " + response);
        } else if (CodelessLibLog.COMMAND)
            Log.i(TAG, "Scanned device: Address:" + device.address + " Address type:" + (device.addressType == Command.GAP_ADDRESS_TYPE_PUBLIC ? Command.GAP_ADDRESS_TYPE_PUBLIC_STRING : Command.GAP_ADDRESS_TYPE_RANDOM_STRING) + " Type:" + (device.type == Command.GAP_SCAN_TYPE_ADV ? Command.GAP_SCAN_TYPE_ADV_STRING : Command.GAP_SCAN_TYPE_RSP_STRING) + " RSSI:" + device.rssi);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.GapScanResult(this));
    }

    public ArrayList<GapScannedDevice> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<GapScannedDevice> devices) {
        this.devices = devices;
    }
}
