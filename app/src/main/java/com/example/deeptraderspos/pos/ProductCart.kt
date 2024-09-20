package com.example.deeptraderspos.pos

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.adapter.CartAdapter
import com.example.deeptraderspos.databinding.ActivityProductCartBinding
import com.example.deeptraderspos.databinding.DialogPaymentBinding
import com.example.deeptraderspos.models.CartItem
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.ProductOrder
import com.example.deeptraderspos.models.ShopInformation
import com.example.deeptraderspos.orders.OrdersActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProductCart : AppCompatActivity() {

    private lateinit var binding: ActivityProductCartBinding

    private lateinit var productCartAdapter: CartAdapter
    private lateinit var f: DecimalFormat
    private lateinit var firestore: FirebaseFirestore

    private var selectedCustomerID: String = "0" // Store selected supplier ID
    private var tax: Double = 0.0 //


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cart)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setHomeButtonEnabled(true) // for back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // for back button
        supportActionBar?.title = getString(R.string.product_cart)

        f = DecimalFormat("#0.00")

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        val linearLayoutManager = LinearLayoutManager(this)
        binding.cartRecyclerview.layoutManager = linearLayoutManager
        binding.cartRecyclerview.setHasFixedSize(true)

        // Fetch cart products from Firestore
        fetchCartProducts()
        fetchShopInfo()

        binding.btnSubmitOrder.setOnClickListener { dialog() }


    }


    private fun fetchCartProducts() {
        firestore.collection("carts")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Handle empty cart UI
                    binding.imageNoProduct.setImageResource(R.drawable.empty_cart)
                    binding.imageNoProduct.visibility = View.VISIBLE
                    binding.txtNoProduct.visibility = View.VISIBLE
                    binding.btnSubmitOrder.visibility = View.GONE
                    binding.cartRecyclerview.visibility = View.GONE
                    binding.linearLayout.visibility = View.GONE
                    binding.txtTotalPrice.visibility = View.GONE
                } else {
                    // Hide "No Product" UI when products exist
                    binding.imageNoProduct.visibility = View.GONE

                    // Create a list to hold CartItem objects
                    val cartProductList = ArrayList<CartItem>()

                    // Loop through Firestore documents and map them to CartItem objects
                    for (document in documents) {

                        val cartItem = CartItem(
                            productId = document.getString("productId") ?: "",
                            productName = document.getString("productName") ?: "",
                            productWeight = document.getDouble("productWeight") ?: 0.0,
                            weightUnitId = document.getString("weightUnitId") ?: "",
                            productPrice = document.getDouble("productPrice") ?: 0.0,
                            quantity = document.getLong("quantity")?.toInt() ?: 1,
                            productStock = document.getLong("productStock")?.toInt() ?: 0
                        )
                        cartProductList.add(cartItem)
                    }

                    // Set up the adapter with the list of CartItem objects
                    productCartAdapter = CartAdapter(
                        this, cartProductList,
                        binding.txtTotalPrice, binding.btnSubmitOrder,
                        binding.imageNoProduct, binding.txtNoProduct
                    )
                    binding.cartRecyclerview.adapter = productCartAdapter
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading cart: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchShopInfo() {
        firestore.collection("shops").document("shopInfo")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val shopInfo = ShopInformation(
                        shopName = document.getString("shopName") ?: "",
                        contactNumber = document.getString("shopContact") ?: "",
                        email = document.getString("shopEmail") ?: "",
                        address = document.getString("shopAddress") ?: "",
                        currencySymbol = document.getString("shopCurrency") ?: "",
                        taxPercentage = (document.getString("shopTax"))!!.toDouble(),
                        id = document.id // Get the document ID
                    )

                    tax = shopInfo.taxPercentage


                } else {
                    Toast.makeText(this, "Shop information not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to fetch shop information: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun dialog() {
        val dialogBinding = DialogPaymentBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()


        val total_cost: Double = productCartAdapter.getTotalPrice()
        dialogBinding.dialogTxtSubtotal.setText("₹" + f.format(total_cost))

        val calculated_tax: Double = (total_cost * tax) / 100.0
        dialogBinding.dialogTxtTotalTax.setText("₹" + f.format(calculated_tax))
        dialogBinding.dialogLevelTax.setText("Total Tax ($tax%)")


        val discount = 0.0
        val calculated_total_cost = total_cost + calculated_tax - discount
        dialogBinding.dialogTxtTotalCost.setText("₹" + f.format(calculated_total_cost))



        dialogBinding.etxtDialogDiscount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var discount = 0.0
                val get_discount = s.toString()
                if (!get_discount.isEmpty() && get_discount != ".") {
                    var calculated_total_cost = total_cost + calculated_tax
                    discount = get_discount.toDouble()
                    if (discount > calculated_total_cost) {
                        dialogBinding.etxtDialogDiscount.setError(getString(R.string.discount_cant_be_greater_than_total_price))
                        dialogBinding.etxtDialogDiscount.requestFocus()

                        dialogBinding.btnSubmit.setVisibility(View.INVISIBLE)
                    } else {
                        dialogBinding.btnSubmit.setVisibility(View.VISIBLE)
                        calculated_total_cost = total_cost + calculated_tax - discount
                        dialogBinding.dialogTxtTotalCost.setText(
                            "₹" + f.format(
                                calculated_total_cost
                            )
                        )
                    }
                } else {
                    val calculated_total_cost = total_cost + calculated_tax - discount
                    dialogBinding.dialogTxtTotalCost.setText("₹" + f.format(calculated_total_cost))
                }
            }

            override fun afterTextChanged(s: Editable) {
            }
        })




        dialogBinding.btnSubmit.setOnClickListener {
            // Proceed with the order
            proceedOrder(
                dialogBinding.dialogOrderType.text.toString(),
                dialogBinding.dialogOrderPayment.text.toString(),
                dialogBinding.dialogCustomer.text.toString(),
                calculated_tax,  // You can calculate this properly
                dialogBinding.etxtDialogDiscount.text.toString(),
                dialogBinding.dialogTxtTotalCost.text.toString()
            )
            dialog.dismiss()
        }

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.selectCustomer.setOnClickListener {
            showCustomersList(dialogBinding.dialogCustomer)
        }

        dialogBinding.selectOrderType.setOnClickListener {
            showOrderTypesList(dialogBinding.dialogOrderType)
        }

        dialogBinding.selectOrderPayment.setOnClickListener {
            showPaymentMethodsList(dialogBinding.dialogOrderPayment)
        }

        dialog.show()
    }


    private fun proceedOrder(
        type: String,
        paymentMethod: String,
        customerName: String,
        calculatedTax: Double,
        discount: String,
        totalCostString: String
    ) {
        val itemCount = productCartAdapter.itemCount
        if (itemCount > 0) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
            val currentTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())

            val numericValueString = totalCostString.replace("₹", "").trim() // Remove the currency symbol and trim any whitespace
            val totalCost: Double? = numericValueString.toDoubleOrNull()

            val productsList = mutableListOf<ProductOrder>()
            for (i in 0 until itemCount) {
                val cartItem = productCartAdapter.getItem(i)
                val productOrder = ProductOrder(
                    productId = cartItem.productId ?: "",
                    productName = cartItem.productName ?: "",
                    productWeight = cartItem.productWeight,
                    quantity = cartItem.quantity,
                    productPrice = cartItem.productPrice
                )
                productsList.add(productOrder)
            }

            // Generate a unique order ID
            val orderId = UUID.randomUUID().toString()

            val order = Order(
                orderId = orderId,
                orderDate = currentDate,
                orderTime = currentTime,
                orderType = type,
                paymentMethod = paymentMethod,
                customerName = customerName,
                tax = calculatedTax,
                discount = discount,
                products = productsList,
                totalPrice = totalCost!!
            )

            // Save order to Firestore
            firestore.collection("AllOrders")
                .document(orderId) // Use the generated order ID
                .set(order)
                .addOnSuccessListener {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                    deleteCartItems() // Call method to delete cart items
                    startActivity(Intent(this, OrdersActivity::class.java))
                    finish() // Return to previous activity
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error placing order: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, R.string.no_product_in_cart, Toast.LENGTH_SHORT).show()
        }
    }



    private fun deleteCartItems() {
        val firestore = FirebaseFirestore.getInstance()

        // Get all cart items from the adapter
        for (i in 0 until productCartAdapter.itemCount) {
            val cartItem = productCartAdapter.getItem(i)
            val cartId = cartItem.productId // Use the productId as the document ID

            if (!cartId.isNullOrEmpty()) {
                firestore.collection("carts").document(cartId).delete()
                    .addOnSuccessListener {
                        // Optionally, you can show a log or toast for each deleted item
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error removing item: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Optionally, show a toast after attempting to delete all items
        Toast.makeText(this, "Cart cleared successfully!", Toast.LENGTH_SHORT).show()
    }




    private fun showCustomersList(dialogCustomer: TextView) {
        val customerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)

        // Fetch customers from Firestore
        firestore.collection("AllCustomers")
            .get()
            .addOnSuccessListener { documents ->
                val customerNames = mutableListOf<String>()
                val customerData = mutableListOf<Map<String, String>>()

                for (document in documents) {
                    val customerName = document.getString("customerName") ?: ""
                    customerNames.add(customerName)

                    // Create a new Map<String, String> to store customer data
                    val customerInfo = mutableMapOf<String, String>()
                    for ((key, value) in document.data) {
                        customerInfo[key] = value.toString() // Convert each value to String
                    }
                    customerData.add(customerInfo)
                }

                customerAdapter.addAll(customerNames)

                val dialog = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.dialog_list_search, null)
                dialog.setView(dialogView)
                dialog.setCancelable(false)

                val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)
                val dialogInput = dialogView.findViewById<EditText>(R.id.dialog_input)
                val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                val dialogList = dialogView.findViewById<ListView>(R.id.dialog_list)

                dialogTitle.setText(R.string.customers)
                dialogList.adapter = customerAdapter

                // Implement search functionality
                dialogInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        charSequence: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        customerAdapter.filter.filter(charSequence)
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                val alertDialog = dialog.create()

                dialogButton.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()

                dialogList.setOnItemClickListener { parent, view, position, id ->
                    alertDialog.dismiss()
                    val selectedItem =
                        customerAdapter.getItem(position) ?: return@setOnItemClickListener

                    dialogCustomer.setText(selectedItem)

                    var customerId = "0"
                    for (i in customerNames.indices) {
                        if (customerNames[i].equals(selectedItem, ignoreCase = true)) {
                            customerId = customerData[i]["customer_id"] ?: "0"
                        }
                    }

                    selectedCustomerID = customerId
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch customers: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    private fun showPaymentMethodsList(dialogPaymentMethod: TextView) {
        val paymentAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)

        // Fetch payment methods from Firestore
        firestore.collection("AllPaymentMethods")
            .get()
            .addOnSuccessListener { documents ->
                val paymentMethods = mutableListOf<String>()
                val paymentData = mutableListOf<Map<String, String>>()

                for (document in documents) {
                    // Safely get the payment method name, provide a default value if it's null
                    val paymentMethodName =
                        document.getString("paymentMethodName") ?: "Unknown Payment Method"
                    paymentMethods.add(paymentMethodName)

                    // Create a new Map<String, String> to store payment method data, safely handle null values
                    val paymentInfo = mutableMapOf<String, String>()
                    for ((key, value) in document.data) {
                        // Safely convert each value to a string, using an empty string if the value is null
                        paymentInfo[key] = value?.toString() ?: ""
                    }
                    paymentData.add(paymentInfo)
                }


                paymentAdapter.addAll(paymentMethods)

                val dialog = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.dialog_list_search, null)
                dialog.setView(dialogView)
                dialog.setCancelable(false)

                val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)
                val dialogInput = dialogView.findViewById<EditText>(R.id.dialog_input)
                val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                val dialogList = dialogView.findViewById<ListView>(R.id.dialog_list)

                dialogTitle.setText(R.string.payment_methods)
                dialogList.adapter = paymentAdapter

                // Implement search functionality
                dialogInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        charSequence: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        paymentAdapter.filter.filter(charSequence)
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                val alertDialog = dialog.create()

                dialogButton.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()

                dialogList.setOnItemClickListener { parent, view, position, id ->
                    alertDialog.dismiss()
                    val selectedItem =
                        paymentAdapter.getItem(position) ?: return@setOnItemClickListener

                    dialogPaymentMethod.text = selectedItem
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to fetch payment methods: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun showOrderTypesList(dialogOrderType: TextView) {
        val orderTypeAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)

        // Fetch order types from Firestore
        firestore.collection("AllOrderTypes")
            .get()
            .addOnSuccessListener { documents ->
                val orderTypes = mutableListOf<String>()
                val orderTypeData = mutableListOf<Map<String, String>>()

                for (document in documents) {
                    // Safely get the order type name, provide a default value if null
                    val orderTypeName = document.getString("orderTypeName") ?: "Unknown Order Type"
                    orderTypes.add(orderTypeName)

                    // Safely convert each field to string, if null provide a default value
                    val orderInfo = mutableMapOf<String, String>()
                    for ((key, value) in document.data) {
                        orderInfo[key] = value?.toString() ?: "" // Safely handle null values
                    }
                    orderTypeData.add(orderInfo)
                }

                orderTypeAdapter.addAll(orderTypes)

                val dialog = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.dialog_list_search, null)
                dialog.setView(dialogView)
                dialog.setCancelable(false)

                val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)
                val dialogInput = dialogView.findViewById<EditText>(R.id.dialog_input)
                val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                val dialogList = dialogView.findViewById<ListView>(R.id.dialog_list)

                dialogTitle.setText(R.string.order_type)
                dialogList.adapter = orderTypeAdapter

                // Implement search functionality
                dialogInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        charSequence: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        orderTypeAdapter.filter.filter(charSequence)
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                val alertDialog = dialog.create()

                dialogButton.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()

                dialogList.setOnItemClickListener { parent, view, position, id ->
                    alertDialog.dismiss()
                    val selectedItem =
                        orderTypeAdapter.getItem(position) ?: return@setOnItemClickListener

                    dialogOrderType.text = selectedItem
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to fetch order types: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


}