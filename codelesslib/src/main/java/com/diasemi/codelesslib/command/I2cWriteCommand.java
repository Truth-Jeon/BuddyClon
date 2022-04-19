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

import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import java.util.Locale;
import java.util.regex.Pattern;

public class I2cWriteCommand extends CodelessCommand {
    public static final String TAG = "I2cWriteCommand";

    public static final String COMMAND = "I2CWRITE";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.I2CWRITE;

    public static final String PATTERN_STRING = "^I2CWRITE=(0[xX][0-9a-fA-F]+|\\d+),(0[xX][0-9a-fA-F]+|\\d+),(0[xX][0-9a-fA-F]+|\\d+)$"; // <address> <register> <data>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int address;
    private int register;
    private int value;

    public I2cWriteCommand(CodelessManager manager, int address, int register, int value) {
        super(manager);
        this.address = address;
        this.register = register;
        this.value = value;
    }

    public I2cWriteCommand(CodelessManager manager) {
        super(manager);
    }

    public I2cWriteCommand(CodelessManager manager, String command, boolean parse) {
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
        return String.format(Locale.US, "0x%02X,0x%02X,%d", address, register, value);
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        return CodelessProfile.countArguments(command, ",") == 3;
    }

    @Override
    protected String parseArguments() {
        Integer num = decodeNumberArgument(1);
        if (num == null)
            return "Invalid slave address";
        address = num;

        num = decodeNumberArgument(2);
        if (num == null)
            return "Invalid slave register";
        register = num;

        num = decodeNumberArgument(3);
        if (num == null)
            return "Invalid byte number";
        value = num;

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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
