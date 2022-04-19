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

import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import java.util.regex.Pattern;

public class TimerStopCommand extends CodelessCommand {
    public static final String TAG = "TimerStopCommand";

    public static final String COMMAND = "TMRSTOP";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.TMRSTOP;

    public static final String PATTERN_STRING = "^TMRSTOP=(\\d+)$"; // <timer>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int timerIndex;

    public TimerStopCommand(CodelessManager manager, int timerIndex) {
        this(manager);
        setTimerIndex(timerIndex);
    }

    public TimerStopCommand(CodelessManager manager) {
        super(manager);
    }

    public TimerStopCommand(CodelessManager manager, String command, boolean parse) {
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
        return Integer.toString(timerIndex);
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
        if (num == null || CodelessLibConfig.CHECK_TIMER_INDEX && (num < CodelessLibConfig.TIMER_INDEX_MIN || num > CodelessLibConfig.TIMER_INDEX_MAX))
            return "Invalid timer index";
        timerIndex = num;
        return null;
    }

    public int getTimerIndex() {
        return timerIndex;
    }

    public void setTimerIndex(int timerIndex) {
        this.timerIndex = timerIndex;
        if (CodelessLibConfig.CHECK_TIMER_INDEX) {
            if (timerIndex < CodelessLibConfig.TIMER_INDEX_MIN || timerIndex > CodelessLibConfig.TIMER_INDEX_MAX)
                invalid = true;
        }
    }
}
