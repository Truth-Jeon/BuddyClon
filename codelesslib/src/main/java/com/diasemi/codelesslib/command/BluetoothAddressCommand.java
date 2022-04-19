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

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.diasemi.codelesslib.CodelessEvent;
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluetoothAddressCommand extends CodelessCommand {
    public static final String TAG = "BluetoothAddressCommand";

    public static final String COMMAND = "BDADDR";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.BDADDR;

    public static final String PATTERN_STRING = "^BDADDR$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private static final String RESPONSE_PATTERN_STRING = "^([^,]*)(?:,([PR]))?$"; // <address> <type>
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(RESPONSE_PATTERN_STRING);

    private String address;
    private boolean random;

    public BluetoothAddressCommand(CodelessManager manager) {
        super(manager);
    }

    public BluetoothAddressCommand(CodelessManager manager, String command, boolean parse) {
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
        if (responseLine() == 1) {
            Matcher matcher = RESPONSE_PATTERN.matcher(response);
            if (matcher.matches()) {
                address = matcher.group(1);
                if (matcher.group(2) != null)
                    random = matcher.group(2).equals("R");
                if (!BluetoothAdapter.checkBluetoothAddress(address))
                    invalid = true;
            } else {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid BD address: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "BD address: " + address + (matcher.group(2) != null ? random ? " (random)" : " (public)" : ""));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.BluetoothAddress(this));
    }

    @Override
    public void processInbound() {
        if (address == null) {
            address = BluetoothAdapter.getDefaultAdapter().getAddress();
        }
        // Android may restrict access to the device BD address and return a hardcoded value.
        if (!address.equals("02:00:00:00:00:00")) {
            if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Send BD address: " + address);
            sendSuccess(address);
        } else {
            Log.e(TAG, "System returned invalid BD address: " + address);
            sendError("BD address not available");
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isPublic() {
        return !random;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }
}
