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
import com.diasemi.codelesslib.CodelessProfile.GPIO;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.regex.Pattern;

public class IoStatusCommand extends CodelessCommand {
    public static final String TAG = "IoStatusCommand";

    public static final String COMMAND = "IO";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.IO;

    public static final String PATTERN_STRING = "^IO=(\\d+)(?:,(\\d))?$"; // <pin> <status>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private GPIO gpio;

    public IoStatusCommand(CodelessManager manager, GPIO gpio) {
        this(manager);
        setGpio(gpio);
    }

    public IoStatusCommand(CodelessManager manager, GPIO gpio, boolean status) {
        this(manager, gpio);
        gpio.setStatus(status);
    }

    public IoStatusCommand(CodelessManager manager, int port, int pin) {
        this(manager, new GPIO(port, pin));
    }

    public IoStatusCommand(CodelessManager manager, int port, int pin, boolean status) {
        this(manager, new GPIO(port, pin), status);
    }

    public IoStatusCommand(CodelessManager manager) {
        super(manager);
    }

    public IoStatusCommand(CodelessManager manager, String command, boolean parse) {
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
        String arguments = String.format(Locale.US, "%d", gpio.getGpio());
        if (gpio.validState())
            arguments += String.format(Locale.US, ",%d", gpio.state);
        return arguments;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                gpio.state = Integer.parseInt(response);
                if (!gpio.isBinary())
                    invalid = true;
            } catch (NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid GPIO status: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "GPIO status: " + gpio.name() + (gpio.isHigh() ? " high" : " low"));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.IoStatus(this));
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 1 || count == 2;
    }

    @Override
    protected String parseArguments() {
        gpio = new GPIO();

        Integer num = decodeNumberArgument(1);
        if (num == null)
            return "Invalid GPIO";
        gpio.setGpio(num);

        if (CodelessProfile.countArguments(command, ",") == 2) {
            num = decodeNumberArgument(2);
            if (num == null || !Command.isBinaryState(num))
                return "Argument must be 0 or 1";
            gpio.state = num;
        }

        return null;
    }

    public GPIO getGpio() {
        return gpio;
    }

    public void setGpio(GPIO gpio) {
        this.gpio = gpio;
        if (gpio.validState() && !gpio.isBinary())
            invalid = true;
    }

    public boolean getStatus() {
        return gpio.isHigh();
    }

    public void setStatus(boolean status) {
        gpio.setStatus(status);
    }
}
