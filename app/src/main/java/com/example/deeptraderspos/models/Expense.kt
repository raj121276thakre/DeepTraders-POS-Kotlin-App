package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class Expense(
    val expenseName: String = "",
    val expenseNote: String? = null,
    val expenseAmount: Double= 0.0,
    val expenseDate: String = "",
    val expenseTime: String = "",
    val id: String? = null // Optional ID field
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(expenseName)
        parcel.writeString(expenseNote)
        parcel.writeDouble(expenseAmount)
        parcel.writeString(expenseDate)
        parcel.writeString(expenseTime)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }
}
