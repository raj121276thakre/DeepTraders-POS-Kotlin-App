package com.example.deeptraderspos

object Constants {
    //We will use this to store the user token number into shared preference
    const val SHARED_PREF_NAME: String = "com.app.smartpos" //pcakage name+ id


    const val ORDER_STATUS: String = "order_status"


    //order status
    const val PENDING: String = "Pending"
    const val PROCESSING: String = "Processing"
    const val COMPLETED: String = "Completed"
    const val CANCEL: String = "Cancel"


    //all table names
    var customers: String = "customers"
    var users: String = "users"
    var suppliers: String = "suppliers"
    var productCategory: String = "product_category"
    var products: String = "products"
    var paymentMethod: String = "payment_method"
    var expense: String = "expense"
    var productCart: String = "product_cart"
    var orderList: String = "order_list"
    var orderDetails: String = "order_details"
}