package com.mcnex.albatross.app

import android.app.Application
import com.diasemi.codelesslib.CodelessManager
import com.mcnex.albatross.util.CodelessUtil
import org.greenrobot.eventbus.EventBus

class App : Application() {

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()

        EventBus.builder()
            .logNoSubscriberMessages(false)
            .sendNoSubscriberEvent(false)
            .installDefaultEventBus()

        setCrashHandler()
    }

    private fun setCrashHandler() {

        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
        }
        val fabricExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            AlbatossExceptionHandler(
                this,
                defaultExceptionHandler,
                fabricExceptionHandler
            )
        )
    }

    companion object {
        lateinit var INSTANCE: App
        val PREFERENCES_NAME = "scan-filter-preferences"

        val PRE_RSSI_FILTER  = "rssiFilter"
        val PRE_RSSI_LEVEL   = "rssiLevel"
        val PRE_CODELESS    = "codeless"
        val PRE_DSPS        = "dsps"
        val PRE_SUOTA       = "suota"
        val PRE_OTHER       = "other"
        val PRE_UNKNOWN     = "unknown"
        val PRE_BEACON      = "iBeacon"
        val PRE_MICROSOFT   = "microsoft"

        val STR_NAME   = "name"
        val STR_ADDRESS   = "address"
        val STR_ADVDATA   = "advData"

        var manager: CodelessManager? = null

        var DEVICE_NAME : String = ""

        val codlesstutil =  CodelessUtil()

//        var is_english = false

        var APP_LANGUAGE = ""

        val server_base_url = "https://www.eyeclonview.com/albatross/"

        val server_rserarch_url = "api/get.php?what=rsearch&golf_name="
        val server_fserarch_url = "api/get.php?what=fsearch&golf_name="
        val server_golfinfo_url = "api/get.php?what=golf_info&nation=%s&region=%s&golf=%s"
        val server_golf_bin_url = "map/%03d%03d%03d/golf.bin"
        val server_point_bin_url = "map/%03d%03d%03d/point.bin"
        val server_notsupport_url = "api/get.php?what=not_support&fw_code="

        val ENV_TIME_OUT = 10000
        val UPDATE_TIME_OUT = 60000

        val MAP_MAX_SIZE = 131072 // 128k byte

        var IS_NET_STATE_CONNET = false

    }
}