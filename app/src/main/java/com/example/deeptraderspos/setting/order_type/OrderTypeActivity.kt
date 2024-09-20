package com.example.deeptraderspos.setting.order_type

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.ActivityOrderTypeBinding
import com.example.deeptraderspos.models.OrderType
import com.google.firebase.firestore.FirebaseFirestore

class OrderTypeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderTypeBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var orderTypeAdapter: OrderTypeAdapter
    private val orderTypeList = mutableListOf<OrderType>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.order_type)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@OrderTypeActivity, AddOrderTypeActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        orderTypeAdapter = OrderTypeAdapter(
            orderTypes = orderTypeList,
            this,
            onDeleteClicked = { orderType ->
                deleteOrderType(orderType) { success ->
                    if (success) {
                        orderTypeAdapter.removeItem(orderType)
                    } else {
                        // Handle error
                    }
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderTypeActivity)
            adapter = orderTypeAdapter
        }

        // Fetch order types from Firestore
        fetchOrderTypesFromFirebase()

        // Implement search functionality
        binding.etxtOrderTypeSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList = orderTypeList.filter { it.orderTypeName?.contains(s.toString(), true) == true }
                    .toMutableList()

                orderTypeAdapter = OrderTypeAdapter(
                    orderTypes = filteredList,
                    this@OrderTypeActivity,
                    onDeleteClicked = { orderType ->
                        deleteOrderType(orderType) { success ->
                            if (success) {
                                orderTypeAdapter.removeItem(orderType)
                            } else {
                                // Handle error
                            }
                        }
                    }
                )
                binding.recyclerView.adapter = orderTypeAdapter
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchOrderTypesFromFirebase() {
        firestore.collection("AllOrderTypes")
            .get()
            .addOnSuccessListener { result ->
                orderTypeList.clear() // Clear the list before adding new items
                for (document in result) {
                    val orderType = document.toObject(OrderType::class.java)
                    orderTypeList.add(orderType)
                }
                orderTypeAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    fun deleteOrderType(orderType: OrderType, callback: (Boolean) -> Unit) {
        val orderTypeId = orderType.id ?: run {
            Toast.makeText(this, "Order Type ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        firestore.collection("AllOrderTypes").document(orderTypeId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Order Type deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete Order Type: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                callback(false)
            }
    }

    override fun onResume() {
        super.onResume()
        fetchOrderTypesFromFirebase() // Refresh the order types list
    }
}
