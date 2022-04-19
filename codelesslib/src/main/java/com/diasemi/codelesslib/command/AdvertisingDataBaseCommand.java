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

import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AdvertisingDataBaseCommand extends CodelessCommand {

    protected static final String DATA_PATTERN_STRING = "(?:[0-9a-fA-F]{2}:)*[0-9a-fA-F]{2}";

    protected static final String RESPONSE_PATTERN_STRING = "^(" + DATA_PATTERN_STRING + ")$"; // <data>
    protected static final Pattern RESPONSE_PATTERN = Pattern.compile(RESPONSE_PATTERN_STRING);

    protected static final String DATA_ARGUMENT_PATTERN_STRING = "^(?:" + DATA_PATTERN_STRING + ")?$";
    protected static final Pattern DATA_ARGUMENT_PATTERN = Pattern.compile(DATA_ARGUMENT_PATTERN_STRING);

    public static boolean validData(String data) {
        return DATA_ARGUMENT_PATTERN.matcher(data).matches();
    }

    protected byte[] data;
    protected boolean hasArguments;

    public AdvertisingDataBaseCommand(CodelessManager manager, byte[] data) {
        this(manager);
        this.data = data;
        hasArguments = true;
    }

    public AdvertisingDataBaseCommand(CodelessManager manager) {
        super(manager);
    }

    public AdvertisingDataBaseCommand(CodelessManager manager, String command, boolean parse) {
        super(manager, command, parse);
    }

    @Override
    protected boolean hasArguments() {
        return hasArguments;
    }

    @Override
    protected String getArguments() {
        return hasArguments ? CodelessUtil.hexArray(data, true, false).replace(" ", ":") : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            Matcher matcher = RESPONSE_PATTERN.matcher(response);
            if (matcher.matches()) {
                data = CodelessUtil.hex2bytes(response);
                if (data == null)
                    invalid = true;
            } else {
                invalid = true;
            }
        }
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

        if (DATA_ARGUMENT_PATTERN.matcher(matcher.group(1)).matches()) {
            data = CodelessUtil.hex2bytes(matcher.group(1));
        } else {
            return "Invalid advertising data";
        }

        return null;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getDataString() {
        return CodelessUtil.hexArray(data, true, false);
    }
}
