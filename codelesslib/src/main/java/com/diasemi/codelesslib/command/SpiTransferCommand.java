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

public class SpiTransferCommand extends CodelessCommand {
    public static final String TAG = "SpiTransferCommand";

    public static final String COMMAND = "SPITR";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.SPITR;

    public static final String PATTERN_STRING = "^SPITR=(?:0[xX])?([0-9a-fA-F]+)$"; // <hexString>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private String hexString;
    private int[] data;

    public SpiTransferCommand(CodelessManager manager, String hexString) {
        this(manager);
        setHexString(hexString);
    }

    public SpiTransferCommand(CodelessManager manager) {
        super(manager);
    }

    public SpiTransferCommand(CodelessManager manager, String command, boolean parse) {
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
        return hexString;
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
            EventBus.getDefault().post(new CodelessEvent.SpiTransfer(this));
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
        String hexString = matcher.group(1);
        if (CodelessLibConfig.CHECK_SPI_HEX_STRING_WRITE) {
            if (hexString == null || hexString.length() == 0)
                return "Invalid hex string";
            int charSize = hexString.length();
            if (hexString.startsWith("0x") || hexString.startsWith("0X"))
                charSize -= 2;
            if (charSize < CodelessLibConfig.SPI_HEX_STRING_CHAR_SIZE_MIN || charSize > CodelessLibConfig.SPI_HEX_STRING_CHAR_SIZE_MAX)
                return "Invalid hex string";
        }
        this.hexString = hexString;

        return null;
    }

    public String getHexString() {
        return hexString;
    }

    public void setHexString(String hexString) {
        this.hexString = hexString;
        if (CodelessLibConfig.CHECK_SPI_HEX_STRING_WRITE) {
            if (hexString == null) {
                invalid = true;
                return;
            }
            int charSize = hexString.length();
            if (hexString.startsWith("0x") || hexString.startsWith("0X"))
                charSize -= 2;
            if (charSize < CodelessLibConfig.SPI_HEX_STRING_CHAR_SIZE_MIN || charSize > CodelessLibConfig.SPI_HEX_STRING_CHAR_SIZE_MAX)
                invalid = true;
        }
    }

    public int[] getData() {
        return data;
    }
}
