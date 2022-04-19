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

import java.util.Locale;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessProfile.Command.HOST_SLEEP_MODE_0;
import static com.diasemi.codelesslib.CodelessProfile.Command.HOST_SLEEP_MODE_1;

public class HostSleepCommand extends CodelessCommand {
    public static final String TAG = "HostSleepCommand";

    public static final String COMMAND = "HOSTSLP";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.HOSTSLP;

    public static final String PATTERN_STRING = "^HOSTSLP(?:=(\\d+),(\\d+),(\\d+),(\\d+))?$"; // <hst_slp_mode> <wkup_byte> <wkup_retry_interval> <wkup_retry_times>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int hostSleepMode;
    private int wakeupByte;
    private int wakeupRetryInterval;
    private int wakeupRetryTimes;
    private boolean hasArguments;

    public HostSleepCommand(CodelessManager manager, int hostSleepMode, int wakeupByte, int wakeupRetryInterval, int wakeupRetryTimes) {
        super(manager);
        setHostSleepMode(hostSleepMode);
        this.wakeupByte = wakeupByte;
        this.wakeupRetryInterval = wakeupRetryInterval;
        this.wakeupRetryTimes = wakeupRetryTimes;
        hasArguments = true;
    }

    public HostSleepCommand(CodelessManager manager) {
        super(manager);
    }

    public HostSleepCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%d,%d,%d", hostSleepMode, wakeupByte, wakeupRetryInterval, wakeupRetryTimes) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                String[] parameters = response.split(" ");
                hostSleepMode = Integer.parseInt(parameters[0]);
                if (hostSleepMode != HOST_SLEEP_MODE_0 && hostSleepMode != HOST_SLEEP_MODE_1)
                    invalid = true;
                wakeupByte = Integer.parseInt(parameters[1]);
                wakeupRetryInterval = Integer.parseInt(parameters[2]);
                wakeupRetryTimes = Integer.parseInt(parameters[3]);
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid host sleep response");
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Host sleep mode:" + hostSleepMode + " wakeup byte:" + wakeupByte + " wakeup retry interval:" + wakeupRetryInterval + " wakeup retry times:" + wakeupRetryTimes);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.HostSleep(this));
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
        if (num == null || num != HOST_SLEEP_MODE_0 && num != HOST_SLEEP_MODE_1)
            return "Invalid host sleep mode";
        hostSleepMode = num;

        num = decodeNumberArgument(2);
        if (num == null)
            return "Invalid wakeup byte";
        wakeupByte = num;

        num = decodeNumberArgument(3);
        if (num == null)
            return "Invalid wakeup retry interval";
        wakeupRetryInterval = num;

        num = decodeNumberArgument(4);
        if (num == null)
            return "Invalid wakeup retry times";
        wakeupRetryTimes = num;

        return null;
    }

    public int getHostSleepMode() {
        return hostSleepMode;
    }

    public void setHostSleepMode(int hostSleepMode) {
        this.hostSleepMode = hostSleepMode;
        if (hostSleepMode != HOST_SLEEP_MODE_0 && hostSleepMode != Command.HOST_SLEEP_MODE_1)
            invalid = true;
    }

    public int getWakeupByte() {
        return wakeupByte;
    }

    public void setWakeupByte(int wakeupByte) {
        this.wakeupByte = wakeupByte;
    }

    public int getWakeupRetryInterval() {
        return wakeupRetryInterval;
    }

    public void setWakeupRetryInterval(int wakeupRetryInterval) {
        this.wakeupRetryInterval = wakeupRetryInterval;
    }

    public int getWakeupRetryTimes() {
        return wakeupRetryTimes;
    }

    public void setWakeupRetryTimes(int wakeupRetryTimes) {
        this.wakeupRetryTimes = wakeupRetryTimes;
    }
}
