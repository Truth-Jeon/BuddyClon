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

import android.util.Log;

import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.dsps.DspsFileReceive;

import java.io.FileOutputStream;
import java.io.IOException;

public class DspsRxLogFile extends LogFileBase {
    private static final String TAG = "DspsRxLogFile";

    private FileOutputStream output;

    public DspsRxLogFile(CodelessManager manager) {
        super(manager, CodelessLibConfig.DSPS_RX_LOG_FILE_PREFIX);
    }

    public DspsRxLogFile(DspsFileReceive dspsFileReceive) {
        super(dspsFileReceive);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    protected boolean create() {
        try {
            output = new FileOutputStream(file);
        } catch (IOException | SecurityException e) {
            Log.e(TAG, "Failed to create file: " + name, e);
            closed = true;
        }
        return !closed;
    }

    public void log(byte[] data) {
        if (closed)
            return;
        if (output == null && !create())
            return;
        try {
            output.write(data);
            if (CodelessLibConfig.DSPS_RX_LOG_FLUSH)
                output.flush();
        } catch (IOException e) {
            Log.e(TAG, "Write failed: " + name, e);
            closed = true;
        }
    }

    public void close() {
        try {
            if (output != null)
                output.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close file: " + name, e);
        }
        closed = true;
    }
}
