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
import com.diasemi.codelesslib.CodelessProfile.GPIO;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessLibConfig.CHECK_GPIO_FUNCTION;
import static com.diasemi.codelesslib.CodelessLibConfig.GPIO_FUNCTION_MAX;
import static com.diasemi.codelesslib.CodelessLibConfig.GPIO_FUNCTION_MIN;

public class IoConfigCommand extends CodelessCommand {
    public static final String TAG = "IoConfigCommand";

    public static final String COMMAND = "IOCFG";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.IOCFG;

    public static final String PATTERN_STRING = "^IOCFG(?:=(\\d+),(\\d+)(?:,(\\d+))?)?$"; // <pin> <function> <level>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private GPIO gpio;
    private ArrayList<GPIO> configuration;
    private boolean hasArguments;

    public IoConfigCommand(CodelessManager manager, GPIO gpio) {
        this(manager);
        this.gpio = gpio;
        setFunction(gpio.function);
        hasArguments = true;
    }

    public IoConfigCommand(CodelessManager manager, int port, int pin, int function) {
        this(manager, new GPIO(port, pin, function));
    }

    public IoConfigCommand(CodelessManager manager, int port, int pin, int function, int level) {
        this(manager, new GPIO(port, pin, function, level));
    }

    public IoConfigCommand(CodelessManager manager) {
        super(manager);
    }

    public IoConfigCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments;
    }

    @Override
    protected String getArguments() {
        if (!hasArguments)
            return null;
        String arguments = String.format(Locale.US, "%d,%d", gpio.getGpio(), gpio.function);
        if (gpio.validLevel())
            arguments += String.format(Locale.US, ",%d", gpio.level);
        return arguments;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            configuration = new ArrayList<>();

            // Get GPIO function from response
            String[] gpioFunction = response.split(" ");
            int[] function = new int[gpioFunction.length];
            try {
                for (int i = 0; i < gpioFunction.length; i++) {
                    function[i] = Integer.parseInt(gpioFunction[i]);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Received invalid GPIO configuration: " + response);
                invalid = true;
                return;
            }

            // Find GPIO configuration (based on number of pins)
            GPIO[] gpioConfig = null;
            for (GPIO[] config : CodelessLibConfig.GPIO_CONFIGURATIONS) {
                if (function.length == config.length) {
                    gpioConfig = config;
                    break;
                }
            }
            if (gpioConfig == null) {
                Log.e(TAG, "Unknown GPIO configuration: " + response);
                invalid = true;
                return;
            }

            if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Using GPIO configuration: " + Arrays.toString(gpioConfig));
            for (int i = 0; i < function.length; i++) {
                if (gpioConfig[i] != null)
                    configuration.add(new GPIO(gpioConfig[i], function[i]));
            }
            if (CodelessLibLog.COMMAND)
                Log.d(TAG, "GPIO configuration: " + configuration);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(!hasArguments ? new CodelessEvent.IoConfig(this) : new CodelessEvent.IoConfigSet(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 2 || count == 3;
    }

    @Override
    protected String parseArguments() {
        gpio = new GPIO();

        int count = CodelessProfile.countArguments(command, ",");
        if (count == 0)
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null)
            return "Invalid GPIO";
        gpio.setGpio(num);

        num = decodeNumberArgument(2);
        if (num == null || CHECK_GPIO_FUNCTION && (num < GPIO_FUNCTION_MIN || num > GPIO_FUNCTION_MAX))
            return "Invalid GPIO function";
        gpio.function = num;

        if (count == 3) {
            num = decodeNumberArgument(3);
            if (num == null)
                return "Invalid level";
            gpio.level = num;
        }

        return null;
    }

    public GPIO getGpio() {
        return gpio;
    }

    public void setGpio(GPIO gpio) {
        this.gpio = gpio;
    }

    public void setGpio(int pack) {
        gpio.setGpio(pack);
    }

    public void setGpio(int port, int pin) {
        gpio.setGpio(port, pin);
    }

    public int getPort() {
        return gpio.port;
    }

    public void setPort(int port) {
        gpio.port = port;
    }

    public int getPin() {
        return gpio.pin;
    }

    public void setPin(int pin) {
        gpio.pin = pin;
    }

    public int getFunction() {
        return gpio.function;
    }

    public void setFunction(int function) {
        gpio.function = function;
        if (CHECK_GPIO_FUNCTION && (function < GPIO_FUNCTION_MIN || function > GPIO_FUNCTION_MAX))
            invalid = true;
    }

    public int getLevel() {
        return gpio.level;
    }

    public void setLevel(int level) {
        gpio.level = level;
    }

    public ArrayList<GPIO> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ArrayList<GPIO> configuration) {
        this.configuration = configuration;
    }
}
