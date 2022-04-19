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

import com.diasemi.codelesslib.CodelessEvent;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.regex.Pattern;

public class I2cConfigCommand extends CodelessCommand {
    public static final String TAG = "I2cConfigCommand";

    public static final String COMMAND = "I2CCFG";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.I2CCFG;

    public static final String PATTERN_STRING = "^I2CCFG=(\\d+),(\\d+),(\\d+)$"; // <count> <rate> <width>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int bitCount;
    private int bitRate;
    private int registerWidth;

    public I2cConfigCommand(CodelessManager manager, int bitCount, int bitRate, int registerWidth) {
        this(manager);
        this.bitCount = bitCount;
        this.bitRate = bitRate;
        this.registerWidth = registerWidth;
    }

    public I2cConfigCommand(CodelessManager manager) {
        super(manager);
    }

    public I2cConfigCommand(CodelessManager manager, String command, boolean parse) {
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
        return String.format(Locale.US, "%d,%d,%d", bitCount, bitRate, registerWidth);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.I2cConfig(this));
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
            return "Invalid slave addressing bit count";
        bitCount = num;

        num = decodeNumberArgument(2);
        if (num == null)
            return "Invalid bit rate";
        bitRate = num;

        num = decodeNumberArgument(3);
        if (num == null)
            return "Invalid register width";
        registerWidth = num;

        return null;
    }

    public int getBitCount() {
        return bitCount;
    }

    public void setBitCount(int bitCount) {
        this.bitCount = bitCount;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getRegisterWidth() {
        return registerWidth;
    }

    public void setRegisterWidth(int registerWidth) {
        this.registerWidth = registerWidth;
    }
}
