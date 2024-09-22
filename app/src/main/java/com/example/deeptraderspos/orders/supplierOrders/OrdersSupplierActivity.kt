package com.example.deeptraderspos.orders.supplierOrders

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.deeptraderspos.databinding.ActivityOrdersBinding
import com.example.deeptraderspos.databinding.ActivityOrdersSupplierBinding
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.orders.customerOrders.OrderAdapter
import com.google.firebase.firestore.FirebaseFirestore

class OrdersSupplierActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var orderAdapter: OrderAdapter
    private val ordersList = mutableListOf<Order>() // List to hold fetched orders
    private val filteredOrdersList = mutableListOf<Order>()
    private lateinit var binding: ActivityOrdersSupplierBinding // ViewBinding

    private var selectedSupplierID: String = "0" // Store selected supplier ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrdersSupplierBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.allOrders_supplier)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView and Adapter
        setupRecyclerView()
        // Fetch orders from Firestore
        fetchOrders()

        binding.etxtSortOrder.setOnClickListener {
            showSuppliersList(binding.etxtSortOrder)
        }

        binding.resetFilterBtn.setOnClickListener {
            // Fetch orders from Firestore
            fetchOrders()
            binding.etxtSortOrder.hint = getString(R.string.sort_order_supplier)
            binding.etxtSortOrder.text?.clear() // Clear the text to show hint
        }
    }

    private fun showSuppliersList(dialogSupplier: TextView) {
        val supplierAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)

        // Fetch suppliers from Firestore
        firestore.collection("AllSuppliers")
            .get()
            .addOnSuccessListener { documents ->
                val supplierNames = mutableListOf<String>()
                val supplierData = mutableListOf<Map<String, String>>()

                for (document in documents) {
                    val supplierName = document.getString("supplierName") ?: ""
                    supplierNames.add(supplierName)

                    // Create a new Map<String, String> to store supplier data
                    val supplierInfo = mutableMapOf<String, String>()
                    for ((key, value) in document.data) {
                        supplierInfo[key] = value.toString() // Convert each value to String
                    }
                    supplierData.add(supplierInfo)
                }

                supplierAdapter.addAll(supplierNames)

                val dialog = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.dialog_list_search, null)
                dialog.setView(dialogView)
                dialog.setCancelable(false)

                val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)
                val dialogInput = dialogView.findViewById<EditText>(R.id.dialog_input)
                val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                val dialogList = dialogView.findViewById<ListView>(R.id.dialog_list)

                dialogTitle.setText(R.string.suppliers)
                dialogList.adapter = supplierAdapter

                // Implement search functionality
                dialogInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                        supplierAdapter.filter.filter(charSequence)
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
                    val selectedItem = supplierAdapter.getItem(position) ?: return@setOnItemClickListener

                    dialogSupplier.text = selectedItem

                    var supplierId = "0"
                    for (i in supplierNames.indices) {
                        if (supplierNames[i].equals(selectedItem, ignoreCase = true)) {
                            supplierId = supplierData[i]["supplier_id"] ?: "0"
                        }
                    }

                    selectedSupplierID = supplierId

                    // Filter orders based on selected supplier ID
                    filterOrdersBySupplier(selectedSupplierID)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch suppliers: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Setup RecyclerView and attach Adapter.
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(this, ordersList, true)
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.ordersRecyclerView.adapter = orderAdapter
    }

    // Fetch orders from Firestore and update the adapter
    private fun fetchOrders() {
        firestore.collection("AllOrdersSuppliers")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedOrders = mutableListOf<Order>()
                for (document in documents) {
                    val order = document.toObject(Order::class.java) // Convert Firestore document to Order object
                    fetchedOrders.add(order) // Add order to list
                }
                // Reverse the order to show the latest orders at the top
                fetchedOrders.reverse()
                updateAdapter(fetchedOrders) // Update the adapter with the new data
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterOrdersBySupplier(supplierName: String) {
        filteredOrdersList.clear() // Clear previous filtered orders

        // Fetch orders from Firestore for the selected supplier name
        firestore.collection("AllOrdersSuppliers")
            .whereEqualTo("supplierName", supplierName) // Adjust this field name based on your Firestore structure
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val order = document.toObject(Order::class.java)
                    filteredOrdersList.add(order) // Add the order to the filtered list
                }
                // Reverse the order to show the latest orders at the top
                filteredOrdersList.reverse()
                // Update the adapter with the filtered list
                updateAdapter(filteredOrdersList)

                // Show a message if no orders were found
                if (filteredOrdersList.isEmpty()) {
                    Toast.makeText(this, "No orders found for $supplierName", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Update the adapter with fetched orders
    private fun updateAdapter(orders: List<Order>) {
        orderAdapter.updateOrderData(orders)
        orderAdapter.notifyDataSetChanged() // Notify the adapter that data has changed
    }
}
