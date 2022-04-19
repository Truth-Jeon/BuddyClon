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
import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.regex.Pattern;

public class SpiReadCommand extends CodelessCommand {
    public static final String TAG = "SpiReadCommand";

    public static final String COMMAND = "SPIRD";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.SPIRD;

    public static final String PATTERN_STRING = "^SPIRD=(\\d+)$"; // <bytes>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int byteNumber;
    private int[] data;

    public SpiReadCommand(CodelessManager manager, int byteNumber) {
        super(manager);
        setByteNumber(byteNumber);
    }

    public SpiReadCommand(CodelessManager manager) {
        super(manager);
    }

    public SpiReadCommand(CodelessManager manager, String command, boolean parse) {
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
        return true;
    }

    @Override
    protected String getArguments() {
        return Integer.toString(byteNumber);
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            String[] values = response.split(",");
            data = new int[values.length];
            try {
                for (int i = 0; i < values.length; i++) {
                    data[i] = Integer.decode(values[i]);
                }
                if (CodelessLibLog.COMMAND)
                    Log.d(TAG, "Read data: " + Arrays.toString(data));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Received invalid data: " + response);
                invalid = true;
            }
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.SpiRead(this));
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        return CodelessProfile.countArguments(command, ",") == 1;
    }

    @Override
    protected String parseArguments() {
        Integer num = decodeNumberArgument(1);
        if (num == null || CodelessLibConfig.CHECK_SPI_READ_SIZE && num > CodelessLibConfig.SPI_MAX_BYTE_READ_SIZE)
            return "Invalid byte number";
        byteNumber = num;

        return null;
    }

    public int getByteNumber() {
        return byteNumber;
    }

    public void setByteNumber(int byteNumber) {
        this.byteNumber = byteNumber;
        if (CodelessLibConfig.CHECK_SPI_READ_SIZE && byteNumber > CodelessLibConfig.SPI_MAX_BYTE_READ_SIZE)
            invalid = true;
    }

    public int[] getData() {
        return data;
    }
}
