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

public class GapStatusCommand extends CodelessCommand {
    public static final String TAG = "GapStatusCommand";

    public static final String COMMAND = "GAPSTATUS";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.GAPSTATUS;

    public static final String PATTERN_STRING = "^GAPSTATUS$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int gapRole = -1;
    private boolean connected;

    public GapStatusCommand(CodelessManager manager) {
        super(manager);
    }

    public GapStatusCommand(CodelessManager manager, String command, boolean parse) {
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
            String[] status = response.split(",");
            try {
                int gapRole = Integer.parseInt(status[0]);
                if (gapRole != Command.GAP_ROLE_PERIPHERAL && gapRole != Command.GAP_ROLE_CENTRAL)
                    invalid = true;
                this.gapRole = gapRole;
                int connected = Integer.parseInt(status[1]);
                if (connected != Command.GAP_STATUS_DISCONNECTED && connected != Command.GAP_STATUS_CONNECTED)
                    invalid = true;
                this.connected = connected == Command.GAP_STATUS_CONNECTED;
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.i(TAG, "Received invalid GAP status response: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "GAP status response: " + response);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.GapStatus(this));
    }

    @Override
    public void processInbound() {
        if (gapRole == -1) {
            gapRole = Command.GAP_ROLE_CENTRAL;
            connected = manager.isConnected();
        }
        String response = String.format(Locale.US, "%d,%d", gapRole, connected ? Command.GAP_STATUS_CONNECTED : Command.GAP_STATUS_DISCONNECTED);
        if (CodelessLibLog.COMMAND)
            Log.d(TAG, "GAP status: " + response);
        sendSuccess(response);
    }

    public int getGapRole() {
        return gapRole;
    }

    public void setGapRole(int gapRole) {
        this.gapRole = gapRole;
        if (gapRole != Command.GAP_ROLE_PERIPHERAL && gapRole != Command.GAP_ROLE_CENTRAL)
            invalid = true;
    }

    public boolean connected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
