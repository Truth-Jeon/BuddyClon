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

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import static com.diasemi.codelesslib.CodelessManager.SPEED_INVALID;

public class DspsFileSend {
    private final static String TAG = "DspsFileSend";

    private CodelessManager manager;
    private File file;
    private Uri uri;
    private DocumentFile documentFile;
    private String fileName;
    private int chunkSize;
    private byte[][] chunks;
    private int chunk;
    private int sentChunks;
    private int totalChunks;
    private int period;
    private boolean started;
    private boolean complete;
    private long startTime;
    private long endTime;
    private int bytesSent;
    private long lastInterval;
    private int bytesSentInterval;
    private int currentSpeed = SPEED_INVALID;

    private DspsFileSend(CodelessManager manager, int chunkSize, int period) {
        this.manager = manager;
        this.chunkSize = Math.min(chunkSize, manager.getDspsChunkSize());
        this.period = period;
    }

    public DspsFileSend(CodelessManager manager, File file, int chunkSize, int period) {
        this(manager, chunkSize, period);
        this.file = file;
        fileName = file.getName();
        loadFile();
    }

    public DspsFileSend(CodelessManager manager, File file, int period) {
        this(manager, file, manager.getDspsChunkSize(), period);
    }

    public DspsFileSend(CodelessManager manager, File file) {
        this(manager, file, manager.getDspsChunkSize(), -1);
    }

    public DspsFileSend(CodelessManager manager, Uri uri, int chunkSize, int period) {
        this(manager, chunkSize, period);
        this.uri = uri;
        documentFile = DocumentFile.fromSingleUri(manager.getContext(), uri);
        fileName = documentFile.getName();
        loadFile();
    }

    public DspsFileSend(CodelessManager manager, Uri uri, int period) {
        this(manager, uri, manager.getDspsChunkSize(), period);
    }

    public DspsFileSend(CodelessManager manager, Uri uri) {
        this(manager, uri, manager.getDspsChunkSize(), -1);
    }

    public CodelessManager getManager() {
        return manager;
    }

    public File getFile() {
        return file;
    }

    public Uri getUri() {
        return uri;
    }

    public DocumentFile getDocumentFile() {
        return documentFile;
    }

    public String getFileName() {
        return fileName;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public byte[][] getChunks() {
        return chunks;
    }

    public byte[] getCurrentChunk() {
        return chunks[chunk];
    }

    public int getChunk() {
        return chunk;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public void setResumeChunk(int chunk) {
        setChunk(period > 0 ? Math.min(chunk - 1, this.chunk) : chunk);
    }

    public int getSentChunks() {
        return sentChunks;
    }

    public void setSentChunks(int sentChunks) {
        this.sentChunks = sentChunks;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public int getPeriod() {
        return period;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete() {
        complete = true;
        endTime = new Date().getTime();
        if (CodelessLibConfig.DSPS_STATS) {
            synchronized (this) {
                manager.getDspsStatsHandler().removeCallbacks(updateStats);
                EventBus.getDefault().post(new CodelessEvent.DspsStats(manager, this, currentSpeed, getAverageSpeed()));
            }
        }
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
        long elapsed = (complete ? endTime : new Date().getTime()) - startTime;
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
            if (complete)
                return;
            synchronized (DspsFileSend.this) {
                long now = new Date().getTime();
                if (now == lastInterval)
                    now++;
                currentSpeed = (int) (bytesSentInterval * 1000L / (now - lastInterval));
                lastInterval = now;
                bytesSentInterval = 0;
                manager.getDspsStatsHandler().postDelayed(this, CodelessLibConfig.DSPS_STATS_INTERVAL);
                EventBus.getDefault().post(new CodelessEvent.DspsStats(manager, DspsFileSend.this, currentSpeed, getAverageSpeed()));
            }
        }
    };

    private void loadFile() {
        if (CodelessLibLog.DSPS)
            Log.d(TAG, "Load file: " + (file != null ? file.getAbsolutePath() : uri.toString()));

        if (Build.VERSION.SDK_INT >= 23 && file != null && manager.getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing storage permission");
            EventBus.getDefault().post(new CodelessEvent.DspsFileError(manager, this));
            return;
        }

        InputStream inputStream = null;
        byte[] data;
        try {
            inputStream = file != null ? new FileInputStream(file) : manager.getContext().getContentResolver().openInputStream(uri);
            data = new byte[(int) (file != null ? file.length() : documentFile.length())];
            inputStream.read(data);
        } catch (IOException | SecurityException e) {
            Log.e(TAG, "Failed to load file: " + (file != null ? file.getAbsolutePath() : uri.toString()), e);
            data = null;
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close file: " + (file != null ? file.getAbsolutePath() : uri.toString()), e);
            }
        }

        if (data == null || data.length == 0) {
            EventBus.getDefault().post(new CodelessEvent.DspsFileError(manager, this));
            return;
        }

        totalChunks = data.length / chunkSize + (data.length % chunkSize != 0 ? 1 : 0);
        chunks = new byte[totalChunks][];
        for (int i = 0; i < data.length; i += chunkSize) {
            chunks[i / chunkSize] = Arrays.copyOfRange(data, i, Math.min(i + chunkSize, data.length));
        }
    }

    public boolean isLoaded() {
        return chunks != null;
    }

    public void start() {
        if (started)
            return;
        started = true;
        if (CodelessLibLog.DSPS)
            Log.d(TAG, manager.getLogPrefix() + "Start file send: " + this);
        chunk = -1;
        startTime = new Date().getTime();
        if (CodelessLibConfig.DSPS_STATS) {
            lastInterval = startTime;
            manager.getDspsStatsHandler().postDelayed(updateStats, CodelessLibConfig.DSPS_STATS_INTERVAL);
        }
        manager.start(this, false);
    }

    public void stop() {
        if (CodelessLibLog.DSPS)
            Log.d(TAG, manager.getLogPrefix() + "Stop file send: " + this);
        endTime = new Date().getTime();
        if (CodelessLibConfig.DSPS_STATS) {
            manager.getDspsStatsHandler().removeCallbacks(updateStats);
        }
        manager.stop(this);
    }

    public Runnable getRunnable() {
        return sendChunk;
    }

    private Runnable sendChunk = new Runnable() {
        @Override
        public void run() {
            chunk++;
            if (CodelessLibLog.DSPS_FILE_CHUNK)
                Log.d(TAG, manager.getLogPrefix() + "Queue file chunk: " + DspsFileSend.this + " " + (chunk + 1) + " of " + totalChunks);
            manager.sendData(DspsFileSend.this);
            if (chunk < totalChunks - 1)
                manager.getHandler().postDelayed(sendChunk, period);
        }
    };

    @NonNull
    @Override
    public String toString() {
        return getFileName();
    }
}
