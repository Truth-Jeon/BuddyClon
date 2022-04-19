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

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessProfile.Command.BINESC_TIME_AFTER_DEFAULT;
import static com.diasemi.codelesslib.CodelessProfile.Command.BINESC_TIME_PRIOR_DEFAULT;

public class BinEscCommand extends CodelessCommand {
    public static final String TAG = "BinEscCommand";

    public static final String COMMAND = "BINESC";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.BINESC;

    public static final String PATTERN_STRING = "^BINESC(?:=(\\d+),(0[xX][0-9a-fA-F]{1,6}|\\d+),(\\d+))?$"; // <time_prior> <seq> <time_after>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private static final String RESPONSE_PATTERN_STRING = "^(\\d+).([0-9a-fA-F]{1,6}).(\\d+)$"; // <time_prior> <seq> <time_after>
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(RESPONSE_PATTERN_STRING);

    private int sequence;
    private int timePrior;
    private int timeAfter;
    private boolean hasArguments;

    public BinEscCommand(CodelessManager manager, int sequence, int timePrior, int timeAfter) {
        this(manager);
        setSequence(sequence);
        setTimePrior(timePrior);
        setTimeAfter(timeAfter);
        hasArguments = true;
    }

    public BinEscCommand(CodelessManager manager, int sequence) {
        this(manager, sequence, BINESC_TIME_PRIOR_DEFAULT, BINESC_TIME_AFTER_DEFAULT);
    }

    public BinEscCommand(CodelessManager manager) {
        super(manager);
    }

    public BinEscCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%#x,%d", timePrior, sequence, timeAfter) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            Matcher matcher = RESPONSE_PATTERN.matcher(response);
            if (matcher.matches()) {
                try {
                    timePrior = Integer.parseInt(matcher.group(1));
                    sequence = Integer.parseInt(matcher.group(2), 16);
                    timeAfter = Integer.parseInt(matcher.group(3));
                } catch (NumberFormatException e) {
                    invalid = true;
                }
            } else {
                invalid = true;
            }
            if (sequence > 0xffffff || timePrior > 0xffff || timeAfter > 0xffff)
                invalid = true;
            if (invalid)
                Log.e(TAG, "Received invalid escape parameters: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Escape sequence: 0x" + Integer.toHexString(sequence) + " (\"" + getSequenceString() + "\") time=" + timePrior + "," + timeAfter);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.BinEsc(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 3;
    }

    @Override
    protected String parseArguments() {
        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || num > 0xffff)
            return "Invalid escape time prior";
        timePrior = num;

        num = decodeNumberArgument(2);
        if (num == null || num > 0xffffff)
            return "Invalid escape sequence";
        sequence = num;

        num = decodeNumberArgument(3);
        if (num == null || num > 0xffff)
            return "Invalid escape time after";
        timeAfter = num;

        return null;
    }

    public String getSequenceString() {
        byte[] sequenceBytes;
        if (sequence > 0xffff)
            sequenceBytes = new byte[] { (byte) sequence, (byte) (sequence >>> 8), (byte) (sequence >>> 16) };
        else if (sequence > 0xff)
            sequenceBytes = new byte[] { (byte) sequence, (byte) (sequence >>> 8) };
        else
            sequenceBytes = new byte[] { (byte) sequence };
        return new String(sequenceBytes, StandardCharsets.US_ASCII);
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence & 0xffffff;
    }

    public int getTimePrior() {
        return timePrior;
    }

    public void setTimePrior(int timePrior) {
        this.timePrior = timePrior & 0xffff;
    }

    public int getTimeAfter() {
        return timeAfter;
    }

    public void setTimeAfter(int timeAfter) {
        this.timeAfter = timeAfter & 0xffff;
    }
}
