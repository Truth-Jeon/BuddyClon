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

import android.content.pm.PackageManager;
import android.util.Log;

import com.diasemi.codelesslib.CodelessEvent;
import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

public class DeviceInformationCommand extends CodelessCommand {
    public static final String TAG = "DeviceInformationCommand";

    public static final String COMMAND = "I";
    public static final String NAME = CodelessProfile.PREFIX + COMMAND;
    public static final CommandID ID = CommandID.ATI;

    public static final String PATTERN_STRING = "^I$";
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private String info;

    public DeviceInformationCommand(CodelessManager manager) {
        super(manager);
    }

    public DeviceInformationCommand(CodelessManager manager, String command, boolean parse) {
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
            info = response;
            if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Device info: " + info);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        EventBus.getDefault().post(new CodelessEvent.DeviceInformation(this));
    }

    @Override
    public void processInbound() {
        if (info == null) {
            if (CodelessLibConfig.CODELESS_LIB_INFO != null) {
                info = CodelessLibConfig.CODELESS_LIB_INFO;
            } else {
                info = "CodeLess Android";
                try {
                    info += " " + manager.getContext().getPackageManager().getPackageInfo(manager.getContext().getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {}
            }
        }
        if (CodelessLibLog.COMMAND)
            Log.d(TAG, "Send device info: " + info);
        sendSuccess(info);
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
