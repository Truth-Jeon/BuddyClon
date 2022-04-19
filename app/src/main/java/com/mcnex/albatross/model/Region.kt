package com.mcnex.albatross.model
import java.io.Serializable

data class Region(
    val region_code: String,
    val region_english: String,
    val region_native: String
) : Serializable