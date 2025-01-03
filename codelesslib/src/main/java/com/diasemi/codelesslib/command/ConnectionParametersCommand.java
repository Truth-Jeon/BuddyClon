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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessProfile.Command.CONNECTION_INTERVAL_MAX;
import static com.diasemi.codelesslib.CodelessProfile.Command.CONNECTION_INTERVAL_MIN;
import static com.diasemi.codelesslib.CodelessProfile.Command.PARAMETER_UPDATE_ACTION_MAX;
import static com.diasemi.codelesslib.CodelessProfile.Command.PARAMETER_UPDATE_ACTION_MIN;
import static com.diasemi.codelesslib.CodelessProfile.Command.SLAVE_LATENCY_MAX;
import static com.diasemi.codelesslib.CodelessProfile.Command.SLAVE_LATENCY_MIN;
import static com.diasemi.codelesslib.CodelessProfile.Command.SUPERVISION_TIMEOUT_MAX;
import static com.diasemi.codelesslib.CodelessProfile.Command.SUPERVISION_TIMEOUT_MIN;

public class ConnectionParametersCommand extends CodelessCommand {
    public static final String TAG = "ConnectionParametersCommand";

    public static final String COMMAND = "CONPAR";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.CONPAR;

    public static final String PATTERN_STRING = "^CONPAR(?:=(\\d+),(\\d+),(\\d+),(\\d+))?$"; // <interval> <latency> <timeout> <action>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private static final String RESPONSE_PATTERN_STRING = "^(\\d+).(\\d+).(\\d+).(\\d+)$"; // <interval> <latency> <timeout> <action>
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(RESPONSE_PATTERN_STRING);

    private int interval;
    private int latency;
    private int timeout;
    private int action;
    private boolean hasArguments;

    public ConnectionParametersCommand(CodelessManager manager, int interval, int latency, int timeout, int action) {
        this(manager);
        setInterval(interval);
        setLatency(latency);
        setTimeout(timeout);
        setAction(action);
        hasArguments = true;
    }

    public ConnectionParametersCommand(CodelessManager manager) {
        super(manager);
    }

    public ConnectionParametersCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%d,%d,%d", interval, latency, timeout, action) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            Matcher matcher = RESPONSE_PATTERN.matcher(response);
            if (matcher.matches()) {
                try {
                    setInterval(Integer.parseInt(matcher.group(1)));
                    setLatency(Integer.parseInt(matcher.group(2)));
                    setTimeout(Integer.parseInt(matcher.group(3)));
                    setAction(Integer.parseInt(matcher.group(4)));
                } catch (NumberFormatException e) {
                    invalid = true;
                }
            } else {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid connection parameters: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Connection parameters: ci=" + interval + " sl=" + latency + " st=" + timeout + " a=" + action);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.ConnectionParameters(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 4;
    }

    @Override
    protected String parseArguments() {
        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || num < CONNECTION_INTERVAL_MIN || num > CONNECTION_INTERVAL_MAX)
            return "Invalid connection interval";
        interval = num;

        num = decodeNumberArgument(2);
        if (num == null || num < SLAVE_LATENCY_MIN || num > SLAVE_LATENCY_MAX)
            return "Invalid slave latency";
        latency = num;

        num = decodeNumberArgument(3);
        if (num == null || num < SUPERVISION_TIMEOUT_MIN || num > SUPERVISION_TIMEOUT_MAX)
            return "Invalid supervision timeout";
        timeout = num;

        num = decodeNumberArgument(4);
        if (num == null || num < PARAMETER_UPDATE_ACTION_MIN || num > PARAMETER_UPDATE_ACTION_MAX)
            return "Invalid parameter update action";
        action = num;

        return null;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
        if (interval < CONNECTION_INTERVAL_MIN || interval > CONNECTION_INTERVAL_MAX)
            invalid = true;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
        if (latency < SLAVE_LATENCY_MIN || latency > SLAVE_LATENCY_MAX)
            invalid = true;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        if (timeout < SUPERVISION_TIMEOUT_MIN || timeout > SUPERVISION_TIMEOUT_MAX)
            invalid = true;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
        if (action < PARAMETER_UPDATE_ACTION_MIN || action > PARAMETER_UPDATE_ACTION_MAX)
            invalid = true;
    }
}
