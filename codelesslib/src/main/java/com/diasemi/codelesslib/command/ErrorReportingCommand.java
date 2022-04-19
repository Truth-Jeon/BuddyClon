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

public class ErrorReportingCommand extends CodelessCommand {
    public static final String TAG = "ErrorReportingCommand";

    public static final String COMMAND = "F";
    public static final String NAME = CodelessProfile.PREFIX + COMMAND;
    public static final CommandID ID = CommandID.ATF;

    public static final String PATTERN_STRING = "^F=(\\d)$"; // <enabled>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private boolean enabled;

    public ErrorReportingCommand(CodelessManager manager, boolean enabled) {
        this(manager);
        this.enabled = enabled;
    }

    public ErrorReportingCommand(CodelessManager manager) {
        super(manager);
    }

    public ErrorReportingCommand(CodelessManager manager, String command, boolean parse) {
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
        return Integer.toString(enabled ? Command.ERROR_REPORTING_ON : Command.ERROR_REPORTING_OFF);
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
        if (num == null || num != Command.ERROR_REPORTING_ON && num != Command.ERROR_REPORTING_OFF)
            return "Argument must be 0 or 1";
        enabled = num == Command.ERROR_REPORTING_ON;
        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
