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
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;
import com.diasemi.codelesslib.CodelessProfile.EventConfig;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessProfile.Command.ACTIVATE_EVENT;
import static com.diasemi.codelesslib.CodelessProfile.Command.CONNECTION_EVENT;
import static com.diasemi.codelesslib.CodelessProfile.Command.DEACTIVATE_EVENT;
import static com.diasemi.codelesslib.CodelessProfile.Command.DISCONNECTION_EVENT;
import static com.diasemi.codelesslib.CodelessProfile.Command.INITIALIZATION_EVENT;
import static com.diasemi.codelesslib.CodelessProfile.Command.WAKEUP_EVENT;

public class EventConfigCommand extends CodelessCommand {
    public static final String TAG = "EventConfigCommand";

    public static final String COMMAND = "EVENT";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.EVENT;

    public static final String PATTERN_STRING = "^EVENT(?:=(\\d+),(\\d))?$"; // <event> <status>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private ArrayList<EventConfig> eventStatusTable;
    private EventConfig eventConfig;
    private boolean hasArguments;

    public EventConfigCommand(CodelessManager manager, int eventType, boolean status) {
        this(manager, new EventConfig(eventType, status));
    }

    public EventConfigCommand(CodelessManager manager, EventConfig eventConfig) {
        this(manager);
        setEventConfig(eventConfig);
        hasArguments = true;
    }

    public EventConfigCommand(CodelessManager manager) {
        super(manager);
    }

    public EventConfigCommand(CodelessManager manager, String command, boolean parse) {
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
        return hasArguments ? String.format(Locale.US, "%d,%d", eventConfig.type, eventConfig.status ? ACTIVATE_EVENT : DEACTIVATE_EVENT) : null;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (eventStatusTable == null)
            eventStatusTable = new ArrayList<>();
        String errorMsg = "Received invalid Event status response: " + response;
        try {
            EventConfig eventConfig = new EventConfig();
            String[] eventData = response.split(",");
            int type = Integer.parseInt(eventData[0]);
            if (type != INITIALIZATION_EVENT && type != CONNECTION_EVENT && type != DISCONNECTION_EVENT && type != WAKEUP_EVENT) {
                Log.e(TAG, errorMsg);
                invalid = true;
                return;
            }
            eventConfig.type = type;
            int status = Integer.parseInt(eventData[1]);
            if (status != DEACTIVATE_EVENT && status != ACTIVATE_EVENT) {
                Log.e(TAG, errorMsg);
                invalid = true;
                return;
            }
            eventConfig.status = status == ACTIVATE_EVENT;
            eventStatusTable.add(eventConfig);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            Log.e(TAG, errorMsg);
            invalid = true;
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(hasArguments ? new CodelessEvent.EventStatus(this) : new CodelessEvent.EventStatusTable(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 2;
    }

    @Override
    protected String parseArguments() {
        eventConfig = new EventConfig();

        if (!CodelessProfile.hasArguments(command))
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || num != INITIALIZATION_EVENT && num != CONNECTION_EVENT && num != DISCONNECTION_EVENT && num != WAKEUP_EVENT)
            return "Invalid event number";
        eventConfig.type = num;

        num = decodeNumberArgument(2);
        if (num == null || num != DEACTIVATE_EVENT && num != ACTIVATE_EVENT)
            return "Invalid event status";
        eventConfig.status = num == ACTIVATE_EVENT;

        return null;
    }

    public EventConfig getEventConfig() {
        return eventConfig;
    }

    public void setEventConfig(EventConfig eventConfig) {
        this.eventConfig = eventConfig;
        if (eventConfig.type != INITIALIZATION_EVENT && eventConfig.type != CONNECTION_EVENT && eventConfig.type != DISCONNECTION_EVENT && eventConfig.type != WAKEUP_EVENT)
            invalid = true;
    }

    public int getType() {
        return eventConfig.type;
    }

    public void setType(int type) {
        eventConfig.type = type;
        if (type != INITIALIZATION_EVENT && type != CONNECTION_EVENT && type != DISCONNECTION_EVENT && type != WAKEUP_EVENT)
            invalid = true;
    }

    public boolean getStatus() {
        return eventConfig.status;
    }

    public void setStatus(boolean status) {
        eventConfig.status = status;
    }

    public ArrayList<EventConfig> getEventStatusTable() {
        return eventStatusTable;
    }

    public void setEventStatusTable(ArrayList<EventConfig> eventStatusTable) {
        this.eventStatusTable = eventStatusTable;
    }
}
