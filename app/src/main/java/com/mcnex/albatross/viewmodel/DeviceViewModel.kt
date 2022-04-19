package com.mcnex.albatross.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.ClipData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diasemi.codelesslib.CodelessManager

class DeviceViewModel : ViewModel() {


    private val mutableSelectedItem = MutableLiveData<BluetoothDevice>()
    private val mutableEvent = MutableLiveData<Boolean>()

    val selectedItem: LiveData<BluetoothDevice> get() = mutableSelectedItem

    val event: LiveData<Boolean> get() = mutableEvent


    fun onReceivedDevice(device : BluetoothDevice){
        mutableSelectedItem.value = device
    }

    fun disable(show : Boolean) {
        mutableEvent.value = show
    }

}