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

public class GapDisconnectCommand extends CodelessCommand {
    public static final String TAG = "GapDisconnectCommand";

    public static final String COMMAND = "GAPDISCONNECT";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.GAPDISCONNECT;

    public static final String PATTERN_STRING = "^GAPDISCONNECT$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private boolean disconnected;

    public GapDisconnectCommand(CodelessManager manager) {
        super(manager);
    }

    public GapDisconnectCommand(CodelessManager manager, String command, boolean parse) {
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
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            if (!response.equals("Disconnected"))
                invalid = true;
            disconnected = true;
            if (invalid) {
                Log.e(TAG, "Received invalid response: " + response);
            } else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Device disconnected");
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.DeviceDisconnected(this));
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }
}
