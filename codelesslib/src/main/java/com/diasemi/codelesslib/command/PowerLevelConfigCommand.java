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
import com.diasemi.codelesslib.CodelessProfile.Command;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

public class PowerLevelConfigCommand extends CodelessCommand {
    public static final String TAG = "PowerLevelConfigCommand";

    public static final String COMMAND = "PWRLVL";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.PWRLVL;

    public static final String PATTERN_STRING = "^PWRLVL(?:=(\\d+))?$"; // <level>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int powerLevel;
    private boolean notSupported;
    private boolean hasArguments;

    public PowerLevelConfigCommand(CodelessManager manager, int powerLevel) {
        this(manager);
        setPowerLevel(powerLevel);
        hasArguments = true;
    }

    public PowerLevelConfigCommand(CodelessManager manager) {
        super(manager);
    }

    public PowerLevelConfigCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? Integer.toString(powerLevel) : null;
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 1;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            if (!response.equals(Command.OUTPUT_POWER_LEVEL_NOT_SUPPORTED)) {
                try {
                    setPowerLevel(Integer.parseInt(response));
                } catch (NumberFormatException e) {
                    invalid = true;
                }
                if (invalid)
                    Log.e(TAG, "Received invalid power level: " + response);
                else if (CodelessLibLog.COMMAND)
                    Log.d(TAG, "Power level: " + powerLevel);
            } else {
                Log.d(TAG, "Power level not supported");
                notSupported = true;
            }
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.PowerLevel(this));
    }

    @Override
    protected String parseArguments() {
        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || !validPowerLevel(num))
            return "Invalid power level";
        powerLevel = num;

        return null;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
        if (!validPowerLevel(powerLevel))
            invalid = true;
    }

    public boolean notSupported() {
        return notSupported;
    }

    private boolean validPowerLevel(int powerLevel) {
        return powerLevel == Command.OUTPUT_POWER_LEVEL_MINUS_19_POINT_5_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_MINUS_13_POINT_5_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_MINUS_10_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_MINUS_7_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_MINUS_5_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_MINUS_3_POINT_5_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_MINUS_2_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_MINUS_1_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_0_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_1_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_1_POINT_5_DBM
                || powerLevel == Command.OUTPUT_POWER_LEVEL_2_POINT_5_DBM;
    }
}
