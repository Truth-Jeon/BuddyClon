package com.mcnex.albatross.util

open class AlbatrossUtil {
    companion object {
        const val MENU_FRAGMENT_MAIN = 10 //scanning

        const val MENU_FRAGMENT_NOTI = 11
        const val MENU_FRAGMENT_DEVICE = 12
        const val MENU_FRAGMENT_DEVICE_SET = 13
        const val MENU_FRAGMENT_SEARCH = 20
        const val MENU_FRAGMENT_MAP_INFO = 21
        const val MENU_FRAGMENT_APP_SET = 30
        const val MENU_FRAGMENT_FAQ = 40
        const val MENU_FRAGMENT_VNV = 51

        const val MENU_DISCONNECT = 100

        //BLE MCNEX packet data max size is 124 Bytes
        const val MCNEX_PACKET_MAX = 32
        const val DATA_PACKET_MAX = 30

        //BLE MODE
        const val COMMAND_STAGE = 0x00.toChar()
        const val DATA_STAGE = 0x01.toChar()
        const val DATA_RECEIVE_STAGE = 0x02.toChar()

        //command packet ACK
        const val FIRST: Byte = 0x01
        const val ACK: Byte = 0x02
        const val NACK: Byte = 0x03
        const val READY: Byte = 0x04
        const val D_STAGE: Byte = 0x2D

        //Data Size
        const val ENV_DATA_SIZE: Byte = 0x17

        //option #1
        const val MAP_DOWN: Byte = 0x02
        const val ENV: Byte = 0x03
        const val VnV: Byte = 0x04

        //UPDATA_option2
        const val UPDATE_GID_CHK: Byte = 0x2A
        const val UPDATE_DOWN_START: Byte = 0x3A
        const val UPDATE_RESULT_REQ: Byte = 0x3B


        //VnV option2
        const val VnV_LED: Byte = 0x01
        const val VnV_GPS: Byte = 0x02
        const val VnV_AUDIO: Byte = 0x03
        const val VnV_BATTERY: Byte = 0x04

        //ENV option
        const val ENV_DATA_REQ: Byte = 0x01
        const val ENV_DATA_SET: Byte = 0x02
        const val ENV_DATA_RESET: Byte = 0x03

        //command packet direction
        const val MtoD: Byte = 0x01
        const val DtoM: Byte = 0x02

        //command return value
        const val DATA_MODE_OK = 0
        const val DATA_MODE_FAIL = 1
        const val COM_CONTINUE = 3

        //env data option
        const val LANGUAGE_KR: Byte = 0x00
        const val LANGUAGE_JP: Byte = 0x01
        const val LANGUAGE_EN: Byte = 0x02

        const val DISTANCE_PRIME_ON: Byte = 0x01
        const val DISTANCE_PRIME_OFF: Byte = 0x00

        const val DISTANCE_METER: Byte = 0x00
        const val DISTANCE_YARD: Byte = 0x01

        const val HOLE_ONE_LEFT: Byte = 0x04
        const val HOLE_ONE_RIGHT: Byte = 0x02
        const val HOLE_TWO: Byte = 0x06

        const val VOLUME_INDEX = 0
        const val LANGUAGE_INDEX = 1
        const val DISTANCE_INDEX = 2
        const val METER_INDEX  = 3
        const val HOLE_INDEX  = 4
        const val BATTERY_INDEX  = 18

        const val CHUNK_SIZE  = 512

    }
}