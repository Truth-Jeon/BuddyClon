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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import com.diasemi.codelesslib.CodelessEvent;
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

public class BatteryLevelCommand extends CodelessCommand {
    public static final String TAG = "BatteryLevelCommand";

    public static final String COMMAND = "BATT";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.BATT;

    public static final String PATTERN_STRING = "^BATT$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int level = -1;

    public BatteryLevelCommand(CodelessManager manager) {
        super(manager);
    }

    public BatteryLevelCommand(CodelessManager manager, String command, boolean parse) {
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
            try {
                level = Integer.parseInt(response);
                if (CodelessLibLog.COMMAND)
                    Log.d(TAG, "Battery level: " + level);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Received invalid battery level: " + response, e);
                invalid = true;
            }
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.BatteryLevel(this));
    }

    @Override
    public void processInbound() {
        if (level == -1)
            level = getBatteryLevel();
        if (level != -1) {
            if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Send battery level: " + level);
            sendSuccess(Integer.toString(level));
        } else {
            Log.e(TAG, "Failed to retrieve battery level");
            sendError("Battery level not available");
        }
    }

    private int getBatteryLevel() {
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager batteryManager = (BatteryManager) manager.getContext().getSystemService(Context.BATTERY_SERVICE);
            if (batteryManager != null) {
                int level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                if (level != Integer.MIN_VALUE)
                    return level;
            }
        }
        Intent status = manager.getContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (status == null || !status.hasExtra(BatteryManager.EXTRA_LEVEL) || !status.hasExtra(BatteryManager.EXTRA_SCALE))
            return -1;
        int level = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return 100 * level / scale;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
