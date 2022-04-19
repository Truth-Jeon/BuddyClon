package com.mcnex.albatross.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mcnex.albatross.model.Golf

class GolfViewModel : ViewModel() {


    private val mutableSelectedItem = MutableLiveData<Golf>()
    private val mutableEvent = MutableLiveData<Boolean>()

    val selectedItem: LiveData<Golf> get() = mutableSelectedItem

    val event: LiveData<Boolean> get() = mutableEvent

    fun onReceivedGolf(golf : Golf){
        mutableSelectedItem.value = golf
    }

    fun disable(show : Boolean) {
        mutableEvent.value = show
    }

}