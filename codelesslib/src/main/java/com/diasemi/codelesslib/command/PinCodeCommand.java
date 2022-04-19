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

public class PinCodeCommand extends CodelessCommand {
    public static final String TAG = "PinCodeCommand";

    public static final String COMMAND = "PIN";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.PIN;

    public static final String PATTERN_STRING = "^PIN(?:=(\\d+))?$"; // <pin>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int pinCode;
    private boolean hasArguments;

    public PinCodeCommand(CodelessManager manager, int pinCode) {
        this(manager);
        this.pinCode = pinCode;
        hasArguments = true;
    }

    public PinCodeCommand(CodelessManager manager) {
        super(manager);
    }

    public PinCodeCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? Integer.toString(pinCode) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                pinCode = Integer.parseInt(response);
                if (CodelessLibLog.COMMAND)
                    Log.d(TAG, "PIN code: " + pinCode);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Received invalid PIN code: " + response);
                invalid = true;
            }
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.PinCode(this));
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
        if (num == null)
            return "Invalid PIN code";
        pinCode = num;
        return null;
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }
}
