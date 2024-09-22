package com.example.deeptraderspos.suppliers

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
import com.example.deeptraderspos.databinding.ActivitySuppliersBinding
import com.example.deeptraderspos.models.Supplier
import com.google.firebase.firestore.FirebaseFirestore

class SuppliersActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuppliersBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var supplierAdapter: SupplierAdapter
    private val suppliersList = mutableListOf<Supplier>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySuppliersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.supplier)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

       binding.fabAdd.setOnClickListener {
           val intent = Intent(this@SuppliersActivity, AddSuppliersActivity::class.java)
           startActivity(intent)
       }


        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        supplierAdapter = SupplierAdapter(
            suppliers = suppliersList,
           this,
            onDeleteClicked = { supplier ->
                deleteSupplier(supplier) { success ->
                    if (success) {
                        supplierAdapter.removeItem(supplier)
                    } else {
                        // Handle error
                    }
                }
            },
            onEditClicked = { supplier -> editSupplier(supplier) }
        )

        binding.suppliersRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@SuppliersActivity)
            adapter = supplierAdapter
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
                    suppliersList.filter { it.supplierName?.contains(s.toString(), true) == true }.toMutableList()

                supplierAdapter = SupplierAdapter(
                    suppliers = filteredList,
                    this@SuppliersActivity,
                    onDeleteClicked = { supplier ->
                        deleteSupplier(supplier) { success ->
                            if (success) {
                                supplierAdapter.removeItem(supplier)
                            } else {
                                // Handle error
                            }
                        }
                    },
                    onEditClicked = { supplier -> editSupplier(supplier) }
                )
                binding.suppliersRecyclerview.adapter = supplierAdapter
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })





    }

    private fun fetchSuppliersFromFirebase() {

        firestore.collection("AllSuppliers")
            .get()
            .addOnSuccessListener { result ->
                suppliersList.clear() // Clear the list before adding new items
                for (document in result) {
                    val supplier = document.toObject(Supplier::class.java)
                    suppliersList.add(supplier)
                }
                supplierAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }

    }





    fun deleteSupplier(supplier: Supplier, callback: (Boolean) -> Unit) {
        val supplierId = supplier.id ?: run {
            Toast.makeText(this, "Supplier ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("AllSuppliers").document(supplierId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Supplier deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete supplier: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }


    private fun editSupplier(supplier: Supplier) {
        val intent = Intent(this, AddSuppliersActivity::class.java).apply {
            putExtra("supplier", supplier)
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
        supplierAdapter = SupplierAdapter(
            suppliers = suppliersList,
            this,
            onDeleteClicked = { supplier ->
                deleteSupplier(supplier) { success ->
                    if (success) {
                        supplierAdapter.removeItem(supplier)
                    } else {
                        // Handle error
                    }
                }
            },
            onEditClicked = { supplier -> editSupplier(supplier) }
        )

        binding.suppliersRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@SuppliersActivity)
            adapter = supplierAdapter
        }

    }


}