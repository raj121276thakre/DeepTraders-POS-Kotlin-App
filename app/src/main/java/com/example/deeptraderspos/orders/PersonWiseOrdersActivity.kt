package com.example.deeptraderspos.orders

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityPersonWiseOrdersBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.pos.PosActivity
import com.google.firebase.firestore.DocumentSnapshot
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


        // Go Back Button
        val goBackBtn = binding.goBackBtn
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

        // Go to POS Activity Button
        val gotoPosBtn = binding.gotoPosBtn
        gotoPosBtn.setOnClickListener {
            val intent =
                Intent(this, PosActivity::class.java)  // Replace with your POS Activity class name
            startActivity(intent)
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


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date as yyyy-MM-dd
                val selectedDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)

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
                        Toast.makeText(this, "No orders found for the selected date", Toast.LENGTH_SHORT)
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
            orderRef.whereEqualTo(
                "supplierName",
                name
            ) // Filter by supplierName when isSupplier is true
        } else {
            orderRef.whereEqualTo("customerName", name) // Filter by customerName otherwise
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