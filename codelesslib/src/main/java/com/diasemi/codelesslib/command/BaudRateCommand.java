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

import java.util.regex.Pattern;

public class BaudRateCommand extends CodelessCommand {
    public static final String TAG = "BaudRateCommand";

    public static final String COMMAND = "BAUD";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.BAUD;

    public static final String PATTERN_STRING = "^BAUD(?:=(\\d+))?$"; // <baud>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int baudRate;
    private boolean hasArguments;

    public BaudRateCommand(CodelessManager manager, int baudRate) {
        this(manager);
        setBaudRate(baudRate);
        hasArguments = true;
    }

    public BaudRateCommand(CodelessManager manager) {
        super(manager);
    }

    public BaudRateCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? Integer.toString(baudRate) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                baudRate = Integer.parseInt(response);
                if (!validBaudRate(baudRate))
                    invalid = true;
            } catch (NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid baud rate: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Baud rate: " + baudRate);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.BaudRate(this));
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
        if (num == null || !validBaudRate(num))
            return "Invalid baud rate";
        baudRate = num;

        return null;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        if (!validBaudRate(baudRate))
            invalid = true;
    }

    private boolean validBaudRate(int baudRate) {
        return baudRate == Command.BAUD_RATE_2400
                || baudRate == Command.BAUD_RATE_4800
                || baudRate == Command.BAUD_RATE_9600
                || baudRate == Command.BAUD_RATE_19200
                || baudRate == Command.BAUD_RATE_38400
                || baudRate == Command.BAUD_RATE_57600
                || baudRate == Command.BAUD_RATE_115200
                || baudRate == Command.BAUD_RATE_230400;
    }
}
