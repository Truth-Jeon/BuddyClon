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

public class ResetIoConfigCommand extends CodelessCommand {
    public static final String TAG = "ResetIoConfigCommand";

    public static final String COMMAND = "Z";
    public static final String NAME = CodelessProfile.PREFIX + COMMAND;
    public static final CommandID ID = CommandID.ATZ;

    public static final String PATTERN_STRING = "^Z$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public ResetIoConfigCommand(CodelessManager manager) {
        super(manager);
    }

    public ResetIoConfigCommand(CodelessManager manager, String command, boolean parse) {
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
