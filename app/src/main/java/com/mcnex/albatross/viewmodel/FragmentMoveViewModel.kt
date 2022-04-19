package com.mcnex.albatross.viewmodel

import android.bluetooth.BluetoothDevice
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mcnex.albatross.util.CodelessUtil

class FragmentMoveViewModel : ViewModel() {

    private val mutableMoveEvent = MutableLiveData<Int>()

    val moveEvent: LiveData<Int> get() = mutableMoveEvent

    fun onMoveEvent(event : Int){
        mutableMoveEvent.value = event
    }


    fun consumeSignInResponse() {
        mutableMoveEvent.value = null
    }




}