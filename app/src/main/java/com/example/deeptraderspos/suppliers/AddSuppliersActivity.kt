package com.example.deeptraderspos.suppliers

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityAddSuppliersBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Supplier
import com.google.firebase.firestore.FirebaseFirestore

class AddSuppliersActivity : InternetCheckActivity() {
    private lateinit var binding: ActivityAddSuppliersBinding

    private var supplier: Supplier? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddSuppliersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.AddSupplierScreen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()

        supplier = intent.getParcelableExtra("supplier")
        if (supplier != null) {
            showSupplierDetails(supplier!!)
            // Make etxtSupplierCell non-editable
            binding.etxtSupplierCell.isFocusable = false
            binding.etxtSupplierCell.isFocusableInTouchMode = false
            binding.etxtSupplierCell.isCursorVisible = false
        }


        // Handle add supplier button click
        binding.txtAddSupplier.setOnClickListener {

            if (supplier == null) {
                saveSupplierData()
            } else {
                updateSupplier()
            }
        }

    }

    private fun showSupplierDetails(supplier: Supplier) {
        binding.etxtSupplierName.setText(supplier.supplierName)
        binding.etxtSupplierCell.setText(supplier.supplierPhone)
        binding.etxtSupplierEmail.setText(supplier.supplierEmail)
        binding.etxtSupplierAddress.setText(supplier.supplierAddress)

        binding.txtAddSupplier.text = "Update Supplier"


    }


    private fun saveSupplierData() {
        showProgressBar("Saving Supplier information...")
        // Collect supplier data from input fields
        val supplierName = binding.etxtSupplierName.text.toString().trim()
        val supplierPhone = binding.etxtSupplierCell.text.toString().trim()
        val supplierEmail = binding.etxtSupplierEmail.text.toString().trim()
        val supplierAddress = binding.etxtSupplierAddress.text.toString().trim()

        // Validation check
        if (supplierName.isEmpty() || supplierPhone.isEmpty()) {
            hideProgressBar()
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if a supplier with the same phone number already exists
        db.collection("AllSuppliers")
            .whereEqualTo("supplierPhone", supplierPhone)
            .get()
            .addOnSuccessListener { querySnapshot ->
                hideProgressBar()
                if (!querySnapshot.isEmpty) {
                    // Supplier with the same phone number already exists
                    Toast.makeText(this, "Supplier with this phone number already exists.", Toast.LENGTH_SHORT).show()
                } else {
                    // Create a supplier object
                    val supplier = Supplier(
                        supplierName = supplierName,
                        supplierPhone = supplierPhone,
                        supplierEmail = if (supplierEmail.isNotEmpty()) supplierEmail else null,
                        supplierAddress = if (supplierAddress.isNotEmpty()) supplierAddress else null
                    )

                    // Save to Firestore
                    db.collection("AllSuppliers")
                        .add(supplier)
                        .addOnSuccessListener { documentReference ->
                            val documentId = documentReference.id
                            val supplierWithId = supplier.copy(id = documentId)

                            db.collection("AllSuppliers").document(documentId)
                                .set(supplierWithId)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Supplier added successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish() // Close the activity after successful save
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Failed to update supplier: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add supplier: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
            .addOnFailureListener { e ->
                hideProgressBar()
                Toast.makeText(this, "Error checking for existing supplier: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    /*
    private fun saveSupplierData() {
        // Collect supplier data from input fields
        val supplierName = binding.etxtSupplierName.text.toString().trim()
        val supplierPhone = binding.etxtSupplierCell.text.toString().trim()
        val supplierEmail = binding.etxtSupplierEmail.text.toString().trim()
        val supplierAddress = binding.etxtSupplierAddress.text.toString().trim()

        // Validation check
        if (supplierName.isEmpty() || supplierPhone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a supplier object
        val supplier = (if (supplierEmail.isNotEmpty()) supplierEmail else null)?.let {
            (if (supplierAddress.isNotEmpty()) supplierAddress else null)?.let { it1 ->
                Supplier(
                    supplierName = supplierName,
                    supplierPhone = supplierPhone,
                    supplierEmail = it,
                    supplierAddress = it1
                )
            }
        }

        // Save to Firestore
        if (supplier != null) {
            db.collection("AllSuppliers")
                .add(supplier)
                .addOnSuccessListener { documentReference ->
                    // Retrieve the generated document ID
                    val documentId = documentReference.id

                    // Create a new supplier object with the ID
                    val supplierWithId = supplier.copy(id = documentId)

                    // Update the Firestore document with the new supplier object containing the ID
                    db.collection("AllSuppliers").document(documentId)
                        .set(supplierWithId)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Supplier added successfully with ID",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish() // Close the activity after successful save
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to update supplier: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add supplier: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

     */



    private fun updateSupplier() {
        // Collect supplier data from input fields
        showProgressBar("Updating Supplier information...")

        val supplierName = binding.etxtSupplierName.text.toString().trim()
        val supplierPhone = binding.etxtSupplierCell.text.toString().trim()
        val supplierEmail = binding.etxtSupplierEmail.text.toString().trim()
        val supplierAddress = binding.etxtSupplierAddress.text.toString().trim()

        // Validation check
        if (supplierName.isEmpty() || supplierPhone.isEmpty()) {
            hideProgressBar()
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Update the supplier object with new data
        supplier?.let {
            val updatedSupplier = it.copy(
                supplierName = supplierName,
                supplierPhone = supplierPhone,
                supplierEmail = if (supplierEmail.isNotEmpty()) supplierEmail else null,
                supplierAddress = if (supplierAddress.isNotEmpty()) supplierAddress else null
            )

            // Update Firestore document with the new supplier data
            updatedSupplier.id?.let { it1 ->
                db.collection("AllSuppliers")
                    .document(it1)  // Use the existing supplier ID
                    .set(updatedSupplier)
                    .addOnSuccessListener {
                        hideProgressBar()
                        Toast.makeText(this, "Supplier updated successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after successful update
                    }
                    .addOnFailureListener { e ->
                        hideProgressBar()
                        Toast.makeText(this, "Failed to update supplier: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }



}


