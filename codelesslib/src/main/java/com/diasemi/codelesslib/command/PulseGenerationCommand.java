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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessLibConfig.CHECK_PWM_DURATION;
import static com.diasemi.codelesslib.CodelessLibConfig.CHECK_PWM_DUTY_CYCLE;
import static com.diasemi.codelesslib.CodelessLibConfig.CHECK_PWM_FREQUENCY;
import static com.diasemi.codelesslib.CodelessLibConfig.PWM_DURATION_MAX;
import static com.diasemi.codelesslib.CodelessLibConfig.PWM_DURATION_MIN;
import static com.diasemi.codelesslib.CodelessLibConfig.PWM_DUTY_CYCLE_MAX;
import static com.diasemi.codelesslib.CodelessLibConfig.PWM_DUTY_CYCLE_MIN;
import static com.diasemi.codelesslib.CodelessLibConfig.PWM_FREQUENCY_MAX;
import static com.diasemi.codelesslib.CodelessLibConfig.PWM_FREQUENCY_MIN;

public class PulseGenerationCommand extends CodelessCommand {
    public static final String TAG = "PulseGenerationCommand";

    public static final String COMMAND = "PWM";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.PWM;

    public static final String PATTERN_STRING = "^PWM(?:=(\\d+),(\\d+),(\\d+))?$"; // <frequency> <dc> <duration>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private static final String RESPONSE_PATTERN_STRING = "^(\\d+).(\\d+).(\\d+)$"; // <frequency> <dc> <duration>
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(RESPONSE_PATTERN_STRING);

    private int frequency;
    private int dutyCycle;
    private int duration;
    private boolean hasArguments;

    public PulseGenerationCommand(CodelessManager manager, int frequency, int dutyCycle, int duration) {
        this(manager);
        setFrequency(frequency);
        setDutyCycle(dutyCycle);
        setDuration(duration);
        hasArguments = true;
    }

    public PulseGenerationCommand(CodelessManager manager) {
        super(manager);
    }

    public PulseGenerationCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%d,%d", frequency, dutyCycle, duration) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            Matcher matcher = RESPONSE_PATTERN.matcher(response);
            if (matcher.matches()) {
                try {
                    setFrequency(Integer.parseInt(matcher.group(1)));
                    setDutyCycle(Integer.parseInt(matcher.group(2)));
                    setDuration(Integer.parseInt(matcher.group(3)));
                } catch (NumberFormatException e) {
                    invalid = true;
                }
            } else {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid PWM parameters: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "PWM parameters: frequency=" + frequency + " dc=" + dutyCycle + " duration=" + duration);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(!hasArguments ? new CodelessEvent.PwmStatus(this) : new CodelessEvent.PwmStart(this));
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
        if (num == null || CHECK_PWM_FREQUENCY && (num < PWM_FREQUENCY_MIN || num > PWM_FREQUENCY_MAX))
            return "Invalid pulse frequency";
        frequency = num;

        num = decodeNumberArgument(2);
        if (num == null || CHECK_PWM_DUTY_CYCLE && (num < PWM_DUTY_CYCLE_MIN || num > PWM_DUTY_CYCLE_MAX))
            return "Invalid pulse duty cycle";
        dutyCycle = num;

        num = decodeNumberArgument(3);
        if (num == null || CHECK_PWM_DURATION && (num < PWM_DURATION_MIN || num > PWM_DURATION_MAX))
            return "Invalid pulse duration";
        duration = num;

        return null;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
        if (CHECK_PWM_FREQUENCY) {
            if (frequency < PWM_FREQUENCY_MIN || frequency > PWM_FREQUENCY_MAX)
                invalid = true;
        }
    }

    public int getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(int dutyCycle) {
        this.dutyCycle = dutyCycle;
        if (CHECK_PWM_DUTY_CYCLE) {
            if (dutyCycle < PWM_DUTY_CYCLE_MIN || dutyCycle > PWM_DUTY_CYCLE_MAX)
                invalid = true;
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        if (CHECK_PWM_DURATION) {
            if (duration < PWM_DURATION_MIN || duration > PWM_DURATION_MAX)
                invalid = true;
        }
    }
}
