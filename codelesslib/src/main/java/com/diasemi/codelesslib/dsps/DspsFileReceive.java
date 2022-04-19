package com.diasemi.codelesslib.dsps;

import android.util.Log;

import com.diasemi.codelesslib.CodelessEvent;
import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.log.DspsRxLogFile;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.zip.CRC32;

import static com.diasemi.codelesslib.CodelessManager.SPEED_INVALID;

public class DspsFileReceive {
    private final static String TAG = "DspsFileReceive";

    private CodelessManager manager;
    private byte[] header;
    private String name;
    private int size;
    private long crc = -1;
    private DspsRxLogFile file;
    private int bytesReceived;
    private CRC32 crc32;
    private boolean started;
    private boolean complete;
    private long startTime;
    private long endTime;
    private long lastInterval;
    private int bytesReceivedInterval;
    private int currentSpeed = SPEED_INVALID;

    public DspsFileReceive(CodelessManager manager) {
        this.manager = manager;
    }

    public CodelessManager getManager() {
        return manager;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public long getCrc() {
        return crc;
    }

    public boolean hasCrc() {
        return crc != -1;
    }

    public boolean crcOk() {
        return crc32 != null && crc32.getValue() == crc;
    }

    public DspsRxLogFile getFile() {
        return file;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isComplete() {
        return complete;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public int getAverageSpeed() {
        long elapsed = (complete ? endTime : new Date().getTime()) - startTime;
        if (elapsed == 0)
            elapsed = 1;
        return (int) (bytesReceived * 1000L / elapsed);
    }

    private Runnable updateStats = new Runnable() {
        @Override
        public void run() {
            if (complete)
                return;
            synchronized (DspsFileReceive.this) {
                long now = new Date().getTime();
                if (now == lastInterval)
                    now++;
                currentSpeed = (int) (bytesReceivedInterval * 1000L / (now - lastInterval));
                lastInterval = now;
                bytesReceivedInterval = 0;
                manager.getDspsStatsHandler().postDelayed(this, CodelessLibConfig.DSPS_STATS_INTERVAL);
                EventBus.getDefault().post(new CodelessEvent.DspsStats(manager, DspsFileReceive.this, currentSpeed, getAverageSpeed()));
            }
        }
    };

    public void start() {
        if (started)
            return;
        started = true;
        if (CodelessLibLog.DSPS)
            Log.d(TAG, manager.getLogPrefix() + "Start file receive");
        manager.start(this);
    }

    public void stop() {
        if (CodelessLibLog.DSPS)
            Log.d(TAG, manager.getLogPrefix() + "Stop file receive");
        endTime = new Date().getTime();
        if (file != null)
            file.close();
        if (CodelessLibConfig.DSPS_STATS) {
            manager.getDspsStatsHandler().removeCallbacks(updateStats);
        }
        manager.stop(this);
    }

    public void onDspsData(byte[] data) {
        if (!started)
            return;

        // Check for header
        if (file == null) {
            if (header == null) {
                header = data;
            } else {
                header = Arrays.copyOf(header, header.length + data.length);
                System.arraycopy(data, 0, header, header.length - data.length, data.length);
            }
            String headerText = new String(header, StandardCharsets.US_ASCII);
            Matcher matcher = CodelessLibConfig.DSPS_RX_FILE_HEADER_PATTERN.matcher(headerText);
            if (matcher.matches()) {
                name = matcher.group(2);
                try {
                    size = Integer.decode(matcher.group(3));
                    if (matcher.group(4) != null)
                        crc = Long.parseLong(matcher.group(4), 16);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "File header parse failure", e);
                }
                int start = matcher.end(1);
                int end = matcher.start(5);

                data = Arrays.copyOfRange(header, end, header.length);
                header = Arrays.copyOfRange(header, start, end);

                if (CodelessLibLog.DSPS)
                    Log.d(TAG, manager.getLogPrefix() + "File receive: " + name + " size=" + size + (crc != -1 ? " crc=" + Long.toHexString(crc).toUpperCase() : ""));
                startTime = new Date().getTime();
                if (CodelessLibConfig.DSPS_STATS) {
                    lastInterval = startTime;
                    manager.getDspsStatsHandler().postDelayed(updateStats, CodelessLibConfig.DSPS_STATS_INTERVAL);
                }

                file = new DspsRxLogFile(this);
                if (crc != -1)
                    crc32 = new CRC32();
                EventBus.getDefault().post(new CodelessEvent.DspsRxFileData(manager, this, size, bytesReceived));
            } else if (!matcher.hitEnd()) {
                header = null;
            }
        }

        if (file == null)
            return;

        // Write data to file
        if (data.length > size - bytesReceived)
            data = Arrays.copyOf(data, size - bytesReceived);
        synchronized (this) {
            bytesReceived += data.length;
            bytesReceivedInterval += data.length;
        }

        if (CodelessLibLog.DSPS_FILE_CHUNK)
            Log.d(TAG, manager.getLogPrefix() + "File receive: " + name + " " + bytesReceived + " of " + size);
        file.log(data);
        if (crc != -1)
            crc32.update(data);

        if (bytesReceived == size) {
            if (CodelessLibLog.DSPS)
                Log.d(TAG, manager.getLogPrefix() + "File received: " + name);
            complete = true;
            endTime = new Date().getTime();
            if (CodelessLibConfig.DSPS_STATS) {
                synchronized (this) {
                    manager.getDspsStatsHandler().removeCallbacks(updateStats);
                    EventBus.getDefault().post(new CodelessEvent.DspsStats(manager, this, currentSpeed, getAverageSpeed()));
                }
            }
            file.close();
            manager.stop(this);
        }

        EventBus.getDefault().post(new CodelessEvent.DspsRxFileData(manager, this, size, bytesReceived));
        if (complete && crc != -1) {
            boolean ok = crc == crc32.getValue();
            Log.d(TAG, manager.getLogPrefix() + "Received file CRC " + (ok ? "OK" : "error") + ": " + name);
            EventBus.getDefault().post(new CodelessEvent.DspsRxFileCrc(manager, this, ok));
        }
    }
}
