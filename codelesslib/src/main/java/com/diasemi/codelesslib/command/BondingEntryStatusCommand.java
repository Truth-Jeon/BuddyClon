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
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.Command;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class BondingEntryStatusCommand extends CodelessCommand {
    public static final String TAG = "BondingEntryStatusCommand";

    public static final String COMMAND = "CHGBNDP";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.CHGBNDP;

    public static final String PATTERN_STRING = "^CHGBNDP(?:=(0x[0-9a-fA-F]+|\\d+),(\\d))?$"; // <index> <status>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int index;
    private boolean persistent;
    private ArrayList<Boolean> tablePersistenceStatus;
    private boolean hasArguments;

    public BondingEntryStatusCommand(CodelessManager manager, int index, boolean persistent) {
        super(manager);
        setIndex(index);
        this.persistent = persistent;
        hasArguments = true;
    }

    public BondingEntryStatusCommand(CodelessManager manager) {
        super(manager);
    }

    public BondingEntryStatusCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%d", index, (persistent ? Command.BONDING_ENTRY_PERSISTENT : Command.BONDING_ENTRY_NON_PERSISTENT)) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        try {
            if (tablePersistenceStatus == null)
                tablePersistenceStatus = new ArrayList<>();
            int index;
            Boolean status = null;
            int commaPos = response.indexOf(",");
            index = Integer.parseInt(response.substring(0, commaPos));
            if (CodelessLibConfig.CHECK_BONDING_DATABASE_INDEX && (index < CodelessLibConfig.BONDING_DATABASE_INDEX_MIN || index > CodelessLibConfig.BONDING_DATABASE_INDEX_MAX))
                invalid = true;
            String statusStr = response.substring(commaPos + 1);
            if (!statusStr.equals("<empty>") && Integer.parseInt(statusStr) != Command.BONDING_ENTRY_NON_PERSISTENT && Integer.parseInt(statusStr) != Command.BONDING_ENTRY_PERSISTENT)
                invalid = true;
            if (!statusStr.equals("<empty>"))
                status = Integer.parseInt(statusStr) != Command.BONDING_ENTRY_NON_PERSISTENT;
            tablePersistenceStatus.add(status);
            if (!invalid)
                Log.d(TAG, "Bonding persistence status: " + index + ", " + (status != null ? (status ? "persistent" : "non-persistent") : "empty"));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            invalid = true;
        }
        if (invalid)
            Log.e(TAG, "Received invalid bonding entry persistence status: " + response);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(hasArguments ? new CodelessEvent.BondingEntryPersistenceStatusSet(this) : new CodelessEvent.BondingEntryPersistenceTableStatus(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 2;
    }

    @Override
    protected String parseArguments() {
        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || CodelessLibConfig.CHECK_BONDING_DATABASE_INDEX && (num < CodelessLibConfig.BONDING_DATABASE_INDEX_MIN || num > CodelessLibConfig.BONDING_DATABASE_INDEX_MAX) && num != CodelessLibConfig.BONDING_DATABASE_ALL_VALUES)
            return "Invalid bonding database index";
        index = num;

        num = decodeNumberArgument(2);
        if (num == null || num != Command.BONDING_ENTRY_NON_PERSISTENT && num != Command.BONDING_ENTRY_PERSISTENT)
            return "Invalid bonding entry persistent status";
        persistent = num == Command.BONDING_ENTRY_PERSISTENT;

        return null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        if (CodelessLibConfig.CHECK_BONDING_DATABASE_INDEX) {
            if ((index < CodelessLibConfig.BONDING_DATABASE_INDEX_MIN || index > CodelessLibConfig.BONDING_DATABASE_INDEX_MAX) && index != CodelessLibConfig.BONDING_DATABASE_ALL_VALUES)
                invalid = true;
        }
    }

    public boolean persistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public ArrayList<Boolean> getTablePersistenceStatus() {
        return tablePersistenceStatus;
    }
}
