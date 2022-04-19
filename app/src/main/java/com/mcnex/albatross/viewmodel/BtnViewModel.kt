package com.mcnex.albatross.viewmodel

import android.bluetooth.BluetoothDevice
import android.media.MediaPlayer
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mcnex.albatross.util.CodelessUtil

class BtnViewModel : ViewModel() {

    private val mutableSelectedBtn = MutableLiveData<Int>()

//    val helloText: ObservableField<String> = ObservableField()
//
//    fun onCreate() {
//        helloText.set("hello!")
//    }
//
//    fun onResume() {}
//
//    fun onPause() {}
//
//    fun onDestroy() {}
//
//    fun showCurrentTime() {
//        helloText.set(System.currentTimeMillis().toString())
//    }
//
    var currentTime2ClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(view: View?) {
            mutableSelectedBtn.value = view!!.id
        }
    }





}