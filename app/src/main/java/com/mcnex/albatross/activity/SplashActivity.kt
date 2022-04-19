package com.mcnex.albatross.activity

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mcnex.albatross.R
import com.mcnex.albatross.databinding.ActivitySplashBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class SplashActivity : AppCompatActivity() {

    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!



    companion object {
        private val TAG = SplashActivity::class.java.simpleName
        var noti_ver = 0
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        binding.lottie.playAnimation()

        binding.lottie.addAnimatorListener(object : AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                val intent = Intent(applicationContext, MainActivity::class.java)

                intent.putExtra("noti_ver", noti_ver)

                startActivity(intent)

                finish()
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }

        })


        val thread = RequestThread()
        thread.start()
    }

    override fun onResume() {
        super.onResume()
//        startLoading()
    }

    private fun startLoading() {
        val mHandler = Handler()
        mHandler.postDelayed({
            openMainPage()
            finish()
        }, 1500)
    }

    private fun openMainPage() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
    }



    internal class RequestThread : Thread() {
        override fun run() {
            try {
                val urlStr = "https://eyeclonview.com/albatross/notice_ver.txt"
                val outputBuilder = StringBuilder()
                val url = URL(urlStr)
                val urconn = url.openConnection() as HttpURLConnection
                urconn.doInput = true
                urconn.doOutput = true
                urconn.connectTimeout = 2000 // 2초
                val resCode = urconn.responseCode
                if (resCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(urconn.inputStream, "UTF-8"))
                    var line: String? = null
                        line = reader.readLine()
                        if (line != null) {
                            Log.d(TAG, "noti_ver: $line")

                            noti_ver = line.toInt()

                        }
                    reader.close()
                    urconn.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}