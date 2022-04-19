package com.mcnex.albatross.viewmodel

import android.bluetooth.BluetoothDevice
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diasemi.codelesslib.CodelessManager
import com.mcnex.albatross.app.App
import com.mcnex.albatross.util.CodelessUtil

class DialogViewModel : ViewModel() {

    companion object {
        val DIALOG_OK   = 100
        val DIALOG_CANCEL   = 101
        val DIALOG_NOTI   = 110
        val DIALOG_DEVICE_SCAN   = 120
        val DIALOG_DISCONNECT   = -1
        val DIALOG_DISCONNECTED   = -2
        val DIALOG_NET_ERROR  = -3
    }

    private val mutableDialogEvent = MutableLiveData<Int>()

    val dialogEvent: LiveData<Int> get() = mutableDialogEvent

    fun onDialogEvent(event : Int){
        mutableDialogEvent.value = event
    }


    fun consumeSignInResponse() {
        mutableDialogEvent.value = null
    }



}