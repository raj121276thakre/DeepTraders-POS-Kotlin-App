package com.example.deeptraderspos.customers

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
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityCustomersBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Customer
import com.example.deeptraderspos.models.Supplier
import com.example.deeptraderspos.suppliers.AddSuppliersActivity
import com.example.deeptraderspos.suppliers.SupplierAdapter
import com.google.firebase.firestore.FirebaseFirestore

class CustomersActivity : InternetCheckActivity() {
    private lateinit var binding: ActivityCustomersBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var customerAdapter: CustomerAdapter
    private val customersList = mutableListOf<Customer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCustomersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.customers)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set status bar color
        Utils.setStatusBarColor(this)

        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@CustomersActivity, AddCustomersActivity::class.java)
            startActivity(intent)
        }


        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        customerAdapter = CustomerAdapter(
            customers = customersList,
            this,
            onDeleteClicked = { customer ->
                deleteSupplier(customer) { success ->
                    if (success) {
                        customerAdapter.removeItem(customer)
                    } else {
                        // Handle error
                    }
                }
            },
            onEditClicked = { customer -> editSupplier(customer) }
        )

        binding.customersRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@CustomersActivity)
            adapter = customerAdapter
        }

        // Fetch suppliers from Firestore
        fetchSuppliersFromFirebase()

        // Implement search functionality
        binding.etxtSupplierSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList =
                    customersList.filter { it.customerName?.contains(s.toString(), true) == true }
                        .toMutableList()

                customerAdapter = CustomerAdapter(
                    customers = filteredList,
                    this@CustomersActivity,
                    onDeleteClicked = { customer ->
                        deleteSupplier(customer) { success ->
                            if (success) {
                                customerAdapter.removeItem(customer)
                            } else {
                                // Handle error
                            }
                        }
                    },
                    onEditClicked = { customer -> editSupplier(customer) }
                )
                binding.customersRecyclerview.adapter = customerAdapter
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })


    }

    private fun fetchSuppliersFromFirebase() {

        firestore.collection("AllCustomers")
            .get()
            .addOnSuccessListener { result ->
                customersList.clear() // Clear the list before adding new items
                for (document in result) {
                    val customer = document.toObject(Customer::class.java)
                    customersList.add(customer)
                }
                customerAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }

    }


    fun deleteSupplier(customer: Customer, callback: (Boolean) -> Unit) {
        val supplierId = customer.id ?: run {
            Toast.makeText(this, "Customer ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("AllCustomers").document(supplierId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Customer deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete supplier: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                callback(false)
            }
    }


    private fun editSupplier(customer: Customer) {
        val intent = Intent(this, AddCustomersActivity::class.java).apply {
            putExtra("customer", customer)
        }
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        setupRecyclerview()
        fetchSuppliersFromFirebase() // Refresh the product list
    }

    private fun setupRecyclerview() {

        // Set up RecyclerView
        customerAdapter = CustomerAdapter(
            customers = customersList,
            this,
            onDeleteClicked = { supplier ->
                deleteSupplier(supplier) { success ->
                    if (success) {
                        customerAdapter.removeItem(supplier)
                    } else {
                        // Handle error
                    }
                }
            },
            onEditClicked = { supplier -> editSupplier(supplier) }
        )

        binding.customersRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@CustomersActivity)
            adapter = customerAdapter
        }

    }


}