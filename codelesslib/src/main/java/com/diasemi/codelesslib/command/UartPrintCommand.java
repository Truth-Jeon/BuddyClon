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

import java.util.regex.Pattern;

public class UartPrintCommand extends CodelessCommand {
    public static final String TAG = "UartPrintCommand";

    public static final String COMMAND = "PRINT";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.PRINT;

    public static final String PATTERN_STRING = "^PRINT=(.*)$"; // <text>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private String text;

    public UartPrintCommand(CodelessManager manager, String text) {
        this(manager);
        this.text = text;
    }

    public UartPrintCommand(CodelessManager manager) {
        super(manager);
    }

    public UartPrintCommand(CodelessManager manager, String command, boolean parse) {
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
        return text;
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        return CodelessProfile.countArguments(command, "\n") == 1;
    }

    @Override
    protected String parseArguments() {
        text = matcher.group(1);
        return null;
    }

    @Override
    public void processInbound() {
        if (CodelessLibLog.COMMAND)
            Log.d(TAG, "Received print command: " + text);
        sendSuccess();
        EventBus.getDefault().post(new CodelessEvent.Print(this));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
