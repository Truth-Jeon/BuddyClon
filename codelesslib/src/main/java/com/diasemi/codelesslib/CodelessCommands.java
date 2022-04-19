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

import com.diasemi.codelesslib.CodelessProfile.BondingEntry;
import com.diasemi.codelesslib.CodelessProfile.GPIO;
import com.diasemi.codelesslib.command.AdcReadCommand;
import com.diasemi.codelesslib.command.AdvertisingDataCommand;
import com.diasemi.codelesslib.command.AdvertisingResponseCommand;
import com.diasemi.codelesslib.command.BasicCommand;
import com.diasemi.codelesslib.command.BatteryLevelCommand;
import com.diasemi.codelesslib.command.BaudRateCommand;
import com.diasemi.codelesslib.command.BinExitAckCommand;
import com.diasemi.codelesslib.command.BinExitCommand;
import com.diasemi.codelesslib.command.BinRequestAckCommand;
import com.diasemi.codelesslib.command.BinRequestCommand;
import com.diasemi.codelesslib.command.BluetoothAddressCommand;
import com.diasemi.codelesslib.command.BondingEntryClearCommand;
import com.diasemi.codelesslib.command.BondingEntryStatusCommand;
import com.diasemi.codelesslib.command.BondingEntryTransferCommand;
import com.diasemi.codelesslib.command.CmdGetCommand;
import com.diasemi.codelesslib.command.CmdPlayCommand;
import com.diasemi.codelesslib.command.CmdStoreCommand;
import com.diasemi.codelesslib.command.CodelessCommand;
import com.diasemi.codelesslib.command.ConnectionParametersCommand;
import com.diasemi.codelesslib.command.CursorCommand;
import com.diasemi.codelesslib.command.DataLengthEnableCommand;
import com.diasemi.codelesslib.command.DeviceInformationCommand;
import com.diasemi.codelesslib.command.DeviceSleepCommand;
import com.diasemi.codelesslib.command.ErrorReportingCommand;
import com.diasemi.codelesslib.command.EventConfigCommand;
import com.diasemi.codelesslib.command.EventHandlerCommand;
import com.diasemi.codelesslib.command.FlowControlCommand;
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

@SuppressWarnings("UnusedReturnValue")
public class CodelessCommands {

    private CodelessManager manager;

    public CodelessCommands(CodelessManager manager) {
        this.manager = manager;
    }

    public CodelessManager getManager() {
        return manager;
    }

    private <T extends CodelessCommand> T sendCommand(T command) {
        command.setOrigin(this);
        manager.sendCommand(command);
        return command;
    }

    public BasicCommand ping() {
        return sendCommand(new BasicCommand(manager));
    }

    public DeviceInformationCommand getDeviceInfo() {
        return sendCommand(new DeviceInformationCommand(manager));
    }

    public ResetCommand resetDevice() {
        return sendCommand(new ResetCommand(manager));
    }

    public BluetoothAddressCommand getBluetoothAddress() {
        return sendCommand(new BluetoothAddressCommand(manager));
    }

    public RssiCommand getPeerRssi() {
        return sendCommand(new RssiCommand(manager));
    }

    public BatteryLevelCommand getBatteryLevel() {
        return sendCommand(new BatteryLevelCommand(manager));
    }

    public RandomNumberCommand getRandomNumber() {
        return sendCommand(new RandomNumberCommand(manager));
    }

    public BinRequestCommand requestBinaryMode() {
        return sendCommand(new BinRequestCommand(manager));
    }

    public BinRequestAckCommand sendBinaryRequestAck() {
        return sendCommand(new BinRequestAckCommand(manager));
    }

    public BinExitCommand sendBinaryExit() {
        return sendCommand(new BinExitCommand(manager));
    }

    public BinExitAckCommand sendBinaryExitAck() {
        return sendCommand(new BinExitAckCommand(manager));
    }

    public ConnectionParametersCommand getConnectionParameters() {
        return sendCommand(new ConnectionParametersCommand(manager));
    }

    public ConnectionParametersCommand setConnectionParameters(int connectionInterval, int slaveLatency, int supervisionTimeout, int action) {
        return sendCommand(new ConnectionParametersCommand(manager, connectionInterval, slaveLatency, supervisionTimeout, action));
    }

    public MaxMtuCommand getMaxMtu() {
        return sendCommand(new MaxMtuCommand(manager));
    }

    public MaxMtuCommand setMaxMtu(int mtu) {
        return sendCommand(new MaxMtuCommand(manager, mtu));
    }

    public DataLengthEnableCommand getDataLength() {
        return sendCommand(new DataLengthEnableCommand(manager));
    }

    public DataLengthEnableCommand setDataLength(boolean enabled, int txPacketLength, int rxPacketLength) {
        return sendCommand(new DataLengthEnableCommand(manager, enabled, txPacketLength, rxPacketLength));
    }

    public DataLengthEnableCommand setDataLengthEnabled(boolean enabled) {
        return sendCommand(new DataLengthEnableCommand(manager, enabled));
    }

    public DataLengthEnableCommand enableDataLength() {
        return setDataLengthEnabled(true);
    }

    public DataLengthEnableCommand disableDataLength() {
        return setDataLengthEnabled(false);
    }

    public AdvertisingDataCommand getAdvertisingData() {
        return sendCommand(new AdvertisingDataCommand(manager));
    }

    public AdvertisingDataCommand setAdvertisingData(byte[] data) {
        return sendCommand(new AdvertisingDataCommand(manager, data));
    }

    public AdvertisingResponseCommand getScanResponseData() {
        return sendCommand(new AdvertisingResponseCommand(manager));
    }

    public AdvertisingResponseCommand setScanResponseData(byte[] data) {
        return sendCommand(new AdvertisingResponseCommand(manager, data));
    }

    public IoConfigCommand readIoConfig() {
        return sendCommand(new IoConfigCommand(manager));
    }

    public ResetIoConfigCommand resetIoConfig() {
        return sendCommand(new ResetIoConfigCommand(manager));
    }

    public IoConfigCommand setIoConfig(GPIO gpio) {
        return sendCommand(new IoConfigCommand(manager, gpio));
    }

    public IoStatusCommand readInput(GPIO gpio) {
        return sendCommand(new IoStatusCommand(manager, gpio));
    }

    public IoStatusCommand setOutput(GPIO gpio, boolean status) {
        return sendCommand(new IoStatusCommand(manager, gpio, status));
    }

    public IoStatusCommand setOutputLow(GPIO gpio) {
        return setOutput(gpio, false);
    }

    public IoStatusCommand setOutputHigh(GPIO gpio) {
        return setOutput(gpio, true);
    }

    public AdcReadCommand readAnalogInput(GPIO gpio) {
        return sendCommand(new AdcReadCommand(manager, gpio));
    }

    public PulseGenerationCommand getPwm() {
        return sendCommand(new PulseGenerationCommand(manager));
    }

    public PulseGenerationCommand setPwm(int frequency, int dutyCycle, int duration) {
        return sendCommand(new PulseGenerationCommand(manager, frequency, dutyCycle, duration));
    }

    public I2cConfigCommand setI2cConfig(int addressSize, int bitrate, int registerSize) {
        return sendCommand(new I2cConfigCommand(manager, addressSize, bitrate, registerSize));
    }

    public I2cScanCommand i2cScan() {
        return sendCommand(new I2cScanCommand(manager));
    }

    public I2cReadCommand i2cRead(int address, int register) {
        return sendCommand(new I2cReadCommand(manager, address, register));
    }

    public I2cReadCommand i2cRead(int address, int register, int count) {
        return sendCommand(new I2cReadCommand(manager, address, register, count));
    }

    public I2cWriteCommand i2cWrite(int address, int register, int value) {
        return sendCommand(new I2cWriteCommand(manager, address, register, value));
    }

    public SpiConfigCommand readSpiConfig() {
        return sendCommand(new SpiConfigCommand(manager));
    }

    public SpiConfigCommand setSpiConfig(int speed, int mode, int size) {
        return sendCommand(new SpiConfigCommand(manager, speed, mode, size));
    }

    public SpiWriteCommand spiWrite(String hexString) {
        return sendCommand(new SpiWriteCommand(manager, hexString));
    }

    public SpiReadCommand spiRead(int count) {
        return sendCommand(new SpiReadCommand(manager, count));
    }

    public SpiTransferCommand spiTransfer(String hexString) {
        return sendCommand(new SpiTransferCommand(manager, hexString));
    }

    public UartPrintCommand print(String text) {
        return sendCommand(new UartPrintCommand(manager, text));
    }

    public MemStoreCommand setMemContent(int index, String content) {
        return sendCommand(new MemStoreCommand(manager, index, content));
    }

    public MemStoreCommand getMemContent(int index) {
        return sendCommand(new MemStoreCommand(manager, index));
    }

    public RandomNumberCommand getRandom() {
        return sendCommand(new RandomNumberCommand(manager));
    }

    public CmdGetCommand getStoredCommands(int index) {
        return sendCommand(new CmdGetCommand(manager, index));
    }

    public CmdStoreCommand storeCommands(int index, String commandString) {
        return sendCommand(new CmdStoreCommand(manager, index, commandString));
    }

    public CmdPlayCommand playCommands(int index) {
        return sendCommand(new CmdPlayCommand(manager, index));
    }

    public TimerStartCommand startTimer(int timerIndex, int commandIndex, int delay) {
        return sendCommand(new TimerStartCommand(manager, timerIndex, commandIndex, delay));
    }

    public TimerStopCommand stopTimer(int timerIndex) {
        return sendCommand(new TimerStopCommand(manager, timerIndex));
    }

    public EventConfigCommand setEventConfig(int eventType, boolean status) {
        return sendCommand(new EventConfigCommand(manager, eventType, status));
    }

    public EventConfigCommand getEventConfigTable() {
        return sendCommand(new EventConfigCommand(manager));
    }

    public EventHandlerCommand setEventHandler(int eventType, String commandString) {
        return sendCommand(new EventHandlerCommand(manager, eventType, commandString));
    }

    public EventHandlerCommand getEventHandlers() {
        return sendCommand(new EventHandlerCommand(manager));
    }

    public BaudRateCommand getBaudRate() {
        return sendCommand(new BaudRateCommand(manager));
    }

    public BaudRateCommand setBaudRate(int baudRate) {
        return sendCommand(new BaudRateCommand(manager, baudRate));
    }

    public UartEchoCommand getUartEcho() {
        return sendCommand(new UartEchoCommand(manager));
    }

    public UartEchoCommand setUartEcho(boolean echo) {
        return sendCommand(new UartEchoCommand(manager, echo));
    }

    public HeartbeatCommand getHeartbeatStatus() {
        return sendCommand(new HeartbeatCommand(manager));
    }

    public HeartbeatCommand setHeartbeatStatus(boolean enable) {
        return sendCommand(new HeartbeatCommand(manager, enable));
    }

    public ErrorReportingCommand setErrorReporting(boolean enable) {
        return sendCommand(new ErrorReportingCommand(manager, enable));
    }

    public CursorCommand timeCursor() {
        return sendCommand(new CursorCommand(manager));
    }

    public DeviceSleepCommand sleep() {
        return sendCommand(new DeviceSleepCommand(manager, true));
    }

    public DeviceSleepCommand awake() {
        return sendCommand(new DeviceSleepCommand(manager, false));
    }

    public HostSleepCommand getHostSleepStatus() {
        return sendCommand(new HostSleepCommand(manager));
    }

    public HostSleepCommand setHostSleepStatus(int hostSleepMode, int wakeupByte, int wakeupRetryInterval, int wakeupRetryTimes) {
        return sendCommand(new HostSleepCommand(manager, hostSleepMode, wakeupByte, wakeupRetryInterval, wakeupRetryTimes));
    }

    public PowerLevelConfigCommand getPowerLevel() {
        return sendCommand(new PowerLevelConfigCommand(manager));
    }

    public PowerLevelConfigCommand setPowerLevel(int powerLevel) {
        return sendCommand(new PowerLevelConfigCommand(manager, powerLevel));
    }

    public SecurityModeCommand getSecurityMode() {
        return sendCommand(new SecurityModeCommand(manager));
    }

    public SecurityModeCommand setSecurityMode(int mode) {
        return sendCommand(new SecurityModeCommand(manager, mode));
    }

    public PinCodeCommand getPinCode() {
        return sendCommand(new PinCodeCommand(manager));
    }

    public PinCodeCommand setPinCode(int code) {
        return sendCommand(new PinCodeCommand(manager, code));
    }

    public FlowControlCommand getFlowControl() {
        return sendCommand(new FlowControlCommand(manager));
    }

    public FlowControlCommand setFlowControl(boolean enabled, GPIO rts, GPIO cts) {
        return sendCommand(new FlowControlCommand(manager, enabled, rts, cts));
    }

    public BondingEntryClearCommand clearBondingDatabaseEntry(int index) {
        return sendCommand(new BondingEntryClearCommand(manager, index));
    }

    public BondingEntryClearCommand clearBondingDatabase() {
        return clearBondingDatabaseEntry(CodelessLibConfig.BONDING_DATABASE_ALL_VALUES);
    }

    public BondingEntryStatusCommand getBondingDatabasePersistenceStatus() {
        return sendCommand(new BondingEntryStatusCommand(manager));
    }

    public BondingEntryStatusCommand setBondingEntryPersistenceStatus(int index, boolean persistent) {
        return sendCommand(new BondingEntryStatusCommand(manager, index, persistent));
    }

    public BondingEntryTransferCommand getBondingDatabase(int index) {
        return sendCommand(new BondingEntryTransferCommand(manager, index));
    }

    public BondingEntryTransferCommand setBondingDatabase(int index, BondingEntry entry) {
        return sendCommand(new BondingEntryTransferCommand(manager, index, entry));
    }
}
