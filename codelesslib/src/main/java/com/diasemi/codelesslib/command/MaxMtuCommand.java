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

import static com.diasemi.codelesslib.CodelessProfile.Command.MTU_MAX;
import static com.diasemi.codelesslib.CodelessProfile.Command.MTU_MIN;

public class MaxMtuCommand extends CodelessCommand {
    public static final String TAG = "MaxMtuCommand";

    public static final String COMMAND = "MAXMTU";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.MAXMTU;

    public static final String PATTERN_STRING = "^MAXMTU(?:=(\\d+))?$"; // <mtu>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int mtu;
    private boolean hasArguments;

    public MaxMtuCommand(CodelessManager manager, int mtu) {
        this(manager);
        setMtu(mtu);
        hasArguments = true;
    }

    public MaxMtuCommand(CodelessManager manager) {
        super(manager);
    }

    public MaxMtuCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? Integer.toString(mtu) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            try {
                setMtu(Integer.parseInt(response));
            } catch (NumberFormatException e) {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid mtu value: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "MTU=" + mtu);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.MaxMtu(this));
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

        Integer num = decodeNumberArgument(1);
        if (num == null || num < MTU_MIN || num > MTU_MAX)
            return "Invalid MTU value";
        mtu = num;

        return null;
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
        if (mtu < MTU_MIN || mtu > MTU_MAX)
            invalid = true;
    }
}
