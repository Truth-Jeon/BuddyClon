/*
 *******************************************************************************
 *
 * Copyright (C) 2020 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.diasemi.codelesslib.log;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.dsps.DspsFileReceive;

import java.io.File;
import java.util.Date;

public abstract class LogFileBase {
    private static final String TAG = "LogFileBase";

    protected Context context;
    protected String name;
    protected File file;
    protected boolean closed;

    public LogFileBase(CodelessManager manager, String prefix) {
        context = manager.getContext();
        name = prefix + CodelessLibConfig.LOG_FILE_DATE.format(new Date());
        if (CodelessLibConfig.LOG_FILE_ADDRESS_SUFFIX)
            name += "_" + manager.getDevice().getAddress().replaceAll(":", "");
        name += CodelessLibConfig.LOG_FILE_EXTENSION;
        File path = new File(CodelessLibConfig.LOG_FILE_PATH);
        file = new File(path, name);
        createPath(path);
    }

    public LogFileBase(DspsFileReceive dspsFileReceive) {
        context = dspsFileReceive.getManager().getContext();
        name = dspsFileReceive.getName();
        File path = new File(CodelessLibConfig.DSPS_RX_FILE_PATH);
        file = new File(path, name);
        createPath(path);
    }

    private void createPath(File path) {
        if (Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(getTag(), "Missing storage permission");
            closed = true;
        } else {
            try {
                if (!path.exists() && !path.mkdirs())
                    closed = true;
            } catch (SecurityException e) {
                Log.e(getTag(), "Failed to create log path: " + path.getAbsolutePath());
                closed = true;
            }
        }
    }

    protected String getTag() {
        return TAG;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public boolean isClosed() {
        return closed;
    }
}
