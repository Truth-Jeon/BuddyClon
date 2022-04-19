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
import com.diasemi.codelesslib.CodelessProfile.GPIO;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessLibConfig.ANALOG_INPUT_GPIO;
import static com.diasemi.codelesslib.CodelessLibConfig.CHECK_ANALOG_INPUT_GPIO;

public class AdcReadCommand extends CodelessCommand {
    public static final String TAG = "AdcReadCommand";

    public static final String COMMAND = "ADC";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.ADC;

    public static final String PATTERN_STRING = "^ADC=(\\d+)$"; // <pin>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private GPIO gpio;

    public AdcReadCommand(CodelessManager manager, GPIO gpio) {
        this(manager);
        setGpio(gpio);
    }

    public AdcReadCommand(CodelessManager manager, int port, int pin) {
        this(manager, new GPIO(port, pin));
    }

    public AdcReadCommand(CodelessManager manager) {
        super(manager);
    }

    public AdcReadCommand(CodelessManager manager, String command, boolean parse) {
        super(manager, command, parse);
        if (gpio == null)
            gpio = new GPIO();
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
        return Integer.toString(gpio.getGpio());
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                gpio.state = Integer.parseInt(response);
                if (CodelessLibLog.COMMAND)
                    Log.d(TAG, "ADC: " + gpio.name() + " " + gpio.state);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Received invalid ADC result: " + response);
                invalid = true;
            }
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.AnalogRead(this));
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
        gpio = new GPIO();
        Integer num = decodeNumberArgument(1);
        if (num == null || CHECK_ANALOG_INPUT_GPIO && !Arrays.asList(ANALOG_INPUT_GPIO).contains(new GPIO(num)))
            return "Invalid ADC GPIO";
        gpio.setGpio(num);
        return null;
    }

    public GPIO getGpio() {
        return gpio;
    }

    public void setGpio(GPIO gpio) {
        this.gpio = gpio;
        gpio.state = GPIO.INVALID;
        if (CHECK_ANALOG_INPUT_GPIO && !Arrays.asList(ANALOG_INPUT_GPIO).contains(gpio))
            invalid = true;
    }

    public int getState() {
        return gpio.state;
    }
}
