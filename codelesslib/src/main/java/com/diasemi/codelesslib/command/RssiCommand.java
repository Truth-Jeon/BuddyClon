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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssiCommand extends CodelessCommand {
    public static final String TAG = "RssiCommand";

    public static final String COMMAND = "RSSI";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.RSSI;

    public static final String PATTERN_STRING = "^RSSI$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private static final String RESPONSE_PATTERN_STRING = "^(-?\\d+).*$"; // <rssi>
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(RESPONSE_PATTERN_STRING);

    private int rssi;

    public RssiCommand(CodelessManager manager) {
        super(manager);
    }

    public RssiCommand(CodelessManager manager, String command, boolean parse) {
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
            Matcher matcher = RESPONSE_PATTERN.matcher(response);
            if (matcher.matches()) {
                try {
                    rssi = Integer.parseInt(matcher.group(1));
                    if (CodelessLibLog.COMMAND)
                        Log.d(TAG, "Peer RSSI: " + rssi);
                } catch (NumberFormatException e) {
                    invalid = true;
                }
            } else {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid RSSI: " + response);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.PeerRssi(this));
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
