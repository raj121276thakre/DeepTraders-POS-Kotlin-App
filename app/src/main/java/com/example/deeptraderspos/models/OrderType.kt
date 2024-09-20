package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class OrderType(
    val id: String? = null , // Unique ID for the category
    val orderTypeName: String =" ",   // Name of the category
    val orderTypeImage: String? = null  // Image URL or resource identifier (optional)
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(orderTypeName)
        parcel.writeString(orderTypeImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderType> {
        override fun createFromParcel(parcel: Parcel): OrderType {
            return OrderType(parcel)
        }

        override fun newArray(size: Int): Array<OrderType?> {
            return arrayOfNulls(size)
        }
    }
}
