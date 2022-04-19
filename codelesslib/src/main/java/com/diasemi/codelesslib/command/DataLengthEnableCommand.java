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
import com.diasemi.codelesslib.CodelessLibLog;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessProfile.Command.DLE_DISABLED;
import static com.diasemi.codelesslib.CodelessProfile.Command.DLE_ENABLED;
import static com.diasemi.codelesslib.CodelessProfile.Command.DLE_PACKET_LENGTH_DEFAULT;
import static com.diasemi.codelesslib.CodelessProfile.Command.DLE_PACKET_LENGTH_MAX;
import static com.diasemi.codelesslib.CodelessProfile.Command.DLE_PACKET_LENGTH_MIN;

public class DataLengthEnableCommand extends CodelessCommand {
    public static final String TAG = "DataLengthEnableCommand";

    public static final String COMMAND = "DLEEN";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.DLEEN;

    public static final String PATTERN_STRING = "^DLEEN(?:=(\\d),(\\d+),(\\d+))?$"; // <enable> <tx> <rx>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private static final String RESPONSE_PATTERN_STRING = "^(\\d).(\\d+).(\\d+)$"; // <enable> <tx> <rx>
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(RESPONSE_PATTERN_STRING);

    private boolean enabled;
    private int txPacketLength;
    private int rxPacketLength;
    private boolean hasArguments;

    public DataLengthEnableCommand(CodelessManager manager, boolean enabled, int txPacketLength, int rxPacketLength) {
        this(manager);
        this.enabled = enabled;
        setTxPacketLength(txPacketLength);
        setRxPacketLength(rxPacketLength);
        hasArguments = true;
    }

    public DataLengthEnableCommand(CodelessManager manager, boolean enabled) {
        this(manager, enabled, DLE_PACKET_LENGTH_DEFAULT, DLE_PACKET_LENGTH_DEFAULT);
    }

    public DataLengthEnableCommand(CodelessManager manager) {
        super(manager);
    }

    public DataLengthEnableCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%d,%d", enabled ? DLE_ENABLED : DLE_DISABLED, txPacketLength, rxPacketLength) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            Matcher matcher = RESPONSE_PATTERN.matcher(response);
            if (matcher.matches()) {
                try {
                    int num = Integer.parseInt(matcher.group(1));
                    if (num != DLE_DISABLED && num != DLE_ENABLED)
                        invalid = true;
                    enabled = num != DLE_DISABLED;
                    setTxPacketLength(Integer.parseInt(matcher.group(2)));
                    setRxPacketLength(Integer.parseInt(matcher.group(3)));
                } catch (NumberFormatException e) {
                    invalid = true;
                }
            } else {
                invalid = true;
            }
            if (invalid)
                Log.e(TAG, "Received invalid DLE parameters: " + response);
            else if (CodelessLibLog.COMMAND)
                Log.d(TAG, "DLE: " + (enabled ? "enabled" : "disabled") + " tx=" + txPacketLength + " rx=" + rxPacketLength);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.DataLengthEnable(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 3;
    }

    @Override
    protected String parseArguments() {
        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || num != DLE_DISABLED && num != DLE_ENABLED)
            return "Enable must be 0 or 1";
        enabled = num != DLE_DISABLED;

        num = decodeNumberArgument(2);
        if (num == null || num < DLE_PACKET_LENGTH_MIN || num > DLE_PACKET_LENGTH_MAX)
            return "Invalid TX packet length";
        txPacketLength = num;

        num = decodeNumberArgument(3);
        if (num == null || num < DLE_PACKET_LENGTH_MIN || num > DLE_PACKET_LENGTH_MAX)
            return "Invalid RX packet length";
        rxPacketLength = num;

        return null;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTxPacketLength() {
        return txPacketLength;
    }

    public void setTxPacketLength(int txPacketLength) {
        this.txPacketLength = txPacketLength;
        if (txPacketLength < DLE_PACKET_LENGTH_MIN || txPacketLength > DLE_PACKET_LENGTH_MAX)
            invalid = true;
    }

    public int getRxPacketLength() {
        return rxPacketLength;
    }

    public void setRxPacketLength(int rxPacketLength) {
        this.rxPacketLength = rxPacketLength;
        if (rxPacketLength < DLE_PACKET_LENGTH_MIN || rxPacketLength > DLE_PACKET_LENGTH_MAX)
            invalid = true;
    }
}
