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

import com.diasemi.codelesslib.CodelessEvent;
import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

public class BondingEntryClearCommand extends CodelessCommand {
    public static final String TAG = "BondingEntryClearCommand";

    public static final String COMMAND = "CLRBNDE";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.CLRBNDE;

    public static final String PATTERN_STRING = "^CLRBNDE=(0x[0-9a-fA-F]+|\\d+)$"; // <index>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int index;

    public BondingEntryClearCommand(CodelessManager manager, int index) {
        super(manager);
        setIndex(index);
    }

    public BondingEntryClearCommand(CodelessManager manager) {
        super(manager);
    }

    public BondingEntryClearCommand(CodelessManager manager, String command, boolean parse) {
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
        return Integer.toString(index);
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.BondingEntryClear(this));
    }

    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        return CodelessProfile.countArguments(command, ",") == 1;
    }

    @Override
    protected String parseArguments() {
        Integer num = decodeNumberArgument(1);
        if (num == null || CodelessLibConfig.CHECK_BONDING_DATABASE_INDEX && (num < CodelessLibConfig.BONDING_DATABASE_INDEX_MIN || num > CodelessLibConfig.BONDING_DATABASE_INDEX_MAX) && num != CodelessLibConfig.BONDING_DATABASE_ALL_VALUES)
            return "Invalid bonding database index";
        index = num;

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
}
