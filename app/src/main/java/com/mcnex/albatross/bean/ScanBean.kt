package com.mcnex.albatross.bean

import android.util.Log

open class ScanBean {

    var icon = 0
    var name: String? = null
    var address: String? = null
    var description: String? = null
    var signal = 0

    constructor( icon: Int, name: String?, address: String?, description: String?, signal: Int ) {
        this.icon = icon
        this.name = name
        this.address = address
        this.description = description
        this.signal = signal
    }

    constructor (name: String?, address: String?) {
        icon = 0
        this.name = name
        this.address = address
        description = null
        signal = 0
    }

}