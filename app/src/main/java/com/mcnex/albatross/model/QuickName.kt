package com.mcnex.albatross.model

import android.os.Parcel
import android.os.Parcelable
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.google.gson.annotations.SerializedName

data class QuickName  (
    @SerializedName("golf_name")
    val golf_name: String

) : SearchSuggestion {

    constructor(parcel: Parcel) : this(parcel.readString().toString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(golf_name)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun getBody(): String {
        return  golf_name
    }

    companion object CREATOR : Parcelable.Creator<QuickName> {
        override fun createFromParcel(parcel: Parcel): QuickName {
            return QuickName(parcel)
        }

        override fun newArray(size: Int): Array<QuickName?> {
            return arrayOfNulls(size)
        }
    }

}