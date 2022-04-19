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
import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.BondingEntry;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessUtil.hex;
import static com.diasemi.codelesslib.CodelessUtil.hex2bytes;
import static com.diasemi.codelesslib.CodelessUtil.hexArray;

public class BondingEntryTransferCommand extends CodelessCommand {
    public static final String TAG = "BondingEntryTransferCommand";

    public static final String COMMAND = "IEBNDE";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.IEBNDE;

    public static final String PATTERN_STRING = "^IEBNDE=(\\d+)(?:,([0-9a-fA-F]{54};[0-9a-fA-F]{50};[0-9a-fA-F]{32};[0-9a-fA-F]{2};[0-9a-fA-F]{8}))?$"; // <index> <entry>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    protected static final String ENTRY_ARGUMENT_PATTERN_STRING = "^[0-9a-fA-F]{54};[0-9a-fA-F]{50};[0-9a-fA-F]{32};[0-9a-fA-F]{2};[0-9a-fA-F]{8}$";
    protected static final Pattern ENTRY_ARGUMENT_PATTERN = Pattern.compile(ENTRY_ARGUMENT_PATTERN_STRING);

    public static boolean validData(String data) {
        return ENTRY_ARGUMENT_PATTERN.matcher(data).matches();
    }

    private int index;
    private String entry;
    private BondingEntry bondingEntry;

    public BondingEntryTransferCommand(CodelessManager manager, int index) {
        super(manager);
        setIndex(index);
    }

    public BondingEntryTransferCommand(CodelessManager manager, int index, BondingEntry bondingEntry) {
        super(manager);
        setIndex(index);
        this.bondingEntry = bondingEntry;
        packEntry(bondingEntry);
    }

    public BondingEntryTransferCommand(CodelessManager manager, int index, String entry) {
        super(manager);
        setIndex(index);
        this.entry = entry;
        parseEntry(entry);
    }

    public BondingEntryTransferCommand(CodelessManager manager) {
        super(manager);
    }

    public BondingEntryTransferCommand(CodelessManager manager, String command, boolean parse) {
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
        String arguments = String.format(Locale.US, "%d", index);
        if (entry != null)
            arguments += "," + entry;
        return arguments;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            if (validData(response)) {
                entry = response;
                parseEntry(response);
            } else {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid bonding entry: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, String.format(Locale.US, "Bonding entry: LTK:%s EDIV:%04X(%d) Rand:%s Key size:%02X(%d) CSRK:%s Bluetooth address:%s Address type:%02X(%d) Authentication level:%02X(%d) Bonding database slot:%02X(%d) IRK:%s Persistence status:%02X(%d) Timestamp:%s",
                        hexArray(bondingEntry.ltk), bondingEntry.ediv, bondingEntry.ediv, hexArray(bondingEntry.rand), bondingEntry.keySize, bondingEntry.keySize, hexArray(bondingEntry.csrk), hexArray(bondingEntry.bluetoothAddress), bondingEntry.addressType, bondingEntry.addressType,
                        bondingEntry.authenticationLevel, bondingEntry.authenticationLevel, bondingEntry.bondingDatabaseSlot, bondingEntry.bondingDatabaseSlot, hexArray(bondingEntry.irk), bondingEntry.persistenceStatus, bondingEntry.persistenceStatus, hexArray(bondingEntry.timestamp)));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.BondingEntryEvent(this));
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 1 || count == 2;
    }

    @Override
    protected String parseArguments() {
        Integer num = decodeNumberArgument(1);
        if (num == null || CodelessLibConfig.CHECK_BONDING_DATABASE_INDEX && (num < CodelessLibConfig.BONDING_DATABASE_INDEX_MIN || num > CodelessLibConfig.BONDING_DATABASE_INDEX_MAX))
            return "Invalid bonding database index";
        index = num;

        if (CodelessProfile.countArguments(command, ",") == 1)
            return null;
        String entry = matcher.group(2);
        if (validData(entry)) {
            this.entry = entry;
            parseEntry(entry);
        } else {
            return "Invalid database entry";
        }

        return null;
    }

    private void parseEntry(String entry) {
        bondingEntry = new BondingEntry();
        try {
            bondingEntry.ltk = hex2bytes(entry.substring(0, 32));
            bondingEntry.ediv = Integer.parseInt(entry.substring(32, 36), 16);
            bondingEntry.rand = hex2bytes(entry.substring(36, 52));
            bondingEntry.keySize = Integer.parseInt(entry.substring(52, 54), 16);
            bondingEntry.csrk = hex2bytes(entry.substring(55, 87));
            bondingEntry.bluetoothAddress = hex2bytes(entry.substring(87, 99));
            bondingEntry.addressType = Integer.parseInt(entry.substring(99, 101), 16);
            bondingEntry.authenticationLevel = Integer.parseInt(entry.substring(101, 103), 16);
            bondingEntry.bondingDatabaseSlot = Integer.parseInt(entry.substring(103, 105), 16);
            bondingEntry.irk = hex2bytes(entry.substring(106, 138));
            bondingEntry.persistenceStatus = Integer.parseInt(entry.substring(139, 141), 16);
            bondingEntry.timestamp = hex2bytes(entry.substring(142, 150));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            invalid = true;
        }
    }

    private void packEntry(BondingEntry bondingEntry) {
        StringBuilder entry = new StringBuilder();
        entry.append(hex(bondingEntry.ltk));
        entry.append(String.format(Locale.US, "%04X", bondingEntry.ediv));
        entry.append(hex(bondingEntry.rand));
        entry.append(String.format(Locale.US, "%02X", bondingEntry.keySize));
        entry.append(";");
        entry.append(hex(bondingEntry.csrk));
        entry.append(hex(bondingEntry.bluetoothAddress));
        entry.append(String.format(Locale.US, "%02X", bondingEntry.addressType));
        entry.append(String.format(Locale.US, "%02X", bondingEntry.authenticationLevel));
        entry.append(String.format(Locale.US, "%02X", bondingEntry.bondingDatabaseSlot));
        entry.append(";");
        entry.append(hex(bondingEntry.irk));
        entry.append(";");
        entry.append(String.format(Locale.US, "%02X", bondingEntry.persistenceStatus));
        entry.append(";");
        entry.append(hex(bondingEntry.timestamp));
        this.entry = entry.toString();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        if (CodelessLibConfig.CHECK_BONDING_DATABASE_INDEX) {
            if (index < CodelessLibConfig.BONDING_DATABASE_INDEX_MIN || index > CodelessLibConfig.BONDING_DATABASE_INDEX_MAX)
                invalid = true;
        }
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
        parseEntry(entry);
    }

    public BondingEntry getBondingEntry() {
        return bondingEntry;
    }

    public void setBondingEntry(BondingEntry bondingEntry) {
        this.bondingEntry = bondingEntry;
        packEntry(bondingEntry);
    }
}
