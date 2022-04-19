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

import java.util.Locale;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessProfile.Command.DISABLE_UART_FLOW_CONTROL;
import static com.diasemi.codelesslib.CodelessProfile.Command.ENABLE_UART_FLOW_CONTROL;
import static com.diasemi.codelesslib.CodelessProfile.Command.GPIO_FUNCTION_UART_CTS;
import static com.diasemi.codelesslib.CodelessProfile.Command.GPIO_FUNCTION_UART_RTS;

public class FlowControlCommand extends CodelessCommand {
    public static final String TAG = "FlowControlCommand";

    public static final String COMMAND = "FLOWCONTROL";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.FLOWCONTROL;

    public static final String PATTERN_STRING = "^FLOWCONTROL(?:=(\\d),(\\d+),(\\d+))?$"; // <fc_mode> <rts_pin> <cts_pin>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int mode;
    private GPIO rtsGpio;
    private GPIO ctsGpio;
    private boolean hasArguments;

    public FlowControlCommand(CodelessManager manager, boolean enabled, GPIO rtsGpio, GPIO ctsGpio) {
        this(manager, enabled ? ENABLE_UART_FLOW_CONTROL : DISABLE_UART_FLOW_CONTROL, rtsGpio, ctsGpio);
    }

    public FlowControlCommand(CodelessManager manager, int mode, GPIO rtsGpio, GPIO ctsGpio) {
        this(manager);
        setMode(mode);
        setRtsGpio(rtsGpio);
        setCtsGpio(ctsGpio);
        hasArguments = true;
    }

    public FlowControlCommand(CodelessManager manager) {
        super(manager);
    }

    public FlowControlCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%d,%d", mode, rtsGpio.getGpio(), ctsGpio.getGpio()) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                String[] values = response.split(" ");
                mode = Integer.parseInt(values[0]);
                if (mode != DISABLE_UART_FLOW_CONTROL && mode != ENABLE_UART_FLOW_CONTROL)
                    invalid = true;

                rtsGpio = new GPIO();
                rtsGpio.setGpio(Integer.parseInt(values[1]));
                rtsGpio.function = GPIO_FUNCTION_UART_RTS;

                ctsGpio = new GPIO();
                ctsGpio.setGpio(Integer.parseInt(values[2]));
                ctsGpio.function = GPIO_FUNCTION_UART_CTS;
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid flow control response: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Flow control: " + (mode == ENABLE_UART_FLOW_CONTROL ? "Enabled" : "Disabled") + " RTS=" + rtsGpio.name() + " CTS=" + ctsGpio.name());
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.FlowControl(this));
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
        if (num == null || mode != DISABLE_UART_FLOW_CONTROL && mode != ENABLE_UART_FLOW_CONTROL)
            return "Invalid mode";
        mode = num;

        num = decodeNumberArgument(2);
        if (num == null)
            return "Invalid RTS GPIO";
        rtsGpio = new GPIO();
        rtsGpio.setGpio(num);
        rtsGpio.function = GPIO_FUNCTION_UART_RTS;

        num = decodeNumberArgument(3);
        if (num == null)
            return "Invalid CTS GPIO";
        ctsGpio = new GPIO();
        ctsGpio.setGpio(num);
        ctsGpio.function = GPIO_FUNCTION_UART_CTS;

        return null;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        if (mode != DISABLE_UART_FLOW_CONTROL && mode != ENABLE_UART_FLOW_CONTROL)
            invalid = true;
    }

    public boolean isEnabled() {
        return mode != DISABLE_UART_FLOW_CONTROL;
    }

    public void setEnabled(boolean enabled) {
        mode = enabled ? ENABLE_UART_FLOW_CONTROL : DISABLE_UART_FLOW_CONTROL;
    }

    public GPIO getRtsGpio() {
        return rtsGpio;
    }

    public void setRtsGpio(GPIO rtsGpio) {
        this.rtsGpio = rtsGpio;
        if (rtsGpio.function != GPIO_FUNCTION_UART_RTS)
            invalid = true;
    }

    public GPIO getCtsGpio() {
        return ctsGpio;
    }

    public void setCtsGpio(GPIO ctsGpio) {
        this.ctsGpio = ctsGpio;
        if (ctsGpio.function != GPIO_FUNCTION_UART_CTS)
            invalid = true;
    }
}
