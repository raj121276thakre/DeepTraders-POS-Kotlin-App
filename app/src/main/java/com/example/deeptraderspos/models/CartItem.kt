package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class CartItem(
    val productId: String? = null,
    val productName: String? = null,
    // Unique ID for the product
    val productWeight: Double = 0.0,  // Weight of the product
    val weightUnitId: String = "",    // Unit ID for the weight (e.g., kg, lbs)
    val productPrice: Double = 0.0,   // Price of the product
    var quantity: Int = 1,            // Quantity of the product in the cart
    val productStock: Int = 0         // Stock of the product
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productId)
        parcel.writeString(productName)
        parcel.writeDouble(productWeight)
        parcel.writeString(weightUnitId)
        parcel.writeDouble(productPrice)
        parcel.writeInt(quantity)
        parcel.writeInt(productStock)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CartItem> {
        override fun createFromParcel(parcel: Parcel): CartItem {
            return CartItem(parcel)
        }

        override fun newArray(size: Int): Array<CartItem?> {
            return arrayOfNulls(size)
        }
    }
}