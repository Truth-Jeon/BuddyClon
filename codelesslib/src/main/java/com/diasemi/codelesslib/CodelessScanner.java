/*
 *******************************************************************************
 *
 * Copyright (C) 2020 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.diasemi.codelesslib;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.diasemi.codelesslib.CodelessProfile.Uuid;
import com.diasemi.codelesslib.misc.RuntimePermissionChecker;

import org.greenrobot.eventbus.EventBus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AlertDialog;

public class CodelessScanner {
    private final static String TAG = "CodelessScanner";

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private ScannerApi scannerApi;
    private boolean scanning = false;
    private Handler handler;

    public CodelessScanner(Context context) {
        this.context = context.getApplicationContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scannerApi = Build.VERSION.SDK_INT < 21 ? scannerApi19 : scannerApi21;
        handler = new Handler(Looper.getMainLooper());
    }

    public static final int DIALOG_MANUFACTURER_ID = 0x00D2;
    public static final int APPLE_MANUFACTURER_ID = 0x004C;
    public static final int MICROSOFT_MANUFACTURER_ID = 0x0006;

    public static class AdvData {

        public byte[] raw;
        public String name;
        public boolean discoverable;
        public boolean limitedDiscoverable;
        public ArrayList<UUID> services = new ArrayList<>();
        public HashMap<Integer, byte[]> manufacturer = new HashMap<>();

        public boolean codeless;
        public boolean dsps;
        public boolean suota;
        public boolean iot;
        public boolean wearable;
        public boolean mesh;
        public boolean proximity;

        public boolean iBeacon;
        public boolean dialogBeacon;
        public UUID beaconUuid;
        public int beaconMajor;
        public int beaconMinor;
        public boolean eddystone;
        public boolean microsoft;

        public boolean other() {
            return iot || wearable || mesh || proximity;
        }

        public boolean beacon() {
            return iBeacon || dialogBeacon || eddystone || microsoft;
        }

        public boolean unknown() {
            return !codeless && !dsps && !suota && !other() && !beacon();
        }
    }

    private AdvData parseAdvertisingData(byte[] data) {
        AdvData advData = new AdvData();
        advData.raw = data;

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            int length = buffer.get() & 0xff;
            if (length == 0)
                break;

            int type = buffer.get() & 0xff;
            --length;

            switch (type) {
                case 0x01: // Flags
                    if (length == 0 || buffer.remaining() == 0)
                        break;
                    byte flags = buffer.get();
                    length -= 1;
                    advData.discoverable = (flags & 0x02) != 0;
                    advData.limitedDiscoverable = (flags & 0x01) != 0;
                    break;

                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2 && buffer.remaining() >= 2) {
                        advData.services.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", buffer.getShort() & 0xffff)));
                        length -= 2;
                    }
                    break;

                case 0x04: // Partial list of 32-bit UUIDs
                case 0x05: // Complete list of 32-bit UUIDs
                    while (length >= 4 && buffer.remaining() >= 4) {
                        advData.services.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", buffer.getInt() & 0xffffffffL)));
                        length -= 4;
                    }
                    break;

                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16 && buffer.remaining() >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        advData.services.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;

                case 0x08: // Shortened Local Name
                    if (advData.name != null)
                        break;
                    // fall through
                case 0x09: // Complete Local Name
                    if (length > buffer.remaining())
                        break;
                    byte[] name = new byte[length];
                    buffer.get(name);
                    length = 0;
                    advData.name = new String(name, StandardCharsets.UTF_8);
                    break;

                case 0xff: // Manufacturer Specific Data
                    if (length >= 2 && buffer.remaining() >= 2) {
                        int manufacturer = buffer.getShort() & 0xffff;
                        length -= 2;
                        byte[] manufacturerData = new byte[0];
                        if (length <= buffer.remaining()) {
                            manufacturerData = new byte[length];
                            length = 0;
                            buffer.get(manufacturerData);
                        }
                        advData.manufacturer.put(manufacturer, manufacturerData);
                    }
                    break;
            }

            if (length > buffer.remaining())
                break;
            buffer.position(buffer.position() + length);
        }

        advData.codeless = advData.services.contains(Uuid.CODELESS_SERVICE_UUID);
        advData.dsps = advData.services.contains(Uuid.DSPS_SERVICE_UUID);
        advData.suota = advData.services.contains(Uuid.SUOTA_SERVICE_UUID);
        advData.iot = advData.services.contains(Uuid.IOT_SERVICE_UUID);
        advData.wearable = advData.services.contains(Uuid.WEARABLES_580_SERVICE_UUID) || advData.services.contains(Uuid.WEARABLES_680_SERVICE_UUID);
        advData.mesh = advData.services.contains(Uuid.MESH_PROVISIONING_SERVICE_UUID) || advData.services.contains(Uuid.MESH_PROXY_SERVICE_UUID);
        advData.proximity = advData.services.contains(Uuid.IMMEDIATE_ALERT_SERVICE_UUID) && advData.services.contains(Uuid.LINK_LOSS_SERVICE_UUID);

        // Check for iBeacon
        int manufacturerId = DIALOG_MANUFACTURER_ID;
        byte[] manufacturerData = advData.manufacturer.get(manufacturerId);
        if (manufacturerData == null) {
            manufacturerId = APPLE_MANUFACTURER_ID;
            manufacturerData = advData.manufacturer.get(manufacturerId);
        }
        if (manufacturerData != null && manufacturerData.length == 23) {
            ByteBuffer manufacturerDataBuffer = ByteBuffer.wrap(manufacturerData).order(ByteOrder.BIG_ENDIAN);
            // Check subtype/length
            if (buffer.get() == 2 && manufacturerDataBuffer.get() == 21) {
                advData.dialogBeacon = manufacturerId == DIALOG_MANUFACTURER_ID;
                advData.iBeacon = manufacturerId == APPLE_MANUFACTURER_ID;
                advData.beaconUuid = new UUID(manufacturerDataBuffer.getLong(), manufacturerDataBuffer.getLong());
                advData.beaconMajor = manufacturerDataBuffer.getShort() & 0xffff;
                advData.beaconMinor = manufacturerDataBuffer.getShort() & 0xffff;
            }
        }

        // Check for Microsoft beacon
        manufacturerData = advData.manufacturer.get(MICROSOFT_MANUFACTURER_ID);
        if (manufacturerData != null && manufacturerData.length == 27) {
            advData.microsoft = true;
        }

        return advData;
    }

    public boolean isScanning() {
        return scanning;
    }

    public void startScanning() {
        startScanning(0, null, 0, null);
    }

    public void startScanning(int duration) {
        startScanning(duration, null, 0, null);
    }

    public void startScanning(Activity activity, int bluetoothRequestCode, RuntimePermissionChecker permissionChecker) {
        startScanning(0, activity, bluetoothRequestCode, permissionChecker);
    }

    public void startScanning(int duration, Activity activity, int bluetoothRequestCode, RuntimePermissionChecker permissionChecker) {
        handler.removeCallbacks(scanTimer);
        if (!checkScanRequirements(activity, bluetoothRequestCode, permissionChecker))
            return;
        if (scanning)
            return;
        scanning = true;
        Log.d(TAG, "Start scanning");
        scannerApi.startScanning();
        if (duration > 0)
            handler.postDelayed(scanTimer, duration);
        EventBus.getDefault().post(new CodelessEvent.ScanStart(this));
    }

    public void stopScanning() {
        handler.removeCallbacks(scanTimer);
        if (!scanning)
            return;
        scanning = false;
        Log.d(TAG, "Stop scanning");
        scannerApi.stopScanning();
        EventBus.getDefault().post(new CodelessEvent.ScanStop(this));
    }

    private Runnable scanTimer = new Runnable() {
        @Override
        public void run() {
            stopScanning();
        }
    };

    private void onScanResult(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        EventBus.getDefault().post(new CodelessEvent.ScanResult(this, device, parseAdvertisingData(scanRecord), rssi));
    }

    private interface ScannerApi {
        void startScanning();
        void stopScanning();
    }

    private ScannerApi scannerApi19 = new ScannerApi() {

        BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                onScanResult(device, rssi, scanRecord);
            }
        };

        @Override
        public void startScanning() {
            bluetoothAdapter.startLeScan(mLeScanCallback);
        }

        @Override
        public void stopScanning() {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    };

    private ScannerApi scannerApi21 = new ScannerApi() {

        BluetoothLeScanner scanner;
        ScanCallback callback;
        ScanSettings settings;

        @TargetApi(21)
        @Override
        public void startScanning() {
            if (scanner == null) {
                scanner = bluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0).build();
                callback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        CodelessScanner.this.onScanResult(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        for (ScanResult result : results)
                            CodelessScanner.this.onScanResult(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                    }
                };
            }
            scanner.startScan(null, settings, callback);
        }

        @TargetApi(21)
        @Override
        public void stopScanning() {
            if (scanner != null && bluetoothAdapter.isEnabled())
                scanner.stopScan(callback);
        }
    };

    public boolean checkScanRequirements(Activity activity, int bluetoothRequestCode, RuntimePermissionChecker permissionChecker) {
        if (!bluetoothAdapter.isEnabled()) {
            if (activity != null) {
                if (bluetoothRequestCode > 0)
                    activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), bluetoothRequestCode);
                else
                    activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
            return false;
        }
        if (!checkPermissions()) {
            if (permissionChecker != null)
                permissionChecker.checkPermission(SCAN_LOCATION_PERMISSION, R.string.codeless_location_permission_rationale, new RuntimePermissionChecker.PermissionRequestCallback() {
                    @Override
                    public void onPermissionRequestResult(int requestCode, String[] permissions, String[] denied) {
                        if (denied == null)
                            EventBus.getDefault().post(new CodelessEvent.ScanRestart(CodelessScanner.this));
                    }
                });
            return false;
        }
        return checkLocationServices(activity);
    }

    public static final String SCAN_LOCATION_PERMISSION = Build.VERSION.SDK_INT < 29 ?  Manifest.permission.ACCESS_COARSE_LOCATION : Manifest.permission.ACCESS_FINE_LOCATION;

    public boolean checkPermissions() {
        return Build.VERSION.SDK_INT < 23 || context.checkSelfPermission(SCAN_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    private Boolean locationServicesRequired;
    private boolean locationServicesSkipCheck;

    public boolean checkLocationServices() {
        return checkLocationServices(null);
    }

    public boolean checkLocationServices(final Activity activity) {
        if (Build.VERSION.SDK_INT < 23 || locationServicesSkipCheck)
            return true;
        // Check if location services are required by reading the setting from Bluetooth app.
        if (locationServicesRequired == null) {
            locationServicesRequired = true;
            try {
                Resources res = context.getPackageManager().getResourcesForApplication("com.android.bluetooth");
                int id = res.getIdentifier("strict_location_check", "bool", "com.android.bluetooth");
                locationServicesRequired = res.getBoolean(id);
            } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                Log.e(TAG, "Failed to read location services requirement setting", e);
            }
            Log.d(TAG, "Location services requirement setting: " + locationServicesRequired);
        }
        if (!locationServicesRequired)
            return true;
        // Check location services setting. Prompt the user to enable them.
        if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF)
            return true;
        Log.d(TAG, "Location services disabled");
        if (activity != null) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.codeless_no_location_services_title)
                    .setMessage(R.string.codeless_no_location_services_msg)
                    .setPositiveButton(R.string.codeless_enable_location_services, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.codeless_no_location_services_scan, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            locationServicesSkipCheck = true;
                            EventBus.getDefault().post(new CodelessEvent.ScanRestart(CodelessScanner.this));
                        }
                    })
                    .show();
        }
        return false;
    }
}
