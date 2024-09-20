package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class ShopInformation(
    val shopName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val address: String = "",
    val currencySymbol: String = "",
    val taxPercentage: Double = 0.0,
    val id: String? = null // Optional ID field
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(shopName)
        parcel.writeString(contactNumber)
        parcel.writeString(email)
        parcel.writeString(address)
        parcel.writeString(currencySymbol)
        parcel.writeDouble(taxPercentage)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShopInformation> {
        override fun createFromParcel(parcel: Parcel): ShopInformation {
            return ShopInformation(parcel)
        }

        override fun newArray(size: Int): Array<ShopInformation?> {
            return arrayOfNulls(size)
        }
    }
}
