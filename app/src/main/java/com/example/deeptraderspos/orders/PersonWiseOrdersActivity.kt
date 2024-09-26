package com.example.deeptraderspos.orders

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
import com.google.firebase.firestore.FirebaseFirestore

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

//        val name = if (isSupplier) {
//            order.supplierName
//        } else {
//            order.customerName
//        }


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

        }

    }

    private fun setToolBarTitle(name: String) {
        if (isSupplier) {
            binding.toolbarTitle.text = getString(R.string.suppliers_wise_orders) + " " + name
        } else {
            binding.toolbarTitle.text = getString(R.string.customers_wise_orders)  + " " + name
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
                // Reverse the order to show the latest orders at the top
                filteredOrdersList.reverse()
                // Update the adapter with the filtered list
                updateAdapter(filteredOrdersList)

                // Show a message if no orders were found
                if (filteredOrdersList.isEmpty()) {
                    Toast.makeText(this, "No orders found for $name", Toast.LENGTH_SHORT)
                        .show()
                }
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