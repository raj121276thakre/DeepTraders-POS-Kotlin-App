package com.example.deeptraderspos.orders.customerOrders

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityOrdersBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Customer
import com.example.deeptraderspos.models.CustomerWithOrders
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.orders.OrderAdapter
import com.example.deeptraderspos.pos.PosActivity
import com.google.firebase.firestore.FirebaseFirestore

class OrdersActivity : InternetCheckActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var orderAdapter: OrderAdapter
    private val customerWithOrdersList = mutableListOf<CustomerWithOrders>() // Holds customer with their orders

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

        // Set status bar color
        Utils.setStatusBarColor(this)

        firestore = FirebaseFirestore.getInstance()

        // Go Back Button
        binding.goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

        // Go to POS Activity Button
        binding.gotoPosBtn.setOnClickListener {
            val intent = Intent(this, PosActivity::class.java)  // Replace with your POS Activity class name
            startActivity(intent)
        }

        // Setup RecyclerView and Adapter
        setupRecyclerView()
        // Fetch customers and their orders from Firestore
        fetchCustomersWithOrders()
    }

    // Setup RecyclerView and attach Adapter
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(this, customerWithOrdersList,false )
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.ordersRecyclerView.adapter = orderAdapter
    }

    // Fetch all customers and their orders from Firestore
    private fun fetchCustomersWithOrders() {
        showProgressBar("Fetching customers and their orders...")

        firestore.collection("AllCustomers").get()
            .addOnSuccessListener { customerDocuments ->
                val customers = customerDocuments.toObjects(Customer::class.java)
                customerWithOrdersList.clear()

                // For each customer, fetch their orders from AllOrders collection
                for (customer in customers) {
                    fetchOrdersForCustomer(customer)
                }
            }
            .addOnFailureListener { e ->
                hideProgressBar()
                Toast.makeText(this, "Error fetching customers: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch orders for a specific customer
    private fun fetchOrdersForCustomer(customer: Customer) {
        firestore.collection("AllOrders")
            .whereEqualTo("customerName", customer.customerName) // Assuming `customerId` is the matching field
            .get()
            .addOnSuccessListener { orderDocuments ->
                val orders = orderDocuments.toObjects(Order::class.java)
                val customerWithOrders = CustomerWithOrders(customer, orders)

                // Add customer with orders to the list
                customerWithOrdersList.add(customerWithOrders)

                // Notify adapter about data change
                orderAdapter.updateEntityWithOrdersData(customerWithOrdersList)
                hideProgressBar()
            }
            .addOnFailureListener { e ->
                hideProgressBar()
                Toast.makeText(this, "Error fetching orders for ${customer.customerName}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
