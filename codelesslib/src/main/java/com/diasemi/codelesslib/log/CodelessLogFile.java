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
import com.diasemi.codelesslib.CodelessProfile;

import java.io.IOException;
import java.io.PrintWriter;

public class CodelessLogFile extends LogFileBase {
    private static final String TAG = "CodelessLogFile";

    private PrintWriter writer;

    public CodelessLogFile(CodelessManager manager) {
        super(manager, CodelessLibConfig.CODELESS_LOG_FILE_PREFIX);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    protected boolean create() {
        try {
            writer = new PrintWriter(file);
        } catch (IOException | SecurityException e) {
            Log.e(TAG, "Failed to create file: " + name, e);
            closed = true;
        }
        return !closed;
    }

    public void log(String line) {
        if (closed)
            return;
        if (writer == null && !create())
            return;
        writer.println(line);
        if (CodelessLibConfig.CODELESS_LOG_FLUSH)
            writer.flush();
    }

    public void log(CodelessProfile.Line line) {
        log((line.getType().isOutbound() ? CodelessLibConfig.CODELESS_LOG_PREFIX_OUTBOUND : CodelessLibConfig.CODELESS_LOG_PREFIX_INBOUND) + line.getText());
    }

    public void logText(String text) {
        log(CodelessLibConfig.CODELESS_LOG_PREFIX_TEXT + text);
    }

    public void close() {
        if (writer != null)
            writer.close();
        closed = true;
    }
}
