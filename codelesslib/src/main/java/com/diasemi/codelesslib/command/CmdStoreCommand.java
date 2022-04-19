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

import com.diasemi.codelesslib.CodelessLibConfig;
import com.diasemi.codelesslib.CodelessManager;
import com.diasemi.codelesslib.CodelessProfile;
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class CmdStoreCommand extends CodelessCommand {
    public static final String TAG = "CmdStoreCommand";

    public static final String COMMAND = "CMDSTORE";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.CMDSTORE;

    public static final String PATTERN_STRING = "^CMDSTORE=(\\d+),((?:[^;]+;?)+)$"; // <index> <command>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int index;
    private ArrayList<CodelessCommand> commands;
    private String commandString;

    public CmdStoreCommand(CodelessManager manager, int index, ArrayList<CodelessCommand> commands) {
        this(manager);
        setIndex(index);
        this.commands = commands;
        commandString = packCommandList(commands);
    }

    public CmdStoreCommand(CodelessManager manager, int index, String commandString) {
        this(manager);
        setIndex(index);
        this.commandString = commandString;
        commands = parseCommandString(commandString);
    }

    public CmdStoreCommand(CodelessManager manager) {
        super(manager);
    }

    public CmdStoreCommand(CodelessManager manager, String command, boolean parse) {
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
        return String.format(Locale.US, "%d,%s", index, commandString);
    }

    @Override
    protected boolean requiresArguments() {
        return true;
    }

    @Override
    protected boolean checkArgumentsCount() {
        return CodelessProfile.countArguments(command, ",") == 2;
    }

    @Override
    protected String parseArguments() {
        Integer num = decodeNumberArgument(1);
        if (num == null || CodelessLibConfig.CHECK_COMMAND_STORE_INDEX && (num < CodelessLibConfig.COMMAND_STORE_INDEX_MIN || num > CodelessLibConfig.COMMAND_STORE_INDEX_MAX))
            return "Invalid index";
        index = num;
        String commandString = matcher.group(2);
        if (commandString == null)
            return "Invalid command strings";
        this.commandString = commandString;
        this.commands = parseCommandString(commandString);
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
