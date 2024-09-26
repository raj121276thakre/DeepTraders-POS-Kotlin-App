package com.example.deeptraderspos.orders.supplierOrders

import android.content.Intent
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
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityOrdersSupplierBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.CustomerWithOrders
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.Supplier
import com.example.deeptraderspos.models.SupplierWithOrders
import com.example.deeptraderspos.orders.OrderAdapter
import com.example.deeptraderspos.pos.PosActivity
import com.google.firebase.firestore.FirebaseFirestore

class OrdersSupplierActivity : InternetCheckActivity() {


    private lateinit var firestore: FirebaseFirestore
    private lateinit var orderAdapter: OrderAdapter
    private val supplierWithOrdersList = mutableListOf<SupplierWithOrders>()
    private lateinit var binding: ActivityOrdersSupplierBinding // ViewBinding



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

        // Set status bar color
        Utils.setStatusBarColor(this)

        firestore = FirebaseFirestore.getInstance()

        // Go Back Button
        binding.goBackBtn.setOnClickListener {
            onBackPressed()
        }

        // Go to POS Activity Button
        binding.gotoPosBtn.setOnClickListener {
            val intent = Intent(this, PosActivity::class.java)  // Replace with your POS Activity class name
            startActivity(intent)
        }

        // Setup RecyclerView and Adapter
        setupRecyclerView()
        // Fetch suppliers and their orders from Firestore
        fetchSuppliersWithOrders()

    }

    // Setup RecyclerView and attach Adapter
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(this, supplierWithOrdersList, true) // Pass true to indicate it's for suppliers
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.ordersRecyclerView.adapter = orderAdapter
    }

    // Fetch all suppliers and their orders from Firestore
    private fun fetchSuppliersWithOrders() {
        showProgressBar("Fetching suppliers and their orders...")

        firestore.collection("AllSuppliers").get() // Fetch from 'AllSuppliers' collection
            .addOnSuccessListener { supplierDocuments ->
                val suppliers = supplierDocuments.toObjects(Supplier::class.java)
                supplierWithOrdersList.clear()

                // For each supplier, fetch their orders from AllOrders collection
                for (supplier in suppliers) {
                    fetchOrdersForSupplier(supplier)
                }

            }
            .addOnFailureListener { e ->
                hideProgressBar()
                Toast.makeText(this, "Error fetching suppliers: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    // Fetch orders for a specific supplier
    private fun fetchOrdersForSupplier(supplier: Supplier) {
        firestore.collection("AllOrdersSuppliers")
            .whereEqualTo("supplierName", supplier.supplierName) // Assuming `supplierName` is the matching field
            .get()
            .addOnSuccessListener { orderDocuments ->
                val orders = orderDocuments.toObjects(Order::class.java)
                val supplierWithOrders = SupplierWithOrders(supplier, orders)

                // Add supplier with orders to the list only if they have orders
                if (orders.isNotEmpty()) {
                    supplierWithOrdersList.add(supplierWithOrders)
                }

                // Notify adapter about data change
                orderAdapter.updateEntityWithOrdersData(supplierWithOrdersList)
                hideProgressBar()
            }
            .addOnFailureListener { e ->
                hideProgressBar()
                Toast.makeText(this, "Error fetching orders for ${supplier.supplierName}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
