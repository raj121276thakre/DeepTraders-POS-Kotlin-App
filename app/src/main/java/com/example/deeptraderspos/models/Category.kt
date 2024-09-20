package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class Category(
    val id: String? = null , // Unique ID for the category
    val categoryName: String =" ",   // Name of the category
    val categoryImage: String? = null  // Image URL or resource identifier (optional)
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(categoryName)
        parcel.writeString(categoryImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}
