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

import java.util.ArrayList;
import java.util.regex.Pattern;

public class CmdGetCommand extends CodelessCommand {
    public static final String TAG = "CmdGetCommand";

    public static final String COMMAND = "CMD";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.CMD;

    public static final String PATTERN_STRING = "^CMD=(\\d+)$"; // <index>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int index;
    private String commandString;
    private ArrayList<CodelessCommand> commands = new ArrayList<>();

    public CmdGetCommand(CodelessManager manager, int index) {
        this(manager);
        setIndex(index);
    }

    public CmdGetCommand(CodelessManager manager) {
        super(manager);
    }

    public CmdGetCommand(CodelessManager manager, String command, boolean parse) {
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
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            commandString = response;
            String[] commandArray = response.split(";");
            for (String commandString : commandArray)
                commands.add(manager.parseTextCommand(commandString));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.StoredCommands(this));
    }

    @Override
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
        if (num == null || CodelessLibConfig.CHECK_COMMAND_STORE_INDEX && (num < CodelessLibConfig.COMMAND_STORE_INDEX_MIN || num > CodelessLibConfig.COMMAND_STORE_INDEX_MAX))
            return "Invalid index";
        index = num;
        return null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        if (CodelessLibConfig.CHECK_COMMAND_STORE_INDEX) {
            if (index < CodelessLibConfig.COMMAND_STORE_INDEX_MIN || index > CodelessLibConfig.COMMAND_STORE_INDEX_MAX)
                invalid = true;
        }
    }

    public String getCommandString() {
        return commandString;
    }

    public void setCommandString(String commandString) {
        this.commandString = commandString;
    }

    public ArrayList<CodelessCommand> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<CodelessCommand> commands) {
        this.commands = commands;
    }
}
