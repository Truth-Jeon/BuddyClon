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

import static com.diasemi.codelesslib.CodelessProfile.Command.SECURITY_MODE_0;
import static com.diasemi.codelesslib.CodelessProfile.Command.SECURITY_MODE_1;
import static com.diasemi.codelesslib.CodelessProfile.Command.SECURITY_MODE_2;
import static com.diasemi.codelesslib.CodelessProfile.Command.SECURITY_MODE_3;

public class SecurityModeCommand extends CodelessCommand {
    public static final String TAG = "SecurityModeCommand";

    public static final String COMMAND = "SEC";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.SEC;

    public static final String PATTERN_STRING = "^SEC(?:=(\\d+))?$"; // <mode>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int mode;
    private boolean hasArguments;

    public SecurityModeCommand(CodelessManager manager, int mode) {
        this(manager);
        setMode(mode);
        hasArguments = true;
    }

    public SecurityModeCommand(CodelessManager manager) {
        super(manager);
    }

    public SecurityModeCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? Integer.toString(mode) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                mode = Integer.parseInt(response);
                if (mode != SECURITY_MODE_0 && mode != SECURITY_MODE_1 && mode != SECURITY_MODE_2 && mode != SECURITY_MODE_3)
                    invalid = true;
            } catch (NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid security mode: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Security mode: " + mode);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.SecurityMode(this));
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
        if (num == null || num != SECURITY_MODE_0 && num != SECURITY_MODE_1 && num != SECURITY_MODE_2 && num != SECURITY_MODE_3)
            return "Invalid security mode";
        mode = num;

        return null;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        if (mode != SECURITY_MODE_0 && mode != SECURITY_MODE_1 && mode != SECURITY_MODE_2 && mode != SECURITY_MODE_3)
            invalid = true;
    }
}
