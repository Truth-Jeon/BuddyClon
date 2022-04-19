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

import android.os.Environment;

import com.diasemi.codelesslib.CodelessProfile.Command;
import com.diasemi.codelesslib.CodelessProfile.CommandID;
import com.diasemi.codelesslib.CodelessProfile.GPIO;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

public class CodelessLibConfig {

    public static final String CODELESS_LIB_INFO = null; // ATI command response (if null, use version)

    public static final String LOG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Dialog Semiconductor/SmartConsole/log";
    public static final SimpleDateFormat LOG_FILE_DATE = new SimpleDateFormat("yyyy-MM-dd'_'HH.mm.ss", Locale.US);
    public static final boolean LOG_FILE_ADDRESS_SUFFIX = true;
    public static final String LOG_FILE_EXTENSION = ".txt";
    public static final boolean CODELESS_LOG = true;
    public static final boolean CODELESS_LOG_FLUSH = true;
    public static final String CODELESS_LOG_FILE_PREFIX = "Codeless_";
    public static final String CODELESS_LOG_PREFIX_TEXT = "";
    public static final String CODELESS_LOG_PREFIX_OUTBOUND = ">> ";
    public static final String CODELESS_LOG_PREFIX_INBOUND = "<< ";
    public static final boolean DSPS_RX_LOG = true;
    public static final boolean DSPS_RX_LOG_FLUSH = true;
    public static final String DSPS_RX_LOG_FILE_PREFIX = "DSPS_RX_";

    public static final boolean GATT_QUEUE_PRIORITY = true;
    public static final boolean GATT_DEQUEUE_BEFORE_PROCESSING = true;
    public static final boolean BLUETOOTH_STATE_MONITOR = true;

    // WARNING: Modifying these may cause parse failure on peer device.
    public static final Charset CHARSET = StandardCharsets.US_ASCII;
    public static final String END_OF_LINE = "\r\n";
    public static final boolean APPEND_END_OF_LINE = true;
    public static final boolean END_OF_LINE_AFTER_COMMAND = false;
    public static final boolean EMPTY_LINE_BEFORE_OK = true;
    public static final boolean EMPTY_LINE_BEFORE_ERROR = true;
    public static final boolean TRAILING_ZERO = true;
    public static final boolean SINGLE_WRITE_RESPONSE = true; // Use single write operation to send response (merge lines)

    public static final boolean DISALLOW_INVALID_PARSED_COMMAND = false;
    public static final boolean DISALLOW_INVALID_COMMAND = true;
    public static final boolean DISALLOW_INVALID_PREFIX = true;
    public static final boolean AUTO_ADD_PREFIX = true;

    public static final boolean LINE_EVENTS = true;

    public static final boolean START_IN_COMMAND_MODE = true;
    public static final boolean HOST_BINARY_REQUEST = true;
    public static final boolean MODE_CHANGE_SEND_BINARY_REQUEST = true;
    public static final boolean ALLOW_INBOUND_BINARY_IN_COMMAND_MODE = false;
    public static final boolean ALLOW_OUTBOUND_BINARY_IN_COMMAND_MODE = false;
    public static final boolean ALLOW_INBOUND_COMMAND_IN_BINARY_MODE = false;
    public static final boolean ALLOW_OUTBOUND_COMMAND_IN_BINARY_MODE = false;

    public static final boolean REQUEST_MTU = true;
    public static final int MTU = 517;

    public static final int DEFAULT_DSPS_CHUNK_SIZE = 128;
    public static final boolean DSPS_CHUNK_SIZE_INCREASE_TO_MTU = true;
    public static final int DSPS_PENDING_MAX_SIZE = 1000;
    public static final boolean DEFAULT_DSPS_RX_FLOW_CONTROL = true;
    public static final boolean DEFAULT_DSPS_TX_FLOW_CONTROL = true;
    public static final boolean SET_FLOW_CONTROL_ON_CONNECTION = true;

    public static final int DSPS_PATTERN_DIGITS = 4;
    public static final byte[] DSPS_PATTERN_SUFFIX = new byte[] { 0x0a };

    public static final String DSPS_RX_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Dialog Semiconductor/SmartConsole/files";
    public static final boolean DSPS_RX_FILE_LOG_DATA = false; // Log file data to DSPS RX log file (if enabled)
    public static final String DSPS_RX_FILE_HEADER_PATTERN_STRING = "(?s)(.{0,100})Name:\\s*(\\S{1,100})\\s*Size:\\s*(\\d{1,9})\\s*(?:CRC:\\s*([0-9a-f]{8})\\s*)?(?:\\x00|END\\s*)(.*)"; // <ignored> <name> <size> <crc> <data>
    public static final Pattern DSPS_RX_FILE_HEADER_PATTERN = Pattern.compile(DSPS_RX_FILE_HEADER_PATTERN_STRING, Pattern.CASE_INSENSITIVE);

    public static final boolean DSPS_STATS = true;
    public static final int DSPS_STATS_INTERVAL = 1000; // ms

    public static final boolean CHECK_TIMER_INDEX = true;
    public static final int TIMER_INDEX_MIN = 0;
    public static final int TIMER_INDEX_MAX = 3;

    public static final boolean CHECK_COMMAND_INDEX = true;
    public static final int COMMAND_INDEX_MIN = 0;
    public static final int COMMAND_INDEX_MAX = 3;

    public static final boolean CHECK_GPIO_FUNCTION = true;
    public static final int GPIO_FUNCTION_MIN = Command.GPIO_FUNCTION_UNDEFINED;
    public static final int GPIO_FUNCTION_MAX = Command.GPIO_FUNCTION_NOT_AVAILABLE;

    public static final boolean CHECK_ANALOG_INPUT_GPIO = true;
    public static final GPIO[] ANALOG_INPUT_GPIO = {
            new GPIO(0, 0), new GPIO(0, 1), new GPIO(0, 2), new GPIO(0, 3),
    };

    public static final boolean CHECK_MEM_INDEX = true;
    public static final int MEM_INDEX_MIN = 0;
    public static final int MEM_INDEX_MAX = 3;

    public static final boolean CHECK_MEM_CONTENT_SIZE = true;
    public static final int MEM_MAX_CHAR_COUNT = 100;

    public static final boolean CHECK_COMMAND_STORE_INDEX = true;
    public static final int COMMAND_STORE_INDEX_MIN = 0;
    public static final int COMMAND_STORE_INDEX_MAX = 3;

    public static final boolean CHECK_ADVERTISING_INTERVAL = true;
    public static final int ADVERTISING_INTERVAL_MIN = 100; // ms
    public static final int ADVERTISING_INTERVAL_MAX = 3000; // ms

    public static final boolean CHECK_SPI_WORD_SIZE = true;
    public static final int SPI_WORD_SIZE = 8; // bits

    public static final boolean CHECK_SPI_HEX_STRING_WRITE = true;
    public static final int SPI_HEX_STRING_CHAR_SIZE_MIN = 2;
    public static final int SPI_HEX_STRING_CHAR_SIZE_MAX = 64;

    public static final boolean CHECK_SPI_READ_SIZE = true;
    public static final int SPI_MAX_BYTE_READ_SIZE = 64;

    public static final boolean CHECK_PWM_FREQUENCY = true;
    public static final int PWM_FREQUENCY_MIN = 1000;
    public static final int PWM_FREQUENCY_MAX = 500000;

    public static final boolean CHECK_PWM_DUTY_CYCLE = true;
    public static final int PWM_DUTY_CYCLE_MIN = 0;
    public static final int PWM_DUTY_CYCLE_MAX = 100;

    public static final boolean CHECK_PWM_DURATION = true;
    public static final int PWM_DURATION_MIN = 100;
    public static final int PWM_DURATION_MAX = 10000;

    public static final boolean CHECK_BONDING_DATABASE_INDEX = true;
    public static final int BONDING_DATABASE_INDEX_MIN = 1;
    public static final int BONDING_DATABASE_INDEX_MAX = 5;
    public static final int BONDING_DATABASE_ALL_VALUES = 0xff;

    // GPIO configurations
    public static final GPIO[] GPIO_LIST_585 = {
            // Port 0, Pin 0-7, 8-9 not used
            new GPIO(0, 0), new GPIO(0, 1), new GPIO(0, 2), new GPIO(0, 3),
            new GPIO(0, 4), new GPIO(0, 5), new GPIO(0, 6), new GPIO(0, 7),
            null, null,
            // Port 1, Pin 0-5, 6-9 not used
            new GPIO(1, 0), new GPIO(1, 1), new GPIO(1, 2), new GPIO(1, 3),
            new GPIO(1, 4), new GPIO(1, 5),
            null, null, null, null,
            // Port 2, Pin 0-9
            new GPIO(2, 0), new GPIO(2, 1), new GPIO(2, 2), new GPIO(2, 3),
            new GPIO(2, 4), new GPIO(2, 5), new GPIO(2, 6), new GPIO(2, 7),
            new GPIO(2, 8), new GPIO(2, 9),
            // Port 3, Pin 0, 1-6 not used
            new GPIO(3, 0),
            null, null, null, null, null, null
    };
    public static final GPIO[] GPIO_LIST_531 = {
            // Port 0, Pin 0-11
            new GPIO(0, 0), new GPIO(0, 1), new GPIO(0, 2), new GPIO(0, 3),
            new GPIO(0, 4), new GPIO(0, 5), new GPIO(0, 6), new GPIO(0, 7),
            new GPIO(0, 8), new GPIO(0, 9), new GPIO(0, 10), new GPIO(0, 11)
    };
    public static final GPIO[][] GPIO_CONFIGURATIONS = {
            GPIO_LIST_585,
            GPIO_LIST_531,
    };

    // Commands to be process by the library.
    public static final HashSet<CommandID> supportedCommands = new HashSet<>();
    static {
        supportedCommands.add(CommandID.AT);
        supportedCommands.add(CommandID.ATI);
        supportedCommands.add(CommandID.BINREQ);
        supportedCommands.add(CommandID.BINREQACK);
        supportedCommands.add(CommandID.BINREQEXIT);
        supportedCommands.add(CommandID.BINREQEXITACK);
        supportedCommands.add(CommandID.RANDOM);
        supportedCommands.add(CommandID.BATT);
        supportedCommands.add(CommandID.BDADDR);
        supportedCommands.add(CommandID.GAPSTATUS);
        supportedCommands.add(CommandID.PRINT);
    }

    // Commands to be sent to the app for processing.
    // App is responsible for sending a proper response.
    public static final HashSet<CommandID> hostCommands = new HashSet<>();
    static {
    }

    public static final boolean HOST_UNSUPPORTED_COMMANDS = false;
    public static final boolean HOST_INVALID_COMMANDS = false;
}
