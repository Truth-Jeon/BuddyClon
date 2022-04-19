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

import static com.diasemi.codelesslib.CodelessProfile.Command.UART_ECHO_OFF;
import static com.diasemi.codelesslib.CodelessProfile.Command.UART_ECHO_ON;

public class UartEchoCommand extends CodelessCommand {
    public static final String TAG = "UartEchoCommand";

    public static final String COMMAND = "E";
    public static final String NAME = CodelessProfile.PREFIX + COMMAND;
    public static final CommandID ID = CommandID.ATE;

    public static final String PATTERN_STRING = "^E(?:=(\\d))?$"; // <echo>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private boolean echo;
    private boolean hasArguments;

    public UartEchoCommand(CodelessManager manager, boolean echo) {
        this(manager);
        this.echo = echo;
        hasArguments = true;
    }

    public UartEchoCommand(CodelessManager manager) {
        super(manager);
    }

    public UartEchoCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? Integer.toString(echo ? UART_ECHO_ON : UART_ECHO_OFF) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                int num = Integer.parseInt(response);
                if (num != UART_ECHO_ON && num != UART_ECHO_OFF)
                    invalid = true;
                echo = num != UART_ECHO_OFF;
            } catch (NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid UART echo state");
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "UART echo state: " + (echo ? "enabled" : "disabled"));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.UartEcho(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 1;
    }

    @Override
    protected String parseArguments() {
        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || num != UART_ECHO_ON && num != UART_ECHO_OFF)
            return "Invalid UART echo state";
        echo = num != UART_ECHO_OFF;
        return null;
    }

    public boolean echo() {
        return echo;
    }

    public void setEcho(boolean echo) {
        this.echo = echo;
    }
}
