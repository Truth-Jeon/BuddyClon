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

import com.diasemi.codelesslib.command.AdcReadCommand;
import com.diasemi.codelesslib.command.AdvertisingDataCommand;
import com.diasemi.codelesslib.command.AdvertisingResponseCommand;
import com.diasemi.codelesslib.command.AdvertisingStartCommand;
import com.diasemi.codelesslib.command.AdvertisingStopCommand;
import com.diasemi.codelesslib.command.BasicCommand;
import com.diasemi.codelesslib.command.BatteryLevelCommand;
import com.diasemi.codelesslib.command.BaudRateCommand;
import com.diasemi.codelesslib.command.BinEscCommand;
import com.diasemi.codelesslib.command.BinExitAckCommand;
import com.diasemi.codelesslib.command.BinExitCommand;
import com.diasemi.codelesslib.command.BinRequestAckCommand;
import com.diasemi.codelesslib.command.BinRequestCommand;
import com.diasemi.codelesslib.command.BinResumeCommand;
import com.diasemi.codelesslib.command.BluetoothAddressCommand;
import com.diasemi.codelesslib.command.BondingEntryClearCommand;
import com.diasemi.codelesslib.command.BondingEntryStatusCommand;
import com.diasemi.codelesslib.command.BondingEntryTransferCommand;
import com.diasemi.codelesslib.command.BroadcasterRoleSetCommand;
import com.diasemi.codelesslib.command.CentralRoleSetCommand;
import com.diasemi.codelesslib.command.CmdGetCommand;
import com.diasemi.codelesslib.command.CmdPlayCommand;
import com.diasemi.codelesslib.command.CmdStoreCommand;
import com.diasemi.codelesslib.command.CodelessCommand;
import com.diasemi.codelesslib.command.ConnectionParametersCommand;
import com.diasemi.codelesslib.command.CursorCommand;
import com.diasemi.codelesslib.command.CustomCommand;
import com.diasemi.codelesslib.command.DataLengthEnableCommand;
import com.diasemi.codelesslib.command.DeviceInformationCommand;
import com.diasemi.codelesslib.command.DeviceSleepCommand;
import com.diasemi.codelesslib.command.ErrorReportingCommand;
import com.diasemi.codelesslib.command.EventConfigCommand;
import com.diasemi.codelesslib.command.EventHandlerCommand;
import com.diasemi.codelesslib.command.FlowControlCommand;
import com.diasemi.codelesslib.command.GapConnectCommand;
import com.diasemi.codelesslib.command.GapDisconnectCommand;
import com.diasemi.codelesslib.command.GapScanCommand;
import com.diasemi.codelesslib.command.GapStatusCommand;
import com.diasemi.codelesslib.command.HeartbeatCommand;
import com.diasemi.codelesslib.command.HostSleepCommand;
import com.diasemi.codelesslib.command.I2cConfigCommand;
import com.diasemi.codelesslib.command.I2cReadCommand;
import com.diasemi.codelesslib.command.I2cScanCommand;
import com.diasemi.codelesslib.command.I2cWriteCommand;
import com.diasemi.codelesslib.command.IoConfigCommand;
import com.diasemi.codelesslib.command.IoStatusCommand;
import com.diasemi.codelesslib.command.MaxMtuCommand;
import com.diasemi.codelesslib.command.MemStoreCommand;
import com.diasemi.codelesslib.command.PeripheralRoleSetCommand;
import com.diasemi.codelesslib.command.PinCodeCommand;
import com.diasemi.codelesslib.command.PowerLevelConfigCommand;
import com.diasemi.codelesslib.command.PulseGenerationCommand;
import com.diasemi.codelesslib.command.RandomNumberCommand;
import com.diasemi.codelesslib.command.ResetCommand;
import com.diasemi.codelesslib.command.ResetIoConfigCommand;
import com.diasemi.codelesslib.command.RssiCommand;
import com.diasemi.codelesslib.command.SecurityModeCommand;
import com.diasemi.codelesslib.command.SpiConfigCommand;
import com.diasemi.codelesslib.command.SpiReadCommand;
import com.diasemi.codelesslib.command.SpiTransferCommand;
import com.diasemi.codelesslib.command.SpiWriteCommand;
import com.diasemi.codelesslib.command.TimerStartCommand;
import com.diasemi.codelesslib.command.TimerStopCommand;
import com.diasemi.codelesslib.command.UartEchoCommand;
import com.diasemi.codelesslib.command.UartPrintCommand;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CodelessProfile {
    private final static String TAG = "CodelessProfile";

    public static class Uuid {
        public static final UUID CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        // Codeless
        public static final UUID CODELESS_SERVICE_UUID = UUID.fromString("866d3b04-e674-40dc-9c05-b7f91bec6e83");
        public static final UUID CODELESS_INBOUND_COMMAND_UUID = UUID.fromString("914f8fb9-e8cd-411d-b7d1-14594de45425");
        public static final UUID CODELESS_OUTBOUND_COMMAND_UUID = UUID.fromString("3bb535aa-50b2-4fbe-aa09-6b06dc59a404");
        public static final UUID CODELESS_FLOW_CONTROL_UUID = UUID.fromString("e2048b39-d4f9-4a45-9f25-1856c10d5639");

        // DSPS
        public static final UUID DSPS_SERVICE_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb7");
        public static final UUID DSPS_SERVER_TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
        public static final UUID DSPS_SERVER_RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
        public static final UUID DSPS_FLOW_CONTROL_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb9");

        // Other
        public static final UUID SUOTA_SERVICE_UUID = UUID.fromString("0000fef5-0000-1000-8000-00805f9b34fb");
        public static final UUID IOT_SERVICE_UUID = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402400");
        public static final UUID WEARABLES_580_SERVICE_UUID = UUID.fromString("00002800-0000-1000-8000-00805f9b34fb");
        public static final UUID WEARABLES_680_SERVICE_UUID = UUID.fromString("00002ea7-0000-1000-8000-00805f9b34fb");
        public static final UUID MESH_PROVISIONING_SERVICE_UUID = UUID.fromString("00001827-0000-1000-8000-00805f9b34fb");
        public static final UUID MESH_PROXY_SERVICE_UUID = UUID.fromString("00001828-0000-1000-8000-00805f9b34fb");
        public static final UUID IMMEDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
        public static final UUID LINK_LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");

        // Device information service
        public static final UUID DEVICE_INFORMATION_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
        public static final UUID MANUFACTURER_NAME_STRING = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb");
        public static final UUID MODEL_NUMBER_STRING = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb");
        public static final UUID SERIAL_NUMBER_STRING = UUID.fromString("00002A25-0000-1000-8000-00805f9b34fb");
        public static final UUID HARDWARE_REVISION_STRING = UUID.fromString("00002A27-0000-1000-8000-00805f9b34fb");
        public static final UUID FIRMWARE_REVISION_STRING = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");
        public static final UUID SOFTWARE_REVISION_STRING = UUID.fromString("00002A28-0000-1000-8000-00805f9b34fb");
        public static final UUID SYSTEM_ID = UUID.fromString("00002A23-0000-1000-8000-00805f9b34fb");
        public static final UUID IEEE_11073 = UUID.fromString("00002A2A-0000-1000-8000-00805f9b34fb");
        public static final UUID PNP_ID = UUID.fromString("00002A50-0000-1000-8000-00805f9b34fb");

        // GAP
        public static final UUID GAP_SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
        public static final UUID GAP_DEVICE_NAME = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb");
    }

    public static final int MTU_DEFAULT = 23;

    // DSPS flow control
    public static final int DSPS_XON = 0x01;
    public static final int DSPS_XOFF = 0x02;

    // Codeless flow control
    public static final int CODELESS_DATA_PENDING = 0x01;

    public static final String PREFIX = "AT";
    public static final String PREFIX_LOCAL = PREFIX + "+";
    public static final String PREFIX_REMOTE = PREFIX + "r";
    public static final String PREFIX_PATTERN_STRING = "^" + PREFIX + "(?:\\+|r\\+?)?";
    public static final Pattern PREFIX_PATTERN = Pattern.compile("(" + PREFIX_PATTERN_STRING + ").*"); // <prefix>
    public static final String COMMAND_PATTERN_STRING = PREFIX_PATTERN_STRING + "([^=]*)=?.*"; // <command>
    public static final Pattern COMMAND_PATTERN = Pattern.compile(COMMAND_PATTERN_STRING);
    public static final String COMMAND_WITH_ARGUMENTS_PREFIX_PATTERN_STRING = "^(?:" + PREFIX_PATTERN_STRING + ")?([^=]*)="; // <command>
    public static final Pattern COMMAND_WITH_ARGUMENTS_PREFIX_PATTERN = Pattern.compile(COMMAND_WITH_ARGUMENTS_PREFIX_PATTERN_STRING);
    public static final String COMMAND_WITH_ARGUMENTS_PATTERN_STRING = COMMAND_WITH_ARGUMENTS_PREFIX_PATTERN_STRING + ".*";
    public static final Pattern COMMAND_WITH_ARGUMENTS_PATTERN = Pattern.compile(COMMAND_WITH_ARGUMENTS_PATTERN_STRING);

    public static boolean hasPrefix(String command) {
        return PREFIX_PATTERN.matcher(command).matches();
    }

    public static String getPrefix(String command) {
        Matcher matcher = PREFIX_PATTERN.matcher(command);
        return matcher.matches() ? matcher.group(1) : null;
    }

    public static boolean isCommand(String command) {
        return COMMAND_PATTERN.matcher(command).matches();
    }

    public static String getCommand(String command) {
        Matcher matcher = COMMAND_PATTERN.matcher(command);
        return matcher.matches() ? matcher.group(1) : null;
    }

    public static String removeCommandPrefix(String command) {
        return command.replaceFirst(CodelessProfile.PREFIX_PATTERN_STRING, "");
    }

    public static boolean hasArguments(String command) {
        return COMMAND_WITH_ARGUMENTS_PATTERN.matcher(command).matches();
    }

    public static int countArguments(String command, String split) {
        return !hasArguments(command) ? 0 : command.replaceFirst(COMMAND_WITH_ARGUMENTS_PREFIX_PATTERN_STRING, "").split(split, -1).length;
    }

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String ERROR_PREFIX = "ERROR: ";
    public static final String INVALID_COMMAND = "Invalid command";
    public static final String COMMAND_NOT_SUPPORTED = "Command not supported";
    public static final String NO_ARGUMENTS = "No arguments";
    public static final String WRONG_NUMBER_OF_ARGUMENTS = "Wrong number of arguments";
    public static final String INVALID_ARGUMENTS = "Invalid arguments";
    public static final String GATT_OPERATION_ERROR = "Gatt operation error";
    public static final String ERROR_MESSAGE_PATTERN_STRING = "^(?:ERROR|INVALID COMMAND|EC\\d{1,8}:).*";
    public static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile(ERROR_MESSAGE_PATTERN_STRING);
    public static final String PEER_INVALID_COMMAND = "INVALID COMMAND";
    public static final String ERROR_CODE_PATTERN_STRING = "^EC(\\d{1,8}):\\s*(.*)"; // <code> <message>
    public static final Pattern ERROR_CODE_PATTERN = Pattern.compile(ERROR_CODE_PATTERN_STRING);

    public static boolean isSuccess(String response) {
        return response.equals(OK);
    }

    public static boolean isError(String response) {
        return response.equals(ERROR);
    }

    public static boolean isErrorMessage(String response) {
        return ERROR_MESSAGE_PATTERN.matcher(response).matches();
    }

    public static boolean isPeerInvalidCommand(String error) {
        return error.startsWith(PEER_INVALID_COMMAND);
    }

    public static class ErrorCodeMessage {
        public int code;
        public String message;

        public ErrorCodeMessage(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static boolean isErrorCodeMessage(String error) {
        return ERROR_CODE_PATTERN.matcher(error).matches();
    }

    public static ErrorCodeMessage parseErrorCodeMessage(String error) {
        Matcher matcher = ERROR_CODE_PATTERN.matcher(error);
        return matcher.matches() ? new ErrorCodeMessage(Integer.parseInt(matcher.group(1)), matcher.group(2)) : null;
    }

    public enum LineType {
        InboundCommand,
        InboundResponse,
        InboundOK,
        InboundError,
        InboundEmpty,
        OutboundCommand,
        OutboundResponse,
        OutboundOK,
        OutboundError,
        OutboundEmpty;

        public boolean isInbound() {
            return this == InboundCommand || this == InboundResponse || this == InboundOK || this == InboundError || this == InboundEmpty;
        }

        public boolean isOutbound() {
            return this == OutboundCommand || this == OutboundResponse || this == OutboundOK || this == OutboundError || this == OutboundEmpty;
        }

        public boolean isCommand() {
            return this == InboundCommand || this == OutboundCommand;
        }

        public boolean isResponse() {
            return this == InboundResponse || this == OutboundResponse;
        }

        public boolean isOK() {
            return this == InboundOK || this == OutboundOK;
        }

        public boolean isError() {
            return this == InboundError || this == OutboundError;
        }

        public boolean isEmpty() {
            return this == InboundEmpty || this == OutboundEmpty;
        }
    }

    public static class Line {

        private String text;
        private LineType type;

        public Line(String text, LineType type) {
            this.text = text;
            this.type = type;
        }

        public Line(LineType type) {
            this.text = "";
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public LineType getType() {
            return type;
        }
    }

    public static class GPIO {

        public static final int INVALID = -1;

        public int port = INVALID;
        public int pin = INVALID;
        public int state = INVALID;
        public int function = INVALID;
        public int level = INVALID;

        public GPIO() {
        }

        public GPIO(int port, int pin) {
            this.port = port;
            this.pin = pin;
        }

        public GPIO(int port, int pin, int function) {
            this(port, pin);
            this.function = function;
        }

        public GPIO(int port, int pin, int function, int level) {
            this(port, pin, function);
            this.level = level;
        }

        public GPIO(int pack) {
            setGpio(pack);
        }

        public GPIO(GPIO gpio) {
            this(gpio.port, gpio.pin, gpio.function, gpio.level);
            state = gpio.state;
        }

        public GPIO(GPIO gpio, int function) {
            this(gpio.port, gpio.pin, function);
        }

        public GPIO(GPIO gpio, int function, int level) {
            this(gpio.port, gpio.pin, function, level);
        }

        public void update(GPIO gpio) {
            if (!equals(gpio))
                return;
            if (gpio.validFunction()) {
                if (function != gpio.function) {
                    level = INVALID;
                    state = INVALID;
                }
                function = gpio.function;
            }
            if (gpio.validLevel())
                level = gpio.level;
            if (gpio.validState())
                state = gpio.state;
        }

        public GPIO pin() {
            return new GPIO(port, pin);
        }

        public boolean validGpio() {
            return port != INVALID && pin != INVALID;
        }

        public int getGpio() {
            return Command.gpioPack(port, pin);
        }

        public void setGpio(int pack) {
            port = Command.gpioGetPort(pack);
            pin = Command.gpioGetPin(pack);
        }

        public void setGpio(int port, int pin) {
            this.port = port;
            this.pin = pin;
        }

        public boolean validState() {
            return state != INVALID;
        }

        public boolean isLow() {
            return state == Command.PIN_STATUS_LOW;
        }

        public boolean isHigh() {
            return state == Command.PIN_STATUS_HIGH;
        }

        public boolean isBinary() {
            return isLow() || isHigh();
        }

        public void setLow() {
            state = Command.PIN_STATUS_LOW;
        }

        public void setHigh() {
            state = Command.PIN_STATUS_HIGH;
        }

        public void setStatus(boolean status) {
            state = status ? Command.PIN_STATUS_HIGH : Command.PIN_STATUS_LOW;
        }

        public boolean validFunction() {
            return function != INVALID;
        }

        public boolean isInput() {
            return function == Command.GPIO_FUNCTION_INPUT || function == Command.GPIO_FUNCTION_INPUT_PULL_UP || function == Command.GPIO_FUNCTION_INPUT_PULL_DOWN;
        }

        public boolean isOutput() {
            return function == Command.GPIO_FUNCTION_OUTPUT;
        }

        public boolean isAnalog() {
            return function == Command.GPIO_FUNCTION_ANALOG_INPUT || function == Command.GPIO_FUNCTION_ANALOG_INPUT_ATTENUATION;
        }

        public boolean isPwm() {
            return function == Command.GPIO_FUNCTION_PWM || function == Command.GPIO_FUNCTION_PWM1
                    || function == Command.GPIO_FUNCTION_PWM2 || function == Command.GPIO_FUNCTION_PWM3;
        }

        public boolean isI2c() {
            return function == Command.GPIO_FUNCTION_I2C_CLK || function == Command.GPIO_FUNCTION_I2C_SDA;
        }

        public boolean isSpi() {
            return function == Command.GPIO_FUNCTION_SPI_CLK || function == Command.GPIO_FUNCTION_SPI_CS
                    || function == Command.GPIO_FUNCTION_SPI_MISO || function == Command.GPIO_FUNCTION_SPI_MOSI;
        }

        public boolean isUart() {
            return function == Command.GPIO_FUNCTION_UART_CTS || function == Command.GPIO_FUNCTION_UART_RTS
                    || function == Command.GPIO_FUNCTION_UART_RX || function == Command.GPIO_FUNCTION_UART_TX
                    || function == Command.GPIO_FUNCTION_UART2_CTS || function == Command.GPIO_FUNCTION_UART2_RTS
                    || function == Command.GPIO_FUNCTION_UART2_RX || function == Command.GPIO_FUNCTION_UART2_TX;
        }

        public boolean validLevel() {
            return level != INVALID;
        }

        public static ArrayList<GPIO> copyConfig(ArrayList<GPIO> config) {
            ArrayList<GPIO> copy = new ArrayList<>(config.size());
            for (GPIO gpio : config)
                copy.add(new GPIO(gpio));
            return copy;
        }

        public static ArrayList<GPIO> updateConfig(ArrayList<GPIO> config, ArrayList<GPIO> update) {
            if (config == null || !Arrays.equals(config.toArray(), update.toArray()))
                return copyConfig(update);
            for (int i = 0; i < config.size(); i++)
                config.get(i).update(update.get(i));
            return config;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof GPIO) {
                GPIO gpio = (GPIO) obj;
                return port == gpio.port && pin == gpio.pin;
            } else if (obj instanceof Integer) {
                return getGpio() == (Integer) obj;
            }
            return super.equals(obj);
        }

        public String name() {
            return "P" + port + "_" + pin;
        }

        @NonNull
        @Override
        public String toString() {
            return name() + (validFunction() ? "(" + function + ")" : "");
        }
    }

    public static class EventConfig {
        public int type;
        public boolean status;

        public EventConfig() {
        }

        public EventConfig(int type, boolean status){
            this.type = type;
            this.status = status;
        }
    }

    public static class GapScannedDevice {
        public String address;
        public int addressType;
        public int type;
        public int rssi;
    }

    public static class EventHandler {
        public int event;
        public ArrayList<CodelessCommand> commands;
    }

    public static class BondingEntry {
        public byte[] ltk;
        public int ediv;
        public byte[] rand;
        public int keySize;
        public byte[] csrk;
        public byte[] bluetoothAddress;
        public int addressType;
        public int authenticationLevel;
        public int bondingDatabaseSlot;
        public byte[] irk;
        public int persistenceStatus;
        public byte[] timestamp;
    }

    public static class Command {

        // ATE
        public static final int UART_ECHO_OFF = 0;
        public static final int UART_ECHO_ON = 1;

        // ATF
        public static final int ERROR_REPORTING_OFF = 0;
        public static final int ERROR_REPORTING_ON = 1;

        // Flow control
        public static final int DISABLE_UART_FLOW_CONTROL = 0;
        public static final int ENABLE_UART_FLOW_CONTROL = 1;

        // Sleep
        public static final int AWAKE_DEVICE = 0;
        public static final int PUT_DEVICE_IN_SLEEP = 1;

        // BINESC
        public static final int BINESC_TIME_PRIOR_DEFAULT = 1000;
        public static final int BINESC_TIME_AFTER_DEFAULT = 1000;

        // GPIO
        public static final int GPIO_FUNCTION_UNDEFINED = 0;
        public static final int GPIO_FUNCTION_INPUT = 1;
        public static final int GPIO_FUNCTION_INPUT_PULL_UP = 2;
        public static final int GPIO_FUNCTION_INPUT_PULL_DOWN = 3;
        public static final int GPIO_FUNCTION_OUTPUT = 4;
        public static final int GPIO_FUNCTION_ANALOG_INPUT = 5;
        public static final int GPIO_FUNCTION_ANALOG_INPUT_ATTENUATION = 6;
        public static final int GPIO_FUNCTION_I2C_CLK = 7;
        public static final int GPIO_FUNCTION_I2C_SDA = 8;
        public static final int GPIO_FUNCTION_CONNECTION_INDICATOR_HIGH = 9;
        public static final int GPIO_FUNCTION_CONNECTION_INDICATOR_LOW = 10;
        public static final int GPIO_FUNCTION_UART_TX = 11;
        public static final int GPIO_FUNCTION_UART_RX = 12;
        public static final int GPIO_FUNCTION_UART_CTS = 13;
        public static final int GPIO_FUNCTION_UART_RTS = 14;
        public static final int GPIO_FUNCTION_UART2_TX = 15; // Reserved
        public static final int GPIO_FUNCTION_UART2_RX = 16; // Reserved
        public static final int GPIO_FUNCTION_UART2_CTS = 17; // Reserved
        public static final int GPIO_FUNCTION_UART2_RTS = 18; // Reserved
        public static final int GPIO_FUNCTION_SPI_CLK = 19;
        public static final int GPIO_FUNCTION_SPI_CS = 20;
        public static final int GPIO_FUNCTION_SPI_MOSI = 21;
        public static final int GPIO_FUNCTION_SPI_MISO = 22;
        public static final int GPIO_FUNCTION_PWM1 = 23; // Reserved
        public static final int GPIO_FUNCTION_PWM = 24;
        public static final int GPIO_FUNCTION_PWM2 = 25; // Reserved
        public static final int GPIO_FUNCTION_PWM3 = 26; // Reserved
        public static final int GPIO_FUNCTION_HEARTBEAT = 27;
        public static final int GPIO_FUNCTION_NOT_AVAILABLE = 28;

        public static final int PIN_STATUS_LOW = 0;
        public static final int PIN_STATUS_HIGH = 1;

        public static boolean isBinaryState(int state) {
            return state == PIN_STATUS_HIGH || state == PIN_STATUS_LOW;
        }

        public static int gpioPack(int port, int pin) {
            return port * 10 + pin;
        }

        public static int gpioGetPort(int pack) {
            return pack / 10;
        }

        public static int gpioGetPin(int pack) {
            return pack % 10;
        }

        // GAP
        public static final int GAP_ROLE_PERIPHERAL = 0;
        public static final int GAP_ROLE_CENTRAL = 1;
        public static final int GAP_STATUS_DISCONNECTED = 0;
        public static final int GAP_STATUS_CONNECTED = 1;
        public static final String GAP_ADDRESS_TYPE_PUBLIC_STRING = "P";
        public static final String GAP_ADDRESS_TYPE_RANDOM_STRING = "R";
        public static final int GAP_ADDRESS_TYPE_PUBLIC = 0;
        public static final int GAP_ADDRESS_TYPE_RANDOM = 1;
        public static final String GAP_SCAN_TYPE_ADV_STRING = "ADV";
        public static final String GAP_SCAN_TYPE_RSP_STRING = "RSP";
        public static final int GAP_SCAN_TYPE_ADV = 0;
        public static final int GAP_SCAN_TYPE_RSP = 1;

        // Connection Parameters
        public static final int CONNECTION_INTERVAL_MIN = 6;
        public static final int CONNECTION_INTERVAL_MAX = 3200;
        public static final int SLAVE_LATENCY_MIN = 0;
        public static final int SLAVE_LATENCY_MAX = 500;
        public static final int SUPERVISION_TIMEOUT_MIN = 10;
        public static final int SUPERVISION_TIMEOUT_MAX = 3200;

        public static final int PARAMETER_UPDATE_DISABLE = 0;
        public static final int PARAMETER_UPDATE_ON_CONNECTION = 1;
        public static final int PARAMETER_UPDATE_NOW_ONLY = 2;
        public static final int PARAMETER_UPDATE_NOW_SAVE = 3;
        public static final int PARAMETER_UPDATE_ACTION_MIN = PARAMETER_UPDATE_DISABLE;
        public static final int PARAMETER_UPDATE_ACTION_MAX = PARAMETER_UPDATE_NOW_SAVE;

        // MTU
        public static final int MTU_MIN = 23;
        public static final int MTU_MAX = 512;

        // DLE
        public static final int DLE_DISABLED = 0;
        public static final int DLE_ENABLED = 1;
        public static final int DLE_PACKET_LENGTH_MIN = 27;
        public static final int DLE_PACKET_LENGTH_MAX = 251;
        public static final int DLE_PACKET_LENGTH_DEFAULT = 251;

        // SPI
        public static final int SPI_CLOCK_VALUE_2_MHZ = 0;
        public static final int SPI_CLOCK_VALUE_4_MHZ = 1;
        public static final int SPI_CLOCK_VALUE_8_MHZ = 2;

        public static final int SPI_MODE_0 = 0;
        public static final int SPI_MODE_1 = 1;
        public static final int SPI_MODE_2 = 2;
        public static final int SPI_MODE_3 = 3;

        // Baud rate
        public static final int BAUD_RATE_2400 = 2400;
        public static final int BAUD_RATE_4800 = 4800;
        public static final int BAUD_RATE_9600 = 9600;
        public static final int BAUD_RATE_19200 = 19200;
        public static final int BAUD_RATE_38400 = 38400;
        public static final int BAUD_RATE_57600 = 57600;
        public static final int BAUD_RATE_115200 = 115200;
        public static final int BAUD_RATE_230400 = 230400;

        // Output power level
        public static final int OUTPUT_POWER_LEVEL_MINUS_19_POINT_5_DBM = 1;
        public static final int OUTPUT_POWER_LEVEL_MINUS_13_POINT_5_DBM = 2;
        public static final int OUTPUT_POWER_LEVEL_MINUS_10_DBM = 3;
        public static final int OUTPUT_POWER_LEVEL_MINUS_7_DBM = 4;
        public static final int OUTPUT_POWER_LEVEL_MINUS_5_DBM = 5;
        public static final int OUTPUT_POWER_LEVEL_MINUS_3_POINT_5_DBM = 6;
        public static final int OUTPUT_POWER_LEVEL_MINUS_2_DBM = 7;
        public static final int OUTPUT_POWER_LEVEL_MINUS_1_DBM = 8;
        public static final int OUTPUT_POWER_LEVEL_0_DBM = 9;
        public static final int OUTPUT_POWER_LEVEL_1_DBM = 10;
        public static final int OUTPUT_POWER_LEVEL_1_POINT_5_DBM = 11;
        public static final int OUTPUT_POWER_LEVEL_2_POINT_5_DBM = 12;

        public static final String OUTPUT_POWER_LEVEL_NOT_SUPPORTED = "NOT SUPPORTED";

        // Event configuration
        public static final int DEACTIVATE_EVENT = 0;
        public static final int ACTIVATE_EVENT = 1;

        public static final int INITIALIZATION_EVENT = 1;
        public static final int CONNECTION_EVENT = 2;
        public static final int DISCONNECTION_EVENT = 3;
        public static final int WAKEUP_EVENT = 4;

        // Bonding entry persistence status
        public static final int BONDING_ENTRY_NON_PERSISTENT = 0;
        public static final int BONDING_ENTRY_PERSISTENT = 1;

        // Event handler configuration
        public static final int CONNECTION_EVENT_HANDLER = 1;
        public static final int DISCONNECTION_EVENT_HANDLER = 2;
        public static final int WAKEUP_EVENT_HANDLER = 3;

        // Heartbeat
        public static final int HEARTBEAT_DISABLED = 0;
        public static final int HEARTBEAT_ENABLED = 1;

        // Host sleep
        public static final int HOST_SLEEP_MODE_0 = 0;
        public static final int HOST_SLEEP_MODE_1 = 1;

        // Security mode
        public static final int SECURITY_MODE_0 = 0;
        public static final int SECURITY_MODE_1 = 1;
        public static final int SECURITY_MODE_2 = 2;
        public static final int SECURITY_MODE_3 = 3;

        // Map command to CodelessCommand class
        public static final HashMap<String, Class<? extends CodelessCommand>> commandMap = new HashMap<>();
        static {
            commandMap.put(BasicCommand.COMMAND, BasicCommand.class);
            commandMap.put(DeviceInformationCommand.COMMAND, DeviceInformationCommand.class);
            commandMap.put(UartEchoCommand.COMMAND, UartEchoCommand.class);
            commandMap.put(ResetIoConfigCommand.COMMAND, ResetIoConfigCommand.class);
            commandMap.put(ErrorReportingCommand.COMMAND, ErrorReportingCommand.class);
            commandMap.put(ResetCommand.COMMAND, ResetCommand.class);
            commandMap.put(BinRequestCommand.COMMAND, BinRequestCommand.class);
            commandMap.put(BinRequestAckCommand.COMMAND, BinRequestAckCommand.class);
            commandMap.put(BinExitCommand.COMMAND, BinExitCommand.class);
            commandMap.put(BinExitAckCommand.COMMAND, BinExitAckCommand.class);
            commandMap.put(BinResumeCommand.COMMAND, BinResumeCommand.class);
            commandMap.put(BinEscCommand.COMMAND, BinEscCommand.class);
            commandMap.put(TimerStartCommand.COMMAND, TimerStartCommand.class);
            commandMap.put(TimerStopCommand.COMMAND, TimerStopCommand.class);
            commandMap.put(CursorCommand.COMMAND, CursorCommand.class);
            commandMap.put(RandomNumberCommand.COMMAND, RandomNumberCommand.class);
            commandMap.put(BatteryLevelCommand.COMMAND, BatteryLevelCommand.class);
            commandMap.put(BluetoothAddressCommand.COMMAND, BluetoothAddressCommand.class);
            commandMap.put(RssiCommand.COMMAND, RssiCommand.class);
            commandMap.put(DeviceSleepCommand.COMMAND, DeviceSleepCommand.class);
            commandMap.put(IoConfigCommand.COMMAND, IoConfigCommand.class);
            commandMap.put(IoStatusCommand.COMMAND, IoStatusCommand.class);
            commandMap.put(AdcReadCommand.COMMAND, AdcReadCommand.class);
            commandMap.put(I2cScanCommand.COMMAND, I2cScanCommand.class);
            commandMap.put(I2cConfigCommand.COMMAND, I2cConfigCommand.class);
            commandMap.put(I2cReadCommand.COMMAND, I2cReadCommand.class);
            commandMap.put(I2cWriteCommand.COMMAND, I2cWriteCommand.class);
            commandMap.put(UartPrintCommand.COMMAND, UartPrintCommand.class);
            commandMap.put(MemStoreCommand.COMMAND, MemStoreCommand.class);
            commandMap.put(PinCodeCommand.COMMAND, PinCodeCommand.class);
            commandMap.put(CmdStoreCommand.COMMAND, CmdStoreCommand.class);
            commandMap.put(CmdPlayCommand.COMMAND, CmdPlayCommand.class);
            commandMap.put(CmdGetCommand.COMMAND, CmdGetCommand.class);
            commandMap.put(AdvertisingStopCommand.COMMAND, AdvertisingStopCommand.class);
            commandMap.put(AdvertisingStartCommand.COMMAND, AdvertisingStartCommand.class);
            commandMap.put(AdvertisingDataCommand.COMMAND, AdvertisingDataCommand.class);
            commandMap.put(AdvertisingResponseCommand.COMMAND, AdvertisingResponseCommand.class);
            commandMap.put(CentralRoleSetCommand.COMMAND, CentralRoleSetCommand.class);
            commandMap.put(PeripheralRoleSetCommand.COMMAND, PeripheralRoleSetCommand.class);
            commandMap.put(BroadcasterRoleSetCommand.COMMAND, BroadcasterRoleSetCommand.class);
            commandMap.put(GapStatusCommand.COMMAND, GapStatusCommand.class);
            commandMap.put(GapScanCommand.COMMAND, GapScanCommand.class);
            commandMap.put(GapConnectCommand.COMMAND, GapConnectCommand.class);
            commandMap.put(GapDisconnectCommand.COMMAND, GapDisconnectCommand.class);
            commandMap.put(ConnectionParametersCommand.COMMAND, ConnectionParametersCommand.class);
            commandMap.put(MaxMtuCommand.COMMAND, MaxMtuCommand.class);
            commandMap.put(DataLengthEnableCommand.COMMAND, DataLengthEnableCommand.class);
            commandMap.put(SpiConfigCommand.COMMAND, SpiConfigCommand.class);
            commandMap.put(SpiWriteCommand.COMMAND, SpiWriteCommand.class);
            commandMap.put(SpiReadCommand.COMMAND, SpiReadCommand.class);
            commandMap.put(SpiTransferCommand.COMMAND, SpiTransferCommand.class);
            commandMap.put(BaudRateCommand.COMMAND, BaudRateCommand.class);
            commandMap.put(PowerLevelConfigCommand.COMMAND, PowerLevelConfigCommand.class);
            commandMap.put(PulseGenerationCommand.COMMAND, PulseGenerationCommand.class);
            commandMap.put(EventConfigCommand.COMMAND, EventConfigCommand.class);
            commandMap.put(BondingEntryClearCommand.COMMAND, BondingEntryClearCommand.class);
            commandMap.put(BondingEntryStatusCommand.COMMAND, BondingEntryStatusCommand.class);
            commandMap.put(BondingEntryTransferCommand.COMMAND, BondingEntryTransferCommand.class);
            commandMap.put(EventHandlerCommand.COMMAND, EventHandlerCommand.class);
            commandMap.put(HeartbeatCommand.COMMAND, HeartbeatCommand.class);
            commandMap.put(HostSleepCommand.COMMAND, HostSleepCommand.class);
            commandMap.put(SecurityModeCommand.COMMAND, SecurityModeCommand.class);
            commandMap.put(FlowControlCommand.COMMAND, FlowControlCommand.class);
        }

        public static final HashSet<CommandID> modeCommands = new HashSet<>();
        static {
            modeCommands.add(CommandID.BINREQ);
            modeCommands.add(CommandID.BINREQACK);
            modeCommands.add(CommandID.BINREQEXIT);
            modeCommands.add(CommandID.BINREQEXITACK);
        }

        public static boolean isModeCommand(CodelessCommand command) {
            return modeCommands.contains(command.getCommandID());
        }
    }

    public static CodelessCommand createCommand(CodelessManager manager, Class<? extends CodelessCommand> commandClass, String command) {
        try {
            return commandClass.getConstructor(CodelessManager.class, String.class, boolean.class).newInstance(manager, command, true);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e(TAG, "Failed to create " + commandClass.getSimpleName() + " object: " + e.getCause());
            return new CustomCommand(manager, PREFIX + command, true);
        }
    }

    public enum CommandID {
        AT,
        ATI,
        ATE,
        ATZ,
        ATF,
        ATR,
        BINREQ,
        BINREQACK,
        BINREQEXIT,
        BINREQEXITACK,
        BINRESUME,
        BINESC,
        TMRSTART,
        TMRSTOP,
        CURSOR,
        RANDOM,
        BATT,
        BDADDR,
        RSSI,
        FLOWCONTROL,
        SLEEP,
        IOCFG,
        IO,
        ADC,
        I2CSCAN,
        I2CCFG,
        I2CREAD,
        I2CWRITE,
        PRINT,
        MEM,
        PIN,
        CMDSTORE,
        CMDPLAY,
        CMD,
        ADVSTOP,
        ADVSTART,
        ADVDATA,
        ADVRESP,
        CENTRAL,
        PERIPHERAL,
        BROADCASTER,
        GAPSTATUS,
        GAPSCAN,
        GAPCONNECT,
        GAPDISCONNECT,
        CONPAR,
        MAXMTU,
        DLEEN,
        HOSTSLP,
        SPICFG,
        SPIWR,
        SPIRD,
        SPITR,
        BAUD,
        PWRLVL,
        PWM,
        EVENT,
        CLRBNDE,
        CHGBNDP,
        IEBNDE,
        HNDL,
        SEC,
        HRTBT,

        CUSTOM
    }
}
