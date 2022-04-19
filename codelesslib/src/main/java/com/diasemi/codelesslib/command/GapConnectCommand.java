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

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

public class GapConnectCommand extends CodelessCommand {
    public static final String TAG = "GapConnectCommand";

    public static final String COMMAND = "GAPCONNECT";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.GAPCONNECT;

    public static final String ADDRESS_PATTERN_STRING = "(?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}";
    public static final Pattern ADDRESS_PATTERN = Pattern.compile("^" + ADDRESS_PATTERN_STRING + "$");

    public static final String PATTERN_STRING = "^GAPCONNECT=("+ ADDRESS_PATTERN_STRING +"),([PR])$"; // <address> <type>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private String address;
    private int addressType;
    private boolean connected;

    public GapConnectCommand(CodelessManager manager, String address, int addressType) {
        this(manager);
        setAddress(address);
        setAddressType(addressType);
    }

    public GapConnectCommand(CodelessManager manager) {
        super(manager);
    }

    public GapConnectCommand(CodelessManager manager, String command, boolean parse) {
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
        if (!response.equals("Connected") && !response.contains("Connecting")) {
            invalid = true;
        } else if (response.equals("Connected")) {
            connected = true;
        }
        if (invalid) {
            Log.e(TAG, "Received invalid response: " + response);
        } else if (CodelessLibLog.COMMAND)
            Log.d(TAG, "Connect status: " + response);

    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid() & connected)
            EventBus.getDefault().post(new CodelessEvent.DeviceConnected(this));
    }

    @Override
    protected boolean hasArguments() {
        return true;
    }

    @Override
    protected String getArguments() {
        return address + "," + (addressType == Command.GAP_ADDRESS_TYPE_PUBLIC ? Command.GAP_ADDRESS_TYPE_PUBLIC_STRING : Command.GAP_ADDRESS_TYPE_RANDOM_STRING);
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        return CodelessProfile.countArguments(command, ",") == 2;
    }

    @Override
    protected String parseArguments() {
        String address = matcher.group(1);
        if (address == null)
            return "Invalid address";
        this.address = address;

        String addressTypeString = matcher.group(2);
        addressType = addressTypeString.equals(Command.GAP_ADDRESS_TYPE_PUBLIC_STRING) ? Command.GAP_ADDRESS_TYPE_PUBLIC : Command.GAP_ADDRESS_TYPE_RANDOM;

        return null;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        if (!ADDRESS_PATTERN.matcher(address).matches())
            invalid = true;
    }

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
        if (addressType != Command.GAP_ADDRESS_TYPE_PUBLIC && addressType != Command.GAP_ADDRESS_TYPE_RANDOM)
            invalid = true;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
