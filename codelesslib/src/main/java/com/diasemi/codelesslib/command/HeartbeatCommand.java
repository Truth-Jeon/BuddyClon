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

import static com.diasemi.codelesslib.CodelessProfile.Command.HEARTBEAT_DISABLED;
import static com.diasemi.codelesslib.CodelessProfile.Command.HEARTBEAT_ENABLED;

public class HeartbeatCommand extends CodelessCommand {
    public static final String TAG = "HeartbeatCommand";

    public static final String COMMAND = "HRTBT";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.HRTBT;

    public static final String PATTERN_STRING = "^HRTBT(?:=(\\d))?$"; // <heartbeat>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private boolean enabled;
    private boolean hasArguments;

    public HeartbeatCommand(CodelessManager manager, boolean enabled) {
        this(manager);
        this.enabled = enabled;
        hasArguments = true;
    }

    public HeartbeatCommand(CodelessManager manager) {
        super(manager);
    }

    public HeartbeatCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? Integer.toString(enabled ? HEARTBEAT_ENABLED : HEARTBEAT_DISABLED) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                int num = Integer.parseInt(response);
                if (num != HEARTBEAT_ENABLED && num != HEARTBEAT_DISABLED)
                    invalid = true;
                enabled = num != HEARTBEAT_DISABLED;
            } catch (NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid heartbeat state: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Heartbeat state: " + (enabled ? "enabled" : "disabled"));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.Heartbeat(this));
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
        if (num == null || num != HEARTBEAT_ENABLED && num != HEARTBEAT_DISABLED)
            return "Invalid heartbeat state";
        enabled = num != HEARTBEAT_DISABLED;

        return null;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
