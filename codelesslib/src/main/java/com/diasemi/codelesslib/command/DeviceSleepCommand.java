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
import com.diasemi.codelesslib.CodelessProfile.Command;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import java.util.regex.Pattern;

public class DeviceSleepCommand extends CodelessCommand {
    public static final String TAG = "DeviceSleepCommand";

    public static final String COMMAND = "SLEEP";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.SLEEP;

    public static final String PATTERN_STRING = "^SLEEP=(\\d)$"; // <sleep>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private boolean sleep;

    public DeviceSleepCommand(CodelessManager manager, boolean sleep) {
        this(manager);
        this.sleep = sleep;
    }

    public DeviceSleepCommand(CodelessManager manager) {
        super(manager);
    }

    public DeviceSleepCommand(CodelessManager manager, String command, boolean parse) {
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
        return Integer.toString(!sleep ? Command.AWAKE_DEVICE : Command.PUT_DEVICE_IN_SLEEP);
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        return CodelessProfile.countArguments(command, ",") == 1;
    }

    @Override
    protected String parseArguments() {
        Integer num = decodeNumberArgument(1);
        if (num == null || num != Command.AWAKE_DEVICE && num != Command.PUT_DEVICE_IN_SLEEP)
            return "Argument must be 0 or 1";
        sleep = num == Command.PUT_DEVICE_IN_SLEEP;
        return null;
    }

    public boolean sleep() {
        return sleep;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }
}
