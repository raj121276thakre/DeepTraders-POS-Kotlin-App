package com.example.deeptraderspos.orders

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.ActivityOrdersBinding
import com.example.deeptraderspos.models.Order
import com.google.firebase.firestore.FirebaseFirestore

class OrdersActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var orderAdapter: OrderAdapter
    private val ordersList = mutableListOf<Order>() // List to hold fetched orders
    private lateinit var binding: ActivityOrdersBinding // ViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.allOrders)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView and Adapter
        setupRecyclerView()

        // Fetch orders from Firestore
        fetchOrders()
    }

    // Setup RecyclerView and attach Adapter
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(this, ordersList)
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.ordersRecyclerView.adapter = orderAdapter
    }

    // Fetch orders from Firestore and update the adapter
    private fun fetchOrders() {
        firestore.collection("AllOrders")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedOrders = mutableListOf<Order>()
                for (document in documents) {
                    val order =
                        document.toObject(Order::class.java) // Convert Firestore document to com.example.deeptraderspos.models.Order object
                    fetchedOrders.add(order) // Add order to list
                }
                updateAdapter(fetchedOrders) // Update the adapter with the new data
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // Update the adapter with fetched orders
    private fun updateAdapter(orders: List<Order>) {
        orderAdapter.updateOrderData(orders)
    }
}
