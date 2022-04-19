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
import com.diasemi.codelesslib.CodelessScript;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public abstract class CodelessCommand {
    public static final String TAG = "CodelessCommand";

    protected CodelessManager manager;
    protected CodelessScript script;
    protected Object origin;
    protected String command;
    protected String prefix;
    protected ArrayList<String> response = new ArrayList<>();
    protected Matcher matcher;
    protected boolean inbound;
    protected boolean parsed;
    protected boolean invalid;
    protected boolean peerInvalid;
    protected boolean complete;
    protected String error;
    protected int errorCode;

    public CodelessCommand(CodelessManager manager) {
        this.manager = manager;
    }

    public CodelessCommand(CodelessManager manager, String command, boolean parse) {
        this(manager);
        this.command = command;
        if (parse)
            parseCommand(command);
    }

    public CodelessManager getManager() {
        return manager;
    }

    public CodelessScript getScript() {
        return script;
    }

    public void setScript(CodelessScript script) {
        this.script = script;
    }

    public Object getOrigin() {
        return origin;
    }

    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    public CodelessCommand origin(Object origin) {
        this.origin = origin;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean hasPrefix() {
        return prefix != null;
    }

    public ArrayList<String> getResponse() {
        return response;
    }

    public void setInbound() {
        inbound = true;
    }

    public boolean isInbound() {
        return inbound;
    }

    public boolean isParsed() {
        return parsed;
    }

    public boolean isValid() {
        return !invalid;
    }

    public void setPeerInvalid() {
        peerInvalid = true;
    }

    public boolean isPeerInvalid() {
        return peerInvalid;
    }

    public void setComplete() {
        complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean failed() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    protected String getTag() {
        return TAG;
    }

    public abstract String getID();

    public abstract String getName();

    public abstract CodelessProfile.CommandID getCommandID();

    public Pattern getPattern() {
        return null;
    }

    public String packCommand() {
        return command = !hasArguments() ? getID() : getID() + "=" + getArguments();
    }

    protected boolean hasArguments() {
        return false;
    }

    protected String getArguments() {
        return null;
    }

    public boolean parsePartialResponse() {
        return false;
    }

    public void parseResponse(String response) {
        this.response.add(response);
        if (CodelessLibLog.COMMAND)
            Log.d(getTag(), "Response: " + response);
    }

    protected int responseLine() {
        return response.size();
    }

    public void onSuccess() {
        if (CodelessLibLog.COMMAND)
            Log.d(getTag(), "Command succeeded");
        complete = true;
        EventBus.getDefault().post(new CodelessEvent.CommandSuccess(this));
        if (script != null)
            script.onSuccess(this);
    }

    public void onError(String msg) {
        if (CodelessLibLog.COMMAND)
            Log.d(getTag(), "Command failed: " + msg);
        if (error == null)
            error = msg;
        complete = true;
        EventBus.getDefault().post(new CodelessEvent.CommandError(this, msg));
        if (script != null)
            script.onError(this);
    }

    public void setErrorCode(int code, String message) {
        errorCode = code;
        error = message;
    }

    public String parseCommand(String command) {
        if (CodelessLibLog.COMMAND)
            Log.d(getTag(), "Parse command: " + command);
        this.command = command;
        parsed = true;

        Pattern pattern = getPattern();
        if (pattern == null) {
            Log.e(getTag(), "No command pattern");
            invalid = true;
            return error = CodelessProfile.INVALID_COMMAND;
        }

        if (requiresArguments() && !CodelessProfile.hasArguments(command)) {
            if (CodelessLibLog.COMMAND)
                Log.d(getTag(), "No arguments");
            invalid = true;
            return error = CodelessProfile.NO_ARGUMENTS;
        }

        if (!checkArgumentsCount()) {
            if (CodelessLibLog.COMMAND)
                Log.d(getTag(), "Wrong number of arguments");
            invalid = true;
            return error = CodelessProfile.WRONG_NUMBER_OF_ARGUMENTS;
        }

        matcher = pattern.matcher(command);
        if (!matcher.matches()) {
            if (CodelessLibLog.COMMAND)
                Log.d(getTag(), "Command pattern match failed");
            invalid = true;
            return error = CodelessProfile.INVALID_ARGUMENTS;
        }

        String msg = parseArguments();
        if (msg != null) {
            if (CodelessLibLog.COMMAND)
                Log.d(getTag(), "Invalid arguments: " + msg);
            error = msg;
            invalid = true;
        }
        return msg;
    }

    protected boolean requiresArguments() {
        return false;
    }

    protected boolean checkArgumentsCount() {
        return true;
    }

    protected String parseArguments() {
        return null;
    }

    public void processInbound() {
        sendSuccess();
    }

    public void sendSuccess() {
        complete = true;
        manager.sendSuccess();
    }

    public void sendSuccess(String response) {
        complete = true;
        manager.sendSuccess(response);
    }

    public void sendError(String msg) {
        complete = true;
        error = msg;
        manager.sendError(CodelessProfile.ERROR_PREFIX + msg);
    }

    public void sendResponse(String response, boolean more) {
        manager.sendResponse(response);
        if (!more) {
            sendSuccess();
        }
    }

    protected Integer decodeNumberArgument(int group) {
        try {
            return Integer.decode(matcher.group(group));
        } catch (NumberFormatException e) {
            if (CodelessLibLog.COMMAND)
                Log.d(getTag(), "Invalid number argument: " + e.getMessage());
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + getName() + "]";
    }
}
