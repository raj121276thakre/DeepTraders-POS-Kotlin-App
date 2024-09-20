package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class Product(
    val productName: String = "",        // Default to empty string
    val productCode: String? = null,     // Optional, default to null
    val productCategory: String = "",     // Default to empty string
    val productDescription: String? = null, // Optional, default to null
    val buyPrice: Double = 0.0,          // Default to 0.0
    val sellPrice: Double = 0.0,         // Default to 0.0
    val stock: Int = 0,                  // Default to 0
    val weight: Double? = null,          // Optional, default to null
    val weightUnit: String? = null,      // Optional, default to null
    val supplier: String = "",            // Default to empty string
    val productImage: String? = null,    // Optional, default to null
    val id: String? = null                // Optional, default to null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productName)
        parcel.writeString(productCode)
        parcel.writeString(productCategory)
        parcel.writeString(productDescription)
        parcel.writeDouble(buyPrice)
        parcel.writeDouble(sellPrice)
        parcel.writeInt(stock)
        parcel.writeValue(weight)
        parcel.writeString(weightUnit)
        parcel.writeString(supplier)
        parcel.writeString(productImage)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}
