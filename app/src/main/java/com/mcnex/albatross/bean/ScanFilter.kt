package com.mcnex.albatross.bean

import java.util.regex.Pattern

open class ScanFilter{
    var name: Pattern? = null
    var address: Pattern? = null
    var advData: Pattern? = null
    var rssi = Int.MIN_VALUE
    var codeless = false
    var dsps = true
    var suota = true
    var other = false
    var unknown = false
    var beacon = false
    var microsoft = false
}