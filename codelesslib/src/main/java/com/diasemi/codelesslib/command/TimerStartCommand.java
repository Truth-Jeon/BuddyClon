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

import java.util.Locale;
import java.util.regex.Pattern;

public class TimerStartCommand extends CodelessCommand {
    public static final String TAG = "TimerStartCommand";

    public static final String COMMAND = "TMRSTART";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.TMRSTART;

    public static final String PATTERN_STRING = "^TMRSTART=(\\d+),(\\d+),(\\d+)$"; // <timer> <command> <delay>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int timerIndex;
    private int commandIndex;
    private int delay;

    public TimerStartCommand(CodelessManager manager, int timerIndex, int commandIndex, int delay) {
        this(manager);
        setTimerIndex(timerIndex);
        setCommandIndex(commandIndex);
        setDelay(delay);
    }

    public TimerStartCommand(CodelessManager manager) {
        super(manager);
    }

    public TimerStartCommand(CodelessManager manager, String command, boolean parse) {
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
        return String.format(Locale.US, "%d,%d,%d", timerIndex, commandIndex, delay);
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        return CodelessProfile.countArguments(command, ",") == 3;
    }

    @Override
    protected String parseArguments() {
        Integer num = decodeNumberArgument(1);
        if (num == null || CodelessLibConfig.CHECK_TIMER_INDEX && (num < CodelessLibConfig.TIMER_INDEX_MIN || num > CodelessLibConfig.TIMER_INDEX_MAX))
            return "Invalid timer index";
        timerIndex = num;

        num = decodeNumberArgument(2);
        if (num == null || CodelessLibConfig.CHECK_COMMAND_INDEX && (num < CodelessLibConfig.COMMAND_INDEX_MIN || num > CodelessLibConfig.COMMAND_INDEX_MAX))
            return "Invalid command index";
        commandIndex = num;

        num = decodeNumberArgument(3);
        if (num == null)
            return "Invalid delay";
        delay = num;

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

    public int getCommandIndex() {
        return commandIndex;
    }

    public void setCommandIndex(int commandIndex) {
        this.commandIndex = commandIndex;
        if (CodelessLibConfig.CHECK_COMMAND_INDEX) {
            if (commandIndex < CodelessLibConfig.COMMAND_INDEX_MIN || commandIndex > CodelessLibConfig.COMMAND_INDEX_MAX)
                invalid = true;
        }
    }

    public int getDelay() {
        return delay * 10;
    }

    public void setDelay(int delay) {
        this.delay = delay / 10;
        if (delay % 10 != 0)
            this.delay++;
    }
}
