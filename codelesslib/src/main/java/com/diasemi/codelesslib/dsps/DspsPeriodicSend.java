/*
 *******************************************************************************
 *
 * Copyright (C) 2020 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.diasemi.codelesslib.dsps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.diasemi.codelesslib.CodelessEvent;
import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import androidx.documentfile.provider.DocumentFile;

import static com.diasemi.codelesslib.CodelessLibConfig.CHARSET;
import static com.diasemi.codelesslib.CodelessLibConfig.DSPS_PATTERN_DIGITS;
import static com.diasemi.codelesslib.CodelessLibConfig.DSPS_PATTERN_SUFFIX;
import static com.diasemi.codelesslib.CodelessManager.SPEED_INVALID;

public class DspsPeriodicSend {
    private final static String TAG = "DspsPeriodicSend";

    private CodelessManager manager;
    private int period;
    private byte[] data;
    private int chunkSize;
    private boolean active;
    private int count;
    private boolean pattern;
    private int patternMaxCount;
    private int patternSentCount;
    private String patternFormat;
    private long startTime;
    private long endTime;
    private int bytesSent;
    private long lastInterval;
    private int bytesSentInterval;
    private int currentSpeed = SPEED_INVALID;

    public DspsPeriodicSend(CodelessManager manager, int period, byte[] data, int chunkSize) {
        this.manager = manager;
        this.period = period;
        this.data = data;
        this.chunkSize = chunkSize;
    }

    public DspsPeriodicSend(CodelessManager manager, int period, byte[] data) {
        this(manager, period, data, manager.getDspsChunkSize());
    }

    public DspsPeriodicSend(CodelessManager manager, int period, String text, int chunkSize) {
        this(manager, period, text.getBytes(CodelessLibConfig.CHARSET), chunkSize);
    }

    public DspsPeriodicSend(CodelessManager manager, int period, String text) {
        this(manager, period, text, manager.getDspsChunkSize());
    }

    private DspsPeriodicSend(CodelessManager manager, int chunkSize, int period) {
        this.manager = manager;
        this.chunkSize = Math.max(Math.min(chunkSize, manager.getDspsChunkSize()), DSPS_PATTERN_DIGITS + (DSPS_PATTERN_SUFFIX != null ? DSPS_PATTERN_SUFFIX.length : 0));
        this.period = period;
        pattern = true;
        patternMaxCount = (int) Math.pow(10, DSPS_PATTERN_DIGITS);
        patternFormat = "%0" + DSPS_PATTERN_DIGITS + "d";
    }

    public DspsPeriodicSend(CodelessManager manager, File file, int chunkSize, int period) {
        this(manager, chunkSize, period);
        loadPattern(file, null);
    }

    public DspsPeriodicSend(CodelessManager manager, File file, int period) {
        this(manager, file, manager.getDspsChunkSize(), period);
    }

    public DspsPeriodicSend(CodelessManager manager, Uri uri, int chunkSize, int period) {
        this(manager, chunkSize, period);
        loadPattern(null, uri);
    }

    public DspsPeriodicSend(CodelessManager manager, Uri uri, int period) {
        this(manager, uri, manager.getDspsChunkSize(), period);
    }

    public CodelessManager getManager() {
        return manager;
    }

    public int getPeriod() {
        return period;
    }

    public byte[] getData() {
        return data;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public boolean isActive() {
        return active;
    }

    public int getCount() {
        return count;
    }

    public void setResumeCount(int count) {
        this.count = count - 1;
    }

    public boolean isPattern() {
        return pattern;
    }

    public int getPatternMaxCount() {
        return patternMaxCount;
    }

    public int getPatternCount() {
        return (count - 1) % patternMaxCount;
    }

    public int getPatternSentCount() {
        return patternSentCount;
    }

    public void setPatternSentCount(int patternSentCount) {
        this.patternSentCount = patternSentCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getBytesSent() {
        return bytesSent;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public int getAverageSpeed() {
        long elapsed = (!active ? endTime : new Date().getTime()) - startTime;
        if (elapsed == 0)
            elapsed = 1;
        return (int) (bytesSent * 1000L / elapsed);
    }

    synchronized public void updateBytesSent(int bytes) {
        bytesSent += bytes;
        bytesSentInterval += bytes;
    }

    private Runnable updateStats = new Runnable() {
        @Override
        public void run() {
            if (!active)
                return;
            synchronized (DspsPeriodicSend.this) {
                long now = new Date().getTime();
                if (now == lastInterval)
                    now++;
                currentSpeed = (int) (bytesSentInterval * 1000L / (now - lastInterval));
                lastInterval = now;
                bytesSentInterval = 0;
                manager.getDspsStatsHandler().postDelayed(this, CodelessLibConfig.DSPS_STATS_INTERVAL);
                EventBus.getDefault().post(new CodelessEvent.DspsStats(manager, DspsPeriodicSend.this, currentSpeed, getAverageSpeed()));
            }
        }
    };

    public void start() {
        if (active)
            return;
        active = true;
        if (CodelessLibLog.DSPS)
            Log.d(TAG, manager.getLogPrefix() + "Start periodic send" + (pattern ? " (pattern)" : "") + ": period=" + period + "ms " + CodelessUtil.hexArrayLog(data));
        count = 0;
        startTime = new Date().getTime();
        if (CodelessLibConfig.DSPS_STATS) {
            lastInterval = startTime;
            manager.getDspsStatsHandler().postDelayed(updateStats, CodelessLibConfig.DSPS_STATS_INTERVAL);
        }
        manager.start(this);
    }

    public void stop() {
        active = false;
        if (CodelessLibLog.DSPS)
            Log.d(TAG, manager.getLogPrefix() + "Stop periodic send" + (pattern ? " (pattern)" : "") + ": period=" + period + "ms " + CodelessUtil.hexArrayLog(data));
        endTime = new Date().getTime();
        if (CodelessLibConfig.DSPS_STATS) {
            manager.getDspsStatsHandler().removeCallbacks(updateStats);
        }
        manager.stop(this);
    }

    public Runnable getRunnable() {
        return sendData;
    }

    private Runnable sendData = new Runnable() {
        @Override
        public void run() {
            count++;
            if (pattern) {
                byte[] patternBytes = String.format(patternFormat, getPatternCount()).getBytes(CHARSET);
                System.arraycopy(patternBytes, 0, data, data.length - DSPS_PATTERN_DIGITS - (DSPS_PATTERN_SUFFIX != null ? DSPS_PATTERN_SUFFIX.length : 0), DSPS_PATTERN_DIGITS);
            }
            if (CodelessLibLog.DSPS_PERIODIC_CHUNK)
                Log.d(TAG, manager.getLogPrefix() + "Queue periodic data (" + count + "): " + CodelessUtil.hexArrayLog(data));
            manager.sendData(DspsPeriodicSend.this);
            manager.getHandler().postDelayed(sendData, period);
        }
    };

    private void loadPattern(File file, Uri uri) {
        if (CodelessLibLog.DSPS)
            Log.d(TAG, "Load pattern: " + (file != null ? file.getAbsolutePath() : uri.toString()));

        if (Build.VERSION.SDK_INT >= 23 && file != null && manager.getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing storage permission");
            EventBus.getDefault().post(new CodelessEvent.DspsPatternFileError(manager, this, file, uri));
            return;
        }

        DocumentFile documentFile = file == null ? DocumentFile.fromSingleUri(manager.getContext(), uri) : null;
        InputStream inputStream = null;
        byte[] pattern;
        try {
            inputStream = file != null ? new FileInputStream(file) : manager.getContext().getContentResolver().openInputStream(uri);
            pattern = new byte[Math.min((int) (file != null ? file.length() : documentFile.length()), chunkSize - DSPS_PATTERN_DIGITS - (DSPS_PATTERN_SUFFIX != null ? DSPS_PATTERN_SUFFIX.length : 0))];
            inputStream.read(pattern);
        } catch (IOException | SecurityException e) {
            Log.e(TAG, "Failed to load pattern: " + (file != null ? file.getAbsolutePath() : uri.toString()), e);
            pattern = null;
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close pattern file: " + (file != null ? file.getAbsolutePath() : uri.toString()), e);
            }
        }

        if (pattern == null) {
            EventBus.getDefault().post(new CodelessEvent.DspsPatternFileError(manager, this, file, uri));
            return;
        }

        data = Arrays.copyOf(pattern, pattern.length + DSPS_PATTERN_DIGITS + (DSPS_PATTERN_SUFFIX != null ? DSPS_PATTERN_SUFFIX.length : 0));
        chunkSize = data.length;
        if (DSPS_PATTERN_SUFFIX != null)
            System.arraycopy(DSPS_PATTERN_SUFFIX, 0, data, data.length - DSPS_PATTERN_SUFFIX.length, DSPS_PATTERN_SUFFIX.length);
    }

    public boolean isLoaded() {
        return data != null;
    }
}
