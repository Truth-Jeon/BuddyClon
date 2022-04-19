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
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public class I2cScanCommand extends CodelessCommand {
    public static final String TAG = "I2cScanCommand";

    public static final String COMMAND = "I2CSCAN";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.I2CSCAN;

    public static final String PATTERN_STRING = "^I2CSCAN$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public static class I2cDevice {
        public int address;
        public int registerZero = -1;

        public I2cDevice(int address) {
            this.address = address;
        }

        public I2cDevice(int address, int registerZero) {
            this(address);
            this.registerZero = registerZero;
        }

        public boolean hasRegisterZero() {
            return registerZero != -1;
        }

        public String addressString() {
            return "0x" + Integer.toString(address, 16);
        }

        @NonNull
        @Override
        public String toString() {
            return addressString() + (hasRegisterZero() ? "(0x" + Integer.toString(registerZero, 16) + ")" : "");
        }
    }

    private ArrayList<I2cDevice> devices = new ArrayList<>();

    public I2cScanCommand(CodelessManager manager) {
        super(manager);
    }

    public I2cScanCommand(CodelessManager manager, String command, boolean parse) {
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
        String[] scanResults = response.split(",");
        for (String result : scanResults) {
            try {
                int index = result.indexOf(":");
                I2cDevice device = new I2cDevice(Integer.decode(index == -1 ? result : result.substring(0, index)));
                if (index != -1)
                    device.registerZero = Integer.decode(result.substring(index + 1));
                devices.add(device);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Received invalid I2C scan results: " + response);
                invalid = true;
                return;
            }
        }
        if (CodelessLibLog.COMMAND)
            Log.d(TAG, "I2C scan results: " + devices);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid()) {
            if (CodelessLibLog.COMMAND && devices.isEmpty())
                Log.d(TAG, "No I2C devices found");
            EventBus.getDefault().post(new CodelessEvent.I2cScan(this));
        }
    }

    public ArrayList<I2cDevice> getDevices() {
        return devices;
    }
}
