package com.mcnex.albatross.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.ClipData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diasemi.codelesslib.CodelessManager

class MapViewModel : ViewModel() {

    private val mutableSelectedItem = MutableLiveData<Int>()
    private val mutableEvent = MutableLiveData<Boolean>()

    val progress: LiveData<Int> get() = mutableSelectedItem

    val show: LiveData<Boolean> get() = mutableEvent


    fun onProgress(event : Int){
        mutableSelectedItem.value = event
    }

    fun disable(show_ : Boolean) {
        mutableEvent.value = show_
    }

}