package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class PaymentMethod(
    val id: String? = null , // Unique ID for the category
    val paymentMethodName: String =" ",   // Name of the category
    val paymentMethodImage: String? = null  // Image URL or resource identifier (optional)
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(paymentMethodName)
        parcel.writeString(paymentMethodImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaymentMethod> {
        override fun createFromParcel(parcel: Parcel): PaymentMethod {
            return PaymentMethod(parcel)
        }

        override fun newArray(size: Int): Array<PaymentMethod?> {
            return arrayOfNulls(size)
        }
    }
}
