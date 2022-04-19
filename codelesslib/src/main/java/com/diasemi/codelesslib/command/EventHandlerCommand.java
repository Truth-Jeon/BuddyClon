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
import com.diasemi.codelesslib.CodelessProfile.EventHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.diasemi.codelesslib.CodelessProfile.Command.CONNECTION_EVENT_HANDLER;
import static com.diasemi.codelesslib.CodelessProfile.Command.DISCONNECTION_EVENT_HANDLER;
import static com.diasemi.codelesslib.CodelessProfile.Command.WAKEUP_EVENT_HANDLER;

public class EventHandlerCommand extends CodelessCommand {
    public static final String TAG = "EventHandlerCommand";

    public static final String COMMAND = "HNDL";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.HNDL;

    public static final String PATTERN_STRING = "^HNDL(?:=(\\d+)(?:,((?:[^;]+;?)*))?)?$"; // <event> <command>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private EventHandler eventHandler;
    private ArrayList<EventHandler> eventHandlerTable;
    private boolean hasArguments;

    public EventHandlerCommand(CodelessManager manager, int event, ArrayList<CodelessCommand> commands) {
        this(manager, event);
        eventHandler.commands = commands;
    }

    public EventHandlerCommand(CodelessManager manager, int event, String commandString) {
        this(manager, event);
        eventHandler.commands = parseCommandString(commandString);
    }

    public EventHandlerCommand(CodelessManager manager, int event) {
        this(manager);
        eventHandler = new EventHandler();
        setEvent(event);
        eventHandler.commands = new ArrayList<>();
        hasArguments = true;
    }

    public EventHandlerCommand(CodelessManager manager, EventHandler eventHandler) {
        this(manager);
        setEventHandler(eventHandler);
        hasArguments = true;
    }

    public EventHandlerCommand(CodelessManager manager) {
        super(manager);
    }

    public EventHandlerCommand(CodelessManager manager, String command, boolean parse) {
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
        if (!hasArguments)
            return null;
        String arguments = String.valueOf(eventHandler.event);
        if (eventHandler.commands.size() > 0)
            arguments += String.format(Locale.US, ",%s", packCommandList(eventHandler.commands));
        return arguments;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (eventHandlerTable == null)
            eventHandlerTable = new ArrayList<>();
        String errorMsg = "Received invalid Event Handler response: " + response;
        try {
            EventHandler eventHandler = new EventHandler();
            int splitPos = response.indexOf(",");
            int event = Integer.parseInt(response.substring(0, splitPos));
            if (event != CONNECTION_EVENT_HANDLER && event != DISCONNECTION_EVENT_HANDLER && event != WAKEUP_EVENT_HANDLER) {
                Log.e(TAG, errorMsg);
                invalid = true;
                return;
            }
            eventHandler.event = event;
            String commandString = response.substring(splitPos + 1);
            eventHandler.commands = parseCommandString(commandString.equals("<empty>") ? "" : commandString);
            eventHandlerTable.add(eventHandler);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            Log.e(TAG, errorMsg);
            invalid = true;
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(hasArguments ? new CodelessEvent.EventCommands(this) : new CodelessEvent.EventCommandsTable(this));
    }

    @Override
    protected boolean checkArgumentsCount() {
        int count = CodelessProfile.countArguments(command, ",");
        return count == 0 || count == 1 || count == 2;
    }

    @Override
    protected String parseArguments() {
        eventHandler = new EventHandler();
        eventHandler.commands = new ArrayList<>();

        int count = CodelessProfile.countArguments(command, ",");
        if (count == 0)
            return null;
        hasArguments = true;

        Integer num = decodeNumberArgument(1);
        if (num == null || num != CONNECTION_EVENT_HANDLER && num != DISCONNECTION_EVENT_HANDLER && num != WAKEUP_EVENT_HANDLER)
            return "Invalid event";
        eventHandler.event = num;

        if (count == 2) {
            String commandString = matcher.group(2);
            eventHandler.commands = parseCommandString(commandString);
        }

        return null;
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
        if (eventHandler.event != CONNECTION_EVENT_HANDLER && eventHandler.event != DISCONNECTION_EVENT_HANDLER && eventHandler.event != WAKEUP_EVENT_HANDLER)
            invalid = true;
    }

    public int getEvent() {
        return eventHandler.event;
    }

    public void setEvent(int event) {
        eventHandler.event = event;
        if (event != CONNECTION_EVENT_HANDLER && event != DISCONNECTION_EVENT_HANDLER && event != WAKEUP_EVENT_HANDLER)
            invalid = true;
    }

    public ArrayList<CodelessCommand> getCommands() {
        return eventHandler.commands;
    }

    public void setCommands(ArrayList<CodelessCommand> commands) {
        eventHandler.commands = commands;
    }

    public String getCommandString() {
        return packCommandList(eventHandler.commands);
    }

    public void setCommandString(String commandString) {
        eventHandler.commands = parseCommandString(commandString);
    }

    public ArrayList<EventHandler> getEventHandlerTable() {
        return eventHandlerTable;
    }

    public void setEventHandlerTable(ArrayList<EventHandler> eventHandlerTable) {
        this.eventHandlerTable = eventHandlerTable;
    }

    private ArrayList<CodelessCommand> parseCommandString(String commandString) {
        String[] commandArray = commandString.split(";");
        ArrayList<CodelessCommand> commandList = new ArrayList<>();
        for (String command : commandArray) {
            if (!command.isEmpty())
                commandList.add(manager.parseTextCommand(command));
        }
        return commandList;
    }

    private String packCommandList(ArrayList<CodelessCommand> commands) {
        StringBuilder stringBuilder = new StringBuilder();
        for (CodelessCommand command : commands) {
            String commandString = command.getPrefix() != null ? command.getPrefix() + command.getCommand() : command.getCommand();
            stringBuilder.append(stringBuilder.length() > 0 ? ";" + commandString : commandString);
        }
        return stringBuilder.toString();
    }
}
