package com.example.deeptraderspos.models

data class Order(
    val orderId: String = "",
    val orderDate: String = "",
    val orderTime: String = "",
    val orderType: String = "",
    val orderStatus: String = "Pending",
    val paymentMethod: String = "",
    val customerName: String = "",
    val tax: Double,
    val discount: String = "",
    val products: List<ProductOrder>,
    val totalPrice: Double,
)

data class ProductOrder(
    val productId: String,
    val productName: String,
    val productWeight: Double,
    val quantity: Int,
    val productPrice: Double
)
