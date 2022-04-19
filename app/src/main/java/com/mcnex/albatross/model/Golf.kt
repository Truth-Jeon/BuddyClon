package com.mcnex.albatross.model

import android.os.Parcel
import android.os.Parcelable
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Golf  (
    val nation : Nation,
    val region  : Region,
    val golf_code: String,
    val golf_name  : GolfName
) : Serializable