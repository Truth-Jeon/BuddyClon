package com.mcnex.albatross.util

import android.util.Log
import com.diasemi.codelesslib.CodelessEvent.ScanStart
import com.mcnex.albatross.app.App.Companion.ENV_TIME_OUT
import com.mcnex.albatross.app.App.Companion.UPDATE_TIME_OUT
import com.mcnex.albatross.app.App.Companion.manager
import com.mcnex.albatross.event.AlbatossEvent
import com.mcnex.albatross.event.TimeOutEvent
import com.mcnex.albatross.util.AlbatrossUtil.Companion.CHUNK_SIZE
import com.mcnex.albatross.util.AlbatrossUtil.Companion.ENV
import com.mcnex.albatross.util.AlbatrossUtil.Companion.ENV_DATA_REQ
import com.mcnex.albatross.util.AlbatrossUtil.Companion.ENV_DATA_SET
import com.mcnex.albatross.util.AlbatrossUtil.Companion.ENV_DATA_SIZE
import com.mcnex.albatross.util.AlbatrossUtil.Companion.FIRST
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MAP_DOWN
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MtoD
import com.mcnex.albatross.util.AlbatrossUtil.Companion.UPDATE_DOWN_START
import com.mcnex.albatross.util.AlbatrossUtil.Companion.UPDATE_GID_CHK
import com.mcnex.albatross.util.AlbatrossUtil.Companion.UPDATE_RESULT_REQ
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.experimental.xor

open class CodelessUtil {

    private val TAG = CodelessUtil::class.java.simpleName
    open fun forEnv(op2: Byte, envData: ByteArray) {
        Log.d(TAG, "forEnvThread")
        val sendBuf = ByteArray(32)
//        val mDeviceFragment = DeviceFragment()
        val envThread: Thread = object : Thread() {
            override fun run() {
                try {
//                    isWorking = true
//                    com.mcnex.albatross.activity.MainActivity.BLEMainThread.envRun = true
                    var receiveBuf = ByteArray(32)
                    Arrays.fill(sendBuf, 0x00.toByte())
                    Arrays.fill(receiveBuf, 0x00.toByte())
                    sendBuf[0] = FIRST
                    sendBuf[5] = ENV
                    sendBuf[6] = op2 //set, request, else
//                    Log.d(ThreadTAG, "Jade make packet!!!!!")
                    if (op2 == ENV_DATA_SET) {
                        sendBuf[7] = ENV_DATA_SIZE
                        //System.arraycopy(intToByte(mapIndex),0,sendBuf,8,11);
                        System.arraycopy(envData, 0, sendBuf, 8, ENV_DATA_SIZE.toInt())
                    } else if (op2 == ENV_DATA_REQ) {
                        sendBuf[7] = 0x00.toByte()
                    } else {
                        sendBuf[7] = envData.size.toByte()
                        System.arraycopy(envData, 0, sendBuf, 8, envData.size)
                    }
                    sendBuf[31] = MtoD

                    TimeOutEvent().let {
                        it.timeoutEvent(ENV_TIME_OUT)
                        EventBus.getDefault().post(it)
                    }
                    manager!!.sendDspsData(sendBuf)
                } catch (e: Exception) {
                    Log.e(TAG, "Jade Exception !!!$e")
                }
            }
        }
        envThread.start()
    }

    open fun updateV2Thread(golfBinary: ByteArray?, option: Byte, chunkSize: Int, upDataSize : Int?) {
        Log.d(TAG, "updateV2Thread")
        val uptV2Thread: Thread = object : Thread() {
            override fun run() {
                try {
                    val sendPacket = ByteArray(32)
                    var getPacket = ByteArray(32)
                    var getCRC = ByteArray(32)
                    var chunkCount = 0
                    var uptReVal = false
                    val bulkBuf = ByteArray(chunkSize)

                    var time_out = ENV_TIME_OUT

                    Arrays.fill(sendPacket, 0x00.toByte())
                    Arrays.fill(getPacket, 0x00.toByte())

                    sendPacket[0] = FIRST
                    sendPacket[5] = MAP_DOWN
                    System.arraycopy(golfBinary, 0, sendPacket, 8, 16)

                    when (option) {
                        UPDATE_GID_CHK -> {
                            sendPacket[6] = UPDATE_GID_CHK
                            sendPacket[7] = 0x10.toByte()
                        }
                        UPDATE_DOWN_START -> {
                            if (upDataSize == null) {
                                Log.e(TAG, "Update Data is Null!!!!")
                            }else {
                                System.arraycopy(intToByte(upDataSize), 0, sendPacket, 1, 4)
                                sendPacket[6] = UPDATE_DOWN_START
                                sendPacket[7] = 0x17.toByte()

                                chunkCount = upDataSize / chunkSize

                                if(upDataSize % chunkSize != 0 ){
                                    chunkCount ++
                                }

                                Log.d(TAG, " [Alba] 11 Chunk Count : $chunkCount")
                                System.arraycopy(intToByte(chunkCount), 0, sendPacket, 24, 4)
                                System.arraycopy(intToByte(chunkSize), 0, sendPacket, 28, 4)
                            }
                        }
                        UPDATE_RESULT_REQ -> {
                            sendPacket[6] = UPDATE_RESULT_REQ
                            sendPacket[7] = 0x11.toByte()
                            time_out = UPDATE_TIME_OUT
                        }
                        else -> Log.e(TAG, "Option value Out of Range!!!")
                    }
                    sendPacket[31] = MtoD

                    TimeOutEvent().let {
                        it.timeoutEvent(time_out)
                        EventBus.getDefault().post(it)
                    }

                    manager!!.sendDspsData( sendPacket )
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "updateV2Thread Error : $e")
                }
            }
        }
        uptV2Thread.start()
    }

    open fun sendChunk( upData : ByteArray) {
        Log.d(TAG, "chunkThread")
        val chunkThread: Thread = object : Thread() {
            override fun run() {
                try {
                    if(upData.size < CHUNK_SIZE){
                        val bulkBuf = ByteArray(CHUNK_SIZE)
                        System.arraycopy(upData, 0, bulkBuf, 0, upData.size)
                        Log.d(TAG, "send map data last add")

                        TimeOutEvent().let {
                            it.timeoutEvent(ENV_TIME_OUT)
                            EventBus.getDefault().post(it)
                        }
                        manager!!.sendDspsData(bulkBuf)
                    }else {
                        Log.d(TAG, "send map data")
                        TimeOutEvent().let {
                            it.timeoutEvent(ENV_TIME_OUT)
                            EventBus.getDefault().post(it)
                        }
                        manager!!.sendDspsData(upData)
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "chunkThread Error : $e")

                }
            }
        }
        chunkThread.start()
    }


    open fun intToByte(value: Int): ByteArray? {
        val reValue: ByteArray = ByteArray(4)
        reValue[3] = (value shr 24).toByte()
        reValue[2] = (value shr 16).toByte()
        reValue[1] = (value shr 8).toByte()
        reValue[0] = value.toByte()
        return reValue
    }


    open fun xcrc32(data: ByteArray, size: Int, init: Byte): Byte {
        var crc = init
        for (i in 0 until size) {
            crc = crc xor data[i]
        }
        return crc
    }
}