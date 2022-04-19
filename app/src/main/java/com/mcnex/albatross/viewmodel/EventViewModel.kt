package com.mcnex.albatross.viewmodel

import android.bluetooth.BluetoothDevice
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mcnex.albatross.util.CodelessUtil

class EventViewModel : ViewModel() {

    companion object {
        val NET_ERROR  = -1
        val BATTERY_ERROR   = -2

    }

    private val mutableEvent = MutableLiveData<Int>()

    val onEvent: LiveData<Int> get() = mutableEvent


    fun onEvent(event : Int){
        mutableEvent.value = event
    }

    fun consumeSignInResponse() {
        mutableEvent.value = null
    }




}