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

import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import java.util.regex.Pattern;

public class BinRequestAckCommand extends CodelessCommand {
    public static final String TAG = "BinRequestAckCommand";

    public static final String COMMAND = "BINREQACK";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.BINREQACK;

    public static final String PATTERN_STRING = "^BINREQACK$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public BinRequestAckCommand(CodelessManager manager) {
        super(manager);
    }

    public BinRequestAckCommand(CodelessManager manager, String command, boolean parse) {
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
    public void onSuccess() {
        super.onSuccess();
        Log.d(TAG, "Binary mode ACK");
        manager.onBinAckSent();
    }

    @Override
    public void processInbound() {
        Log.d(TAG, "Received binary mode ACK");
        sendSuccess();
        manager.onBinAckReceived();
    }
}
