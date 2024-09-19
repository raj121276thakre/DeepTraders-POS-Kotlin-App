package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class Supplier(
    val supplierName: String = "",
    val supplierPhone: String = "",
    val supplierEmail: String? = null,
    val supplierAddress: String? = null,
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
        parcel.writeString(supplierName)
        parcel.writeString(supplierPhone)
        parcel.writeString(supplierEmail)
        parcel.writeString(supplierAddress)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Supplier> {
        override fun createFromParcel(parcel: Parcel): Supplier {
            return Supplier(parcel)
        }

        override fun newArray(size: Int): Array<Supplier?> {
            return arrayOfNulls(size)
        }
    }
}