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

import java.util.Random;
import java.util.regex.Pattern;

public class RandomNumberCommand extends CodelessCommand {
    public static final String TAG = "RandomNumberCommand";

    public static final String COMMAND = "RANDOM";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.RANDOM;

    public static final String PATTERN_STRING = "^RANDOM$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private static final Random random = new Random();

    private long number = Long.MIN_VALUE;

    public RandomNumberCommand(CodelessManager manager) {
        super(manager);
    }

    public RandomNumberCommand(CodelessManager manager, String command, boolean parse) {
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
            try {
                number = Long.decode(response);
                if (CodelessLibLog.COMMAND)
                    Log.d(TAG, "Random number: " + number);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Received invalid random number: " + response, e);
                invalid = true;
            }
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.RandomNumber(this));
    }

    @Override
    public void processInbound() {
        if (number == Long.MIN_VALUE)
            initRandomNumber();
        if (CodelessLibLog.COMMAND)
            Log.d(TAG, "Send random number: " + number);
        sendSuccess(String.format("0x%08X", number));
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public void initRandomNumber() {
        number = random.nextLong() & 0xffffffffL;
    }
}
