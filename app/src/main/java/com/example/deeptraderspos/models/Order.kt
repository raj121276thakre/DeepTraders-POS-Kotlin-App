package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class Order(
    val orderId: String = "",
    val orderDate: String = "",
    val orderTime: String = "",
    val orderType: String = "",
    val orderStatus: String = "Pending",
    val paymentMethod: String = "",
    val customerName: String = "",
    val tax: Double = 0.0,
    val discount: String = "",
    val products: List<ProductOrder> =  emptyList(),
    val totalPrice: Double = 0.0,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readString().toString(),
        TODO("products"),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(orderId)
        parcel.writeString(orderDate)
        parcel.writeString(orderTime)
        parcel.writeString(orderType)
        parcel.writeString(orderStatus)
        parcel.writeString(paymentMethod)
        parcel.writeString(customerName)
        parcel.writeDouble(tax)
        parcel.writeString(discount)
        parcel.writeDouble(totalPrice)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Order> {
        override fun createFromParcel(parcel: Parcel): Order {
            return Order(parcel)
        }

        override fun newArray(size: Int): Array<Order?> {
            return arrayOfNulls(size)
        }
    }
}

data class ProductOrder(
    val productId: String = "",
    val productName: String = "",
    val productWeight: Double = 0.0,
    val quantity: Int = 1,
    val productPrice: Double = 0.0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productId)
        parcel.writeString(productName)
        parcel.writeDouble(productWeight)
        parcel.writeInt(quantity)
        parcel.writeDouble(productPrice)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductOrder> {
        override fun createFromParcel(parcel: Parcel): ProductOrder {
            return ProductOrder(parcel)
        }

        override fun newArray(size: Int): Array<ProductOrder?> {
            return arrayOfNulls(size)
        }
    }
}
