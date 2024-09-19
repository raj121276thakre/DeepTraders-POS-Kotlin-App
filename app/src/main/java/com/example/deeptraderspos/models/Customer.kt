package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class Customer(
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String? = null,
    val customerAddress: String? = null,
    val id: String? = null // Optional ID field
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(customerName)
        parcel.writeString(customerPhone)
        parcel.writeString(customerEmail)
        parcel.writeString(customerAddress)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Customer> {
        override fun createFromParcel(parcel: Parcel): Customer {
            return Customer(parcel)
        }

        override fun newArray(size: Int): Array<Customer?> {
            return arrayOfNulls(size)
        }
    }
}
