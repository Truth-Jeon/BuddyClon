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

import java.util.regex.Pattern;

public class PeripheralRoleSetCommand extends CodelessCommand {
    public static final String TAG = "PeripheralRoleSetCommand";

    public static final String COMMAND = "PERIPHERAL";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.PERIPHERAL;

    public static final String PATTERN_STRING = "^PERIPHERAL$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public PeripheralRoleSetCommand(CodelessManager manager) {
        super(manager);
    }

    public PeripheralRoleSetCommand(CodelessManager manager, String command, boolean parse) {
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
}
