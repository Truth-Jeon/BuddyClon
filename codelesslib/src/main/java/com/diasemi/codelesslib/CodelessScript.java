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

import android.util.Log;

import com.diasemi.codelesslib.CodelessProfile.CommandID;
import com.diasemi.codelesslib.command.CodelessCommand;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class CodelessScript {
    public static final String TAG = "CodelessScript";

    private static int nextScriptId;

    private int id = nextScriptId++;
    private String name;
    private CodelessManager manager;
    private ArrayList<String> script = new ArrayList<>();
    private ArrayList<CodelessCommand> commands = new ArrayList<>();
    private int current;
    private boolean stopOnError = true;
    private boolean invalid;
    private boolean custom;
    private boolean started;
    private boolean stopped;
    private boolean complete;

    public CodelessScript(CodelessManager manager) {
        this.manager = manager;
    }

    public CodelessScript(CodelessManager manager, String text) {
        this(manager);
        setScript(text);
    }

    public CodelessScript(CodelessManager manager, ArrayList<String> script) {
        this(manager);
        setScript(script);
    }

    public CodelessScript(String name, CodelessManager manager) {
        this(manager);
        this.name = name;
    }

    public CodelessScript(String name, CodelessManager manager, String script) {
        this(manager, script);
        this.name = name;
    }

    public CodelessScript(String name, CodelessManager manager, ArrayList<String> script) {
        this(manager, script);
        this.name = name;
    }

    private void initScript() {
        commands = new ArrayList<>(script.size());
        for (String text : script) {
            CodelessCommand command = manager.parseTextCommand(text);
            command.setScript(this);
            commands.add(command);
            if (!command.isValid())
                invalid = true;
            if (command.getCommandID() == CommandID.CUSTOM)
                custom = true;
        }
    }

    public void start() {
        if (started)
            return;
        started = true;
        if (CodelessLibLog.SCRIPT)
            Log.d(TAG, "Script start: " + this);
        EventBus.getDefault().post(new CodelessEvent.ScriptStartEvent(this));
        current = -1;
        sendNextCommand();
    }

    public void stop() {
        if (CodelessLibLog.SCRIPT)
            Log.d(TAG, "Script stopped: " + this);
        stopped = true;
        complete = true;
    }

    public void onSuccess(CodelessCommand command) {
        if (CodelessLibLog.SCRIPT)
            Log.d(TAG, "Script command success: " + this + " " + command);
        EventBus.getDefault().post(new CodelessEvent.ScriptCommandEvent(this, command));
        sendNextCommand();
    }

    public void onError(CodelessCommand command) {
        if (CodelessLibLog.SCRIPT)
            Log.d(TAG, "Script command error: " + this + " " + command + " " + command.getError());
        EventBus.getDefault().post(new CodelessEvent.ScriptCommandEvent(this, command));
        if (!stopOnError) {
            sendNextCommand();
        } else {
            stop();
            EventBus.getDefault().post(new CodelessEvent.ScriptEndEvent(this, true));
        }
    }

    private void sendNextCommand() {
        if (stopped)
            EventBus.getDefault().post(new CodelessEvent.ScriptEndEvent(this, false));
        if (complete)
            return;
        current++;
        if (current < commands.size()) {
            CodelessCommand command = getCurrentCommand();
            if (CodelessLibLog.SCRIPT)
                Log.d(TAG, "Script command: " + this + "[" + (current +  1) + "] " + command);
            manager.sendCommand(command);
        } else {
            complete = true;
            if (CodelessLibLog.SCRIPT)
                Log.d(TAG, "Script end: " + this);
            EventBus.getDefault().post(new CodelessEvent.ScriptEndEvent(this, false));
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CodelessManager getManager() {
        return manager;
    }

    public ArrayList<String> getScript() {
        return script;
    }

    public void setScript(ArrayList<String> script) {
        if (started)
            return;
        this.script = script;
        initScript();
    }

    public void setScript(String text) {
        if (started)
            return;
        script = new ArrayList<>();
        for (String line : text.split("\n")) {
            line = line.trim();
            if (!line.isEmpty())
                script.add(line);
        }
        initScript();
    }

    public String getText() {
        if (script.isEmpty())
            return "";
        StringBuilder text = new StringBuilder();
        for (String command : script)
            text.append(command).append("\n");
        return text.toString();
    }

    public ArrayList<CodelessCommand> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<CodelessCommand> commands) {
        if (started)
            return;
        this.commands = commands;
        script = new ArrayList<>(commands.size());
        for (CodelessCommand command : commands) {
            if (!command.isParsed())
                command.packCommand();
            String text = command.getCommand();
            if (!command.isValid())
                invalid = true;
            if (command.getCommandID() == CommandID.CUSTOM)
                custom = true;
            else if (command.hasPrefix())
                text = command.getPrefix() + text;
            script.add(text);
        }
    }

    public int getCurrent() {
        return current;
    }

    public CodelessCommand getCurrentCommand() {
        return commands.get(current);
    }

    public String getCurrentCommandText() {
        return script.get(current);
    }

    public int getCommandIndex(CodelessCommand command) {
        return commands.indexOf(command);
    }

    public void setCurrent(int current) {
        if (!started)
            this.current = current;
    }

    public boolean stopOnError() {
        return stopOnError;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    public boolean hasInvalid() {
        return invalid;
    }

    public boolean hasCustom() {
        return custom;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isComplete() {
        return complete;
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + (name != null ? name : "Script " + id) + "]";
    }
}
