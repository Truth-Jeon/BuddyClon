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

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class I2cReadCommand extends CodelessCommand {
    public static final String TAG = "I2cReadCommand";

    public static final String COMMAND = "I2CREAD";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.I2CREAD;

    public static final String PATTERN_STRING = "^I2CREAD=(0[xX][0-9a-fA-F]+|\\d+),(0[xX][0-9a-fA-F]+|\\d+)(?:,(\\d+))?$"; // <address> <register> <bytes>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int address;
    private int register;
    private int byteCount;
    private int[] data = new int[0];

    public I2cReadCommand(CodelessManager manager, int address, int register) {
        super(manager);
        this.address = address;
        this.register = register;
        byteCount = -1;
    }

    public I2cReadCommand(CodelessManager manager, int address, int register, int byteCount) {
        this(manager, address, register);
        this.byteCount = byteCount;
    }

    public I2cReadCommand(CodelessManager manager) {
        super(manager);
    }

    public I2cReadCommand(CodelessManager manager, String command, boolean parse) {
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
    protected boolean hasArguments() {
        return true;
    }

    @Override
    protected String getArguments() {
        if (byteCount == -1)
            return String.format(Locale.US, "0x%02X,0x%02X", address, register);
        return String.format(Locale.US, "0x%02X,0x%02X,%d", address, register, byteCount);
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            String[] values = response.split(",");
            data = new int[values.length];
            try {
                for (int i = 0; i < values.length; i++) {
                    data[i] = Integer.decode(values[i]);
                }
                if (CodelessLibLog.COMMAND)
                    Log.d(TAG, "Read data: " + Arrays.toString(data));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Received invalid data: " + response);
                invalid = true;
            }
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.I2cRead(this));
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 2 || count == 3;
    }

    @Override
    protected String parseArguments() {
        byteCount = -1;
        int count = CodelessProfile.countArguments(command, ",");

        Integer num = decodeNumberArgument(1);
        if (num == null)
            return "Invalid slave address";
        address = num;

        num = decodeNumberArgument(2);
        if (num == null)
            return "Invalid register";
        register = num;

        if (count == 3) {
            num = decodeNumberArgument(3);
            if (num == null)
                return "Invalid number of bytes";
            byteCount = num;
        }

        return null;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public int getByteCount() {
        return byteCount;
    }

    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

    public int[] getData() {
        return data;
    }
}
