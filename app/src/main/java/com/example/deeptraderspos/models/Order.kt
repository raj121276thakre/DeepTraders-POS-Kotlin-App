package com.example.deeptraderspos.models

import android.os.Parcel
import android.os.Parcelable

data class Order(
    val orderId: String = "",
    val orderDate: String = "",
    val orderTime: String = "",
    val orderType: String = "",
    val orderStatus: String = "",
    val paymentMethod: String = "",
    val customerName: String = "",
    val supplierName: String = "",
    val tax: Double = 0.0,
    val discount: String = "",
    val products: List<ProductOrder> = emptyList(),  // Use List instead of ArrayList
    val totalPrice: Double = 0.0,
    val totalPaidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val remainingAmtPaidDate: String = "",
    val remainingAmtPaidTime: String = "",
    var remainingPayments: List<RemainingPayment> = emptyList(),
    var updatedRemainingAmount: Double= 0.0,
    var updatedTotalPaidAmount: Double= 0.0,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.createTypedArrayList(ProductOrder.CREATOR) ?: emptyList(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(RemainingPayment.CREATOR)?: emptyList(),
        parcel.readDouble(),
        parcel.readDouble(),
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
        parcel.writeString(supplierName)
        parcel.writeDouble(tax)
        parcel.writeString(discount)
        parcel.writeTypedList(products)
        parcel.writeDouble(totalPrice)
        parcel.writeDouble(totalPaidAmount)
        parcel.writeDouble(remainingAmount)
        parcel.writeString(remainingAmtPaidDate)
        parcel.writeString(remainingAmtPaidTime)
        parcel.writeDouble(updatedRemainingAmount)
        parcel.writeDouble(updatedTotalPaidAmount)
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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readDouble()
    )

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

data class RemainingPayment(
    var paidAmount: Double = 0.0,
    var paidDate: String? = null,
    var paidTime: String? = null,
    var remainingAmount: Double = 0.0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(paidAmount)
        parcel.writeString(paidDate)
        parcel.writeString(paidTime)
        parcel.writeDouble(remainingAmount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RemainingPayment> {
        override fun createFromParcel(parcel: Parcel): RemainingPayment {
            return RemainingPayment(parcel)
        }

        override fun newArray(size: Int): Array<RemainingPayment?> {
            return arrayOfNulls(size)
        }
    }
}
