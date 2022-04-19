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

import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

public class CustomCommand extends CodelessCommand {
    public static final String TAG = "CustomCommand";

    public static final String COMMAND = "CUSTOM";
    public static final String NAME = "CUSTOM";
    public static final CommandID ID = CommandID.CUSTOM;

    public CustomCommand(CodelessManager manager) {
        super(manager);
    }

    public CustomCommand(CodelessManager manager, String command, boolean parse) {
        super(manager, command, parse);
    }

    public CustomCommand(CodelessManager manager, String command) {
        this(manager, command, true);
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
    public String parseCommand(String command) {
        if (CodelessLibLog.COMMAND)
            Log.d(TAG, "Custom command: " + command);
        this.command = command;
        parsed = true;
        return null;
    }
}
