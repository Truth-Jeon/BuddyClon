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
import com.diasemi.codelesslib.CodelessProfile.CommandID;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.regex.Pattern;

public class MemStoreCommand extends CodelessCommand {
    public static final String TAG = "MemStoreCommand";

    public static final String COMMAND = "MEM";
    public static final String NAME = CodelessProfile.PREFIX_LOCAL + COMMAND;
    public static final CommandID ID = CommandID.MEM;

    public static final String PATTERN_STRING = "^MEM=(\\d+)(?:,(.*))?$"; // <index> <text>
    public static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private int memIndex;
    private String text;

    public MemStoreCommand(CodelessManager manager, int memIndex) {
        this(manager);
        setMemIndex(memIndex);
    }

    public MemStoreCommand(CodelessManager manager, int memIndex, String text) {
        this(manager, memIndex);
        this.text = text;
    }

    public MemStoreCommand(CodelessManager manager) {
        super(manager);
    }

    public MemStoreCommand(CodelessManager manager, String command, boolean parse) {
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
        String arguments = String.format(Locale.US, "%d", memIndex);
        if (text != null)
            arguments += "," + text;
        return arguments;
    }

    @Override
    public void parseResponse(String response) {
        super.parseResponse(response);
        if (responseLine() == 1) {
            text = response;
            if (CodelessLibLog.COMMAND)
                Log.d(TAG, "Memory index: " + memIndex + " contains: " + text);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        if (isValid())
            EventBus.getDefault().post(new CodelessEvent.MemoryTextContent(this));
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
        if (num == null || CodelessLibConfig.CHECK_MEM_INDEX && (num < CodelessLibConfig.MEM_INDEX_MIN || num > CodelessLibConfig.MEM_INDEX_MAX))
            return "Invalid memory index";
        memIndex = num;

        if (CodelessProfile.countArguments(command, ",") == 1)
            return null;
        text = matcher.group(2);
        if (CodelessLibConfig.CHECK_MEM_CONTENT_SIZE && text.length() > CodelessLibConfig.MEM_MAX_CHAR_COUNT)
            return "Text exceeds max character number";

        return null;
    }

    public int getMemIndex() {
        return memIndex;
    }

    public void setMemIndex(int memIndex) {
        this.memIndex = memIndex;
        if (CodelessLibConfig.CHECK_MEM_INDEX) {
            if (memIndex < CodelessLibConfig.MEM_INDEX_MIN || memIndex > CodelessLibConfig.MEM_INDEX_MAX)
                invalid = true;
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        if (CodelessLibConfig.CHECK_MEM_CONTENT_SIZE && text.length() > CodelessLibConfig.MEM_MAX_CHAR_COUNT)
            invalid = true;
    }
}
