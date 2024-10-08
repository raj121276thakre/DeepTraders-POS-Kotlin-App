package com.example.deeptraderspos.orders

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.adapter.ProductOrderAdapter
import com.example.deeptraderspos.customers.CustomersActivity
import com.example.deeptraderspos.databinding.ActivityPersonWiseOrdersBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.ProductOrder
import com.example.deeptraderspos.pos.PosActivity
import com.example.deeptraderspos.suppliers.SuppliersActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class PersonWiseOrdersActivity : InternetCheckActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var personOrderAdapter: PersonOrderAdapter
    private val ordersList = mutableListOf<Order>() // List to hold fetched orders
    private val filteredOrdersList = mutableListOf<Order>()
    private lateinit var binding: ActivityPersonWiseOrdersBinding // ViewBinding

    private lateinit var productOrderAdapter: ProductOrderAdapter

    private var selectedCustomerID: String = "0" // Store selected supplier ID

    // private lateinit var order: Order
    private lateinit var name: String
    private var isSupplier: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPersonWiseOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.personWiseOrders)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set status bar color
        Utils.setStatusBarColor(this)

        firestore = FirebaseFirestore.getInstance()
        // Retrieve the order from the intent
        // order = intent.getParcelableExtra<Order>("order") ?: return

        name = intent.getStringExtra("name").toString()
        isSupplier =
            intent.getBooleanExtra("isSupplier", false) // Default is false (customer) if not found


        setToolBarTitle(name)

        if (isSupplier) {
            binding.fabAddBillManually.visibility = View.GONE
            binding.fabAddBill.visibility = View.VISIBLE
        } else {
            binding.fabAddBillManually.visibility = View.VISIBLE
            binding.fabAddBill.visibility = View.GONE
        }

        // Go Back Button
        val goBackBtn = binding.goBackBtn
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

        // Go to POS Activity Button
        val gotoPosBtn = binding.gotoPosBtn
        gotoPosBtn.setOnClickListener {
            val intent =
                Intent(this, PosActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("isSupplier", isSupplier)// Replace with your POS Activity class name
            startActivity(intent)
        }

        binding.fabAddBill.setOnClickListener {
            val intent =
                Intent(this, PosActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("isSupplier", isSupplier)// Replace with your POS Activity class name
            startActivity(intent)
        }

        binding.fabAddBillManually.setOnClickListener {
            showOrderDetailsDialog()
        }

        // Setup RecyclerView and Adapter
        setupRecyclerView()


        fetchOrdersByPersonName(name)

        binding.resetFilterBtn.setOnClickListener {
            // Fetch orders from Firestore
            // Filter orders based on selected customer ID
            fetchOrdersByPersonName(name)
            // Clear the date text from the TextView
            binding.txtSelectDate.text = getString(R.string.select_date)
        }

        binding.txtSelectDate.setOnClickListener {
            showDatePickerDialog()
        }


    }


    //


    private fun showOrderDetailsDialog() {
        // Show the dialog

        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_bill, null)
        dialog.setView(dialogView)
        dialog.setCancelable(false)


        // Initialize dialog views
        val txtCustomer = dialogView.findViewById<TextView>(R.id.dialog_customer)
        val txtPaymentMethod = dialogView.findViewById<TextView>(R.id.dialog_payMethod)
        val txtOrderType = dialogView.findViewById<TextView>(R.id.dialog_orderType)
        val edtSubtotal = dialogView.findViewById<TextView>(R.id.dialog_txt_subtotal)
        val edtDiscount = dialogView.findViewById<EditText>(R.id.etxt_discount)
        val edtTax = dialogView.findViewById<TextView>(R.id.dialog_txt_total_tax)
        val txtTotalAmount = dialogView.findViewById<TextView>(R.id.dialog_txt_total_price)
        val edtTotalPaid = dialogView.findViewById<EditText>(R.id.etxt_total_paid)
        val txtRemainingAmount = dialogView.findViewById<TextView>(R.id.dialog_txt_remaining_amount)
        val btnSubmitOrder = dialogView.findViewById<Button>(R.id.btn_submit)

        val btnAddProduct = dialogView.findViewById<Button>(R.id.btnAddProduct)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btn_close)
        val recyclerViewProducts = dialogView.findViewById<RecyclerView>(R.id.rvProducts)


        val paidAmount = edtTotalPaid.text.toString().trim().toDoubleOrNull()

        // Setup RecyclerView
        val productOrders = ArrayList<ProductOrder>()
        productOrderAdapter = ProductOrderAdapter(
            this,
            productOrders,
            edtSubtotal,
            txtTotalAmount,
            txtRemainingAmount,
            paidAmount
        )
        recyclerViewProducts.adapter = productOrderAdapter
        recyclerViewProducts.layoutManager = LinearLayoutManager(this)

        // Set up listeners for customer, payment method, and order type selection
        txtCustomer.setOnClickListener { showCustomersList(txtCustomer) }
        txtPaymentMethod.setOnClickListener { showPaymentMethodsList(txtPaymentMethod) }
        txtOrderType.setOnClickListener { showOrderTypesList(txtOrderType) }


        // Add product button
        btnAddProduct.setOnClickListener {
            showProductSelectionDialog(productOrders, productOrderAdapter)
        }




        txtCustomer.text = name
        txtOrderType.text = "Home Delivery"
        txtPaymentMethod.text = "Upi"
        txtRemainingAmount.text = productOrderAdapter.getTotalPrice().toString()


        // TextWatcher to recalculate total and remaining amounts when values change
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recalculateTotal(edtTotalPaid, txtRemainingAmount)
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        edtTotalPaid.addTextChangedListener(textWatcher)

        val alertDialog = dialog.create()


        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        btnSubmitOrder.setOnClickListener {
            // Reset previous errors
            txtOrderType.error = null
            txtPaymentMethod.error = null
            txtCustomer.error = null


            // Get values from the fields
            val orderType = txtOrderType.text.toString()
            val paymentMethod = txtPaymentMethod.text.toString()
            val customer = txtCustomer.text.toString()
            val discount = "0"
            val totalCost = txtTotalAmount.text.toString()
            val totalPaidAmount = edtTotalPaid.text.toString()
            val remainingAmount = txtRemainingAmount.text.toString()



            proceedOrder(
                type = orderType,
                paymentMethod = paymentMethod,
                name = customer, // Pass customer name
                discount = discount,
                totalCostString = totalCost,
                totalPaidAmount = totalPaidAmount,
                remainingAmount = remainingAmount,
                isSupplier = false // It's a customer order
            )


        }


        alertDialog.show()
    }


    private fun proceedOrder(
        type: String,
        paymentMethod: String,
        name: String, // Can be customer or supplier name
        discount: String,
        totalCostString: String,
        totalPaidAmount: String,
        remainingAmount: String,
        isSupplier: Boolean // Flag to distinguish between customer and supplier
    ) {
        // Get total number of products from the adapter
        val itemCount = productOrderAdapter.itemCount

        // Check if there are products in the order
        if (itemCount > 0) {
            // Get the current date and time
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
            val currentTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())

            // Convert string amounts to double
            val totalCost = totalCostString.replace("₹", "").trim().toDoubleOrNull()
            val totalRemaining = remainingAmount.replace("₹", "").trim().toDoubleOrNull()
            val totalPaidAmountDouble = totalPaidAmount.toDoubleOrNull()

            // Determine the order status
            val orderStatus =
                if (totalRemaining == 0.0 || (totalPaidAmountDouble != null && totalPaidAmountDouble >= (totalCost
                        ?: 0.0))
                ) {
                    "Completed"
                } else {
                    "Pending"
                }

            // Build the product list from the adapter
            val productsList = ArrayList<ProductOrder>().apply {
                for (i in 0 until itemCount) {
                    val productItem = productOrderAdapter.getItem(i)
                    add(
                        ProductOrder(
                            productId = productItem.productId ?: "",
                            productName = productItem.productName ?: "",
                            productWeight = productItem.productWeight,
                            quantity = productItem.quantity,
                            productPrice = productItem.productPrice
                        )
                    )
                }
            }

            // Generate a unique order ID
            generateOrderId(isSupplier) { orderId ->
                // Create an Order object with the gathered details
                val order = Order(
                    orderId = orderId,
                    orderDate = currentDate,
                    orderTime = currentTime,
                    orderType = type,
                    paymentMethod = paymentMethod,
                    customerName = if (!isSupplier) name else "", // Set customer name
                    supplierName = if (isSupplier) name else "", // Set supplier name
                    tax = 0.0,
                    discount = discount,
                    products = productsList,
                    totalPrice = totalCost ?: 0.0,
                    totalPaidAmount = totalPaidAmountDouble ?: 0.0,
                    remainingAmount = totalRemaining ?: 0.0,
                    updatedRemainingAmount = totalRemaining ?: 0.0,
                    updatedTotalPaidAmount = totalPaidAmountDouble ?: 0.0,
                    orderStatus = orderStatus
                )

                // Choose the collection based on whether it's a supplier or customer order
                val collection = if (isSupplier) "AllOrdersSuppliers" else "AllOrders"

                // Save the order to Firestore
                firestore.collection(collection)
                    .document(orderId)
                    .set(order)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "${if (isSupplier) "Supplier" else "Customer"} Order placed successfully!",
                            Toast.LENGTH_SHORT
                        ).show()


                        // Redirect to the appropriate screen based on whether it's a supplier or customer order
                        if (isSupplier) {
                            startActivity(Intent(this, SuppliersActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this, CustomersActivity::class.java))
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error placing order: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            // No products in the cart, show a message
            Toast.makeText(this, R.string.no_product_in_cart, Toast.LENGTH_SHORT).show()
        }
    }




    private fun generateOrderId(isSupplier: Boolean, onOrderIdGenerated: (String) -> Unit) {
        // val ordersCollection = firestore.collection("AllOrders")
        val ordersCollection =
            firestore.collection(if (isSupplier) "AllOrdersSuppliers" else "AllOrders")

        // Get total number of orders
        ordersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val totalOrders = querySnapshot.size() // Get the count of orders
                val newOrderNumber = totalOrders + 1

                // Dynamically calculate the padding size based on the total number of orders
                val length = when {
                    newOrderNumber < 1000 -> 4 // If less than 1000, use 4 digits
                    newOrderNumber < 10000 -> 5 // If between 1000 and 9999, use 5 digits
                    newOrderNumber < 100000 -> 6 // If between 10000 and 99999, use 6 digits
                    newOrderNumber < 1000000 -> 7 // If between 100000 and 999999, use 7 digits
                    else -> newOrderNumber.toString().length // Adjust based on the number of digits
                }

                // Pad the order number with leading zeros to match the calculated length
                val paddedOrderId = newOrderNumber.toString().padStart(length, '0')

                // Prefix with 'C' for customer or 'S' for supplier based on the isSupplier flag
                val newOrderId = if (isSupplier) "S$paddedOrderId" else "C$paddedOrderId"

                // Return the generated orderId
                onOrderIdGenerated(newOrderId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error generating order ID: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    private fun recalculateTotal(edtTotalPaid: EditText, txtRemainingAmount: TextView) {
        // Get the total price from the adapter
        val totalAmount = productOrderAdapter.getTotalPrice()

        // Get the paid amount from the EditText
        val paidAmount = edtTotalPaid.text.toString().toDoubleOrNull() ?: 0.0

        // Calculate the remaining amount
        val remainingAmount = totalAmount - paidAmount
        txtRemainingAmount.text = "₹${String.format("%.2f", remainingAmount)}"
    }

    @SuppressLint("MissingInflatedId")
    private fun showProductSelectionDialog(
        productOrders: ArrayList<ProductOrder>,
        productOrderAdapter: ProductOrderAdapter
    ) {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_product_input,
            null
        ) // Update the layout to use a custom input dialog
        dialog.setView(dialogView)
        dialog.setCancelable(false)

        val edtProductName = dialogView.findViewById<EditText>(R.id.edt_product_name)
        val edtProductPrice = dialogView.findViewById<EditText>(R.id.edt_product_price)
        val edtQuantity = dialogView.findViewById<EditText>(R.id.edt_quantity)
        val btnAddProduct = dialogView.findViewById<Button>(R.id.btn_add_product)

        val alertDialog = dialog.create()

        btnAddProduct.setOnClickListener {
            val productName = edtProductName.text.toString()
            val productPrice = edtProductPrice.text.toString().toDoubleOrNull()
            val quantity = edtQuantity.text.toString().toIntOrNull()



            if (productName.isNotEmpty() && productPrice != null && quantity != null && quantity > 0) {
                val productOrder = ProductOrder(
                    productId = "", // You can generate a unique ID or leave it empty
                    productName = productName,
                    productPrice = productPrice,
                    quantity = quantity
                )

                productOrders.add(productOrder)
                productOrderAdapter.notifyDataSetChanged()
                alertDialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter valid product details", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        alertDialog.show()
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
                        customerInfo[key] =
                            value?.toString() ?: "N/A" // Convert each value to String
                    }
                    customerData.add(customerInfo)
                }

                customerAdapter.addAll(customerNames)

                val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
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

                val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
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
                    val orderTypeName = document.getString("orderTypeName")
                        ?: "Unknown com.example.deeptraderspos.models.Order Type"
                    orderTypes.add(orderTypeName)

                    // Safely convert each field to string, if null provide a default value
                    val orderInfo = mutableMapOf<String, String>()
                    for ((key, value) in document.data) {
                        orderInfo[key] = value?.toString() ?: "" // Safely handle null values
                    }
                    orderTypeData.add(orderInfo)
                }

                orderTypeAdapter.addAll(orderTypes)

                val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
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


//..........


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date as yyyy-MM-dd
                val selectedDate =
                    String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)

                // Update the TextView with the selected date
                binding.txtSelectDate.text = selectedDate

                // Filter the orders by the selected date
                filterOrdersByDate(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }


    private fun filterOrdersByDate(selectedDate: String) {
        filteredOrdersList.clear() // Clear previous filtered orders

        val orderRef = firestore.collection(if (isSupplier) "AllOrdersSuppliers" else "AllOrders")

        // Parse the selectedDate to a Date object (assuming date format is yyyy-MM-dd)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date? = simpleDateFormat.parse(selectedDate)



        if (date != null) {

            val query = if (isSupplier) {
                // Filter by supplier name and date (assuming orderDate is stored as String "yyyy-MM-dd")
                orderRef.whereEqualTo("supplierName", name)
                    .whereEqualTo("orderDate", selectedDate)
            } else {
                // Filter by customer name and date (assuming orderDate is stored as String "yyyy-MM-dd")
                orderRef.whereEqualTo("customerName", name)
                    .whereEqualTo("orderDate", selectedDate)
            }

            query.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val order = document.toObject(Order::class.java)
                        filteredOrdersList.add(order) // Add the order to the filtered list
                    }

                    // Sort the filtered orders by orderDate (optional if already sorted by Firestore)
                    filteredOrdersList.sortByDescending { it.orderDate }

                    // Update the adapter with the filtered list
                    updateAdapter(filteredOrdersList)

                    // Show a message if no orders were found
                    if (filteredOrdersList.isEmpty()) {
                        Toast.makeText(
                            this,
                            "No orders found for the selected date",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setToolBarTitle(name: String) {
        if (isSupplier) {
            binding.toolbarTitle.text = getString(R.string.suppliers_wise_orders) + " " + name
        } else {
            binding.toolbarTitle.text = getString(R.string.customers_wise_orders) + " " + name
        }
    }


    // Setup RecyclerView and attach Adapter. ,
    private fun setupRecyclerView() {
        personOrderAdapter = PersonOrderAdapter(this, ordersList, isSupplier)
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.ordersRecyclerView.adapter = personOrderAdapter
    }


    private fun fetchOrdersByPersonName(name: String) {
        filteredOrdersList.clear() // Clear previous filtered orders
        val orderRef = firestore.collection(if (isSupplier) "AllOrdersSuppliers" else "AllOrders")

        // Conditionally filter by supplierName or customerName
        val query = if (isSupplier) {
            orderRef
                .whereEqualTo(
                    "supplierName",
                    name
                )
        } else {
            orderRef
                .whereEqualTo("customerName", name)

        }



        query.get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    val order = document.toObject(Order::class.java)
                    filteredOrdersList.add(order) // Add the order to the filtered list
                }

                // Show a message if no orders were found
                if (filteredOrdersList.isEmpty()) {
                    Toast.makeText(this, "No orders found for $name", Toast.LENGTH_SHORT)
                        .show()
                }

                // Reverse the order to show the latest orders at the top
                filteredOrdersList.reverse()
                // Update the adapter with the filtered list
                updateAdapter(filteredOrdersList)


            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    // Update the adapter with fetched orders
    private fun updateAdapter(orders: List<Order>) {
        personOrderAdapter.updatePersonOrderData(orders)
        personOrderAdapter.notifyDataSetChanged()  // Notify the adapter that data has changed

    }


}