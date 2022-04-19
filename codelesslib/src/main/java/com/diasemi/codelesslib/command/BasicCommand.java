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

import java.util.regex.Pattern;

public class BasicCommand extends CodelessCommand {
    public static final String TAG = "BasicCommand";

    public static final String COMMAND = "";
    public static final String NAME = CodelessProfile.PREFIX + COMMAND;
    public static final CommandID ID = CommandID.AT;

    public static final String PATTERN_STRING = "^$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public BasicCommand(CodelessManager manager) {
        super(manager);
    }

    public BasicCommand(CodelessManager manager, String command, boolean parse) {
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
    public void onSuccess() {
        super.onSuccess();
        if (CodelessLibLog.COMMAND)
            Log.d(TAG, "OK");
        EventBus.getDefault().post(new CodelessEvent.Ping(this));
    }
}
