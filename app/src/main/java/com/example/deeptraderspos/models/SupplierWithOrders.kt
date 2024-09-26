package com.example.deeptraderspos.models

data class SupplierWithOrders(
    val supplier: Supplier,
    val orders: List<Order>
)
