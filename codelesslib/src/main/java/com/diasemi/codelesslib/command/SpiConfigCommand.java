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
import com.diasemi.codelesslib.CodelessProfile.Command;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.regex.Pattern;

public class SpiConfigCommand extends CodelessCommand {
    public static final String TAG = "SpiConfigCommand";

    public static final String COMMAND = "SPICFG";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.SPICFG;

    public static final String PATTERN_STRING = "^SPICFG(?:=(\\d+),(\\d+),(\\d+))?$"; // <speed> <mode> <size>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int speed;
    private int mode;
    private int size;
    private boolean hasArguments;

    public SpiConfigCommand(CodelessManager manager, int speed, int mode, int size) {
        this(manager);
        setSpeed(speed);
        setMode(mode);
        setSize(size);
        hasArguments = true;
    }

    public SpiConfigCommand(CodelessManager manager) {
        super(manager);
    }

    public SpiConfigCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%d,%d", speed, mode, size) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                setSpeed(Integer.parseInt(matcher.group(0)));
                setMode(Integer.parseInt(matcher.group(1)));
                setSize(Integer.parseInt(matcher.group(2)));
            } catch (NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid SPI configuration: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "SPI configuration: speed=" + speed + " mode=" + mode + " size=" + size);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.SpiConfig(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",") ;
        return count == 0 || count == 3;
    }

    @Override
    protected String parseArguments() {
        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || (num != Command.SPI_CLOCK_VALUE_2_MHZ && num != Command.SPI_CLOCK_VALUE_4_MHZ && num != Command.SPI_CLOCK_VALUE_8_MHZ))
            return "Invalid SPI clock value";
        speed = num;

        num = decodeNumberArgument(2);
        if (num == null || (num != Command.SPI_MODE_0 && num != Command.SPI_MODE_1 && num != Command.SPI_MODE_2 && num != Command.SPI_MODE_3))
            return "Invalid SPI mode";
        mode = num;

        num = decodeNumberArgument(3);
        if (num == null || CodelessLibConfig.CHECK_SPI_WORD_SIZE && num != CodelessLibConfig.SPI_WORD_SIZE)
            return "Invalid SPI word size";
        size = num;

        return null;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        if (speed != Command.SPI_CLOCK_VALUE_2_MHZ && speed != Command.SPI_CLOCK_VALUE_4_MHZ && speed != Command.SPI_CLOCK_VALUE_8_MHZ)
            invalid = true;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        if (mode != Command.SPI_MODE_0 && mode != Command.SPI_MODE_1 && mode != Command.SPI_MODE_2 && mode != Command.SPI_MODE_3)
            invalid = true;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        if (CodelessLibConfig.CHECK_SPI_WORD_SIZE && size != CodelessLibConfig.SPI_WORD_SIZE)
            invalid = true;
    }
}
