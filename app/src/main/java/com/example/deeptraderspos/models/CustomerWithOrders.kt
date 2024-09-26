package com.example.deeptraderspos.models



data class CustomerWithOrders(
    val customer: Customer,    // Holds customer details
    val orders: List<Order>    // Holds all orders for that customer
)
