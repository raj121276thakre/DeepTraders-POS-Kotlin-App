package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class Units(
    val id: String? = null , // Unique ID for the category
    val unitName: String =" ",   // Name of the category
    val unitImage: String? = null  // Image URL or resource identifier (optional)
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(unitName)
        parcel.writeString(unitImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Units> {
        override fun createFromParcel(parcel: Parcel): Units {
            return Units(parcel)
        }

        override fun newArray(size: Int): Array<Units?> {
            return arrayOfNulls(size)
        }
    }
}
