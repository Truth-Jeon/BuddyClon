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
import com.diasemi.codelesslib.CodelessUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

public class AdvertisingDataCommand extends AdvertisingDataBaseCommand {
    public static final String TAG = "AdvertisingDataCommand";

    public static final String COMMAND = "ADVDATA";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.ADVDATA;

    public static final String PATTERN_STRING = "^ADVDATA(?:=((?:" + DATA_PATTERN_STRING + ")?))?$"; // <data>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public AdvertisingDataCommand(CodelessManager manager, byte[] data) {
        super(manager, data);
    }

    public AdvertisingDataCommand(CodelessManager manager) {
        super(manager);
    }

    public AdvertisingDataCommand(CodelessManager manager, String command, boolean parse) {
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
            if (invalid)
                Log.e(TAG, "Received invalid advertising data: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Advertising data: " + CodelessUtil.hexArrayLog(data));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid()) {
            if (data == null) {
                data = new byte[0];
                if (CodelessLibLog.COMMAND)
                    Log.d(TAG, "No advertising data");
            }
            EventBus.getDefault().post(new CodelessEvent.AdvertisingData(this));
        }
    }
}
