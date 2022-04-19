package com.mcnex.albatross.event

import com.diasemi.codelesslib.CodelessManager

open class TimeOutEvent : AlbatossEvent(){

    var timeout = 1000

     fun timeoutEvent(time : Int){
        timeout = time
    }

}