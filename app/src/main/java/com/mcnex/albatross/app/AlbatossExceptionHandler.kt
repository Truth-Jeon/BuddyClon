package com.mcnex.albatross.app


import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Process
import java.io.PrintWriter
import java.io.StringWriter

class AlbatossExceptionHandler(
    application: Application,
    private val defaultExceptionHandler: Thread.UncaughtExceptionHandler,
    private val fabricExceptionHandler: Thread.UncaughtExceptionHandler
) : Thread.UncaughtExceptionHandler {

    private var lastActivity: Activity? = null
    private var activityCount = 0

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    lastActivity = activity
                }


                override fun onActivityStarted(activity: Activity) {
                    activityCount++
                    lastActivity = activity
                }


                override fun onActivityStopped(activity: Activity) {
                    activityCount--
                    if (activityCount < 0) {
                        lastActivity = null
                    }

                }

                override fun onActivityResumed(p0: Activity) {
                }

                override fun onActivityPaused(p0: Activity) {
                }

                override fun onActivityDestroyed(p0: Activity) {
                }

                override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
                }
            })
    }

    override fun uncaughtException(thread: Thread?, throwable: Throwable) {
        fabricExceptionHandler.uncaughtException(thread, throwable)
        lastActivity?.run {
            val stringWriter = StringWriter()
            throwable.printStackTrace(PrintWriter(stringWriter))

            startErrorActivity(this, stringWriter.toString())
        } ?: defaultExceptionHandler.uncaughtException(thread, throwable)

        Process.killProcess(Process.myPid())
        System.exit(-1)
    }

    private fun startErrorActivity(activity: Activity, errorText: String) = activity.run {
        startActivity(intent)
        finish()
    }

}