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

import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import java.util.regex.Pattern;

public class AdvertisingStartCommand extends CodelessCommand {
    public static final String TAG = "AdvertisingStartCommand";

    public static final String COMMAND = "ADVSTART";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.ADVSTART;

    public static final String PATTERN_STRING = "^ADVSTART(?:=(\\d+))?$"; // <interval>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int interval;
    private boolean hasArguments;

    public AdvertisingStartCommand(CodelessManager manager, int interval) {
        this(manager);
        setInterval(interval);
        hasArguments = true;
    }

    public AdvertisingStartCommand(CodelessManager manager) {
        super(manager);
    }

    public AdvertisingStartCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments;
    }

    @Override
    protected String getArguments() {
        return hasArguments ? Integer.toString(interval) : null;
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 1;
    }

    @Override
    protected String parseArguments() {
        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || CodelessLibConfig.CHECK_ADVERTISING_INTERVAL && (num < CodelessLibConfig.ADVERTISING_INTERVAL_MIN || num > CodelessLibConfig.ADVERTISING_INTERVAL_MAX))
            return "Invalid interval";
        interval = num;
        return null;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
        if (CodelessLibConfig.CHECK_ADVERTISING_INTERVAL) {
            if (interval < CodelessLibConfig.ADVERTISING_INTERVAL_MIN || interval > CodelessLibConfig.ADVERTISING_INTERVAL_MAX)
                invalid = true;
        }
    }
}
