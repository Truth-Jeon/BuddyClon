package com.mcnex.albatross.model
import java.io.Serializable

data class Nation(
    val nation_code: String,
    val nation_english: String,
    val nation_native: String
) : Serializable