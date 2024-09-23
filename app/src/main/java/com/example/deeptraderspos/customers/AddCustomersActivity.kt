package com.example.deeptraderspos.customers

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityAddCustomersBinding
import com.example.deeptraderspos.databinding.ActivityCustomersBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Customer
import com.example.deeptraderspos.models.Supplier
import com.google.firebase.firestore.FirebaseFirestore

class AddCustomersActivity : InternetCheckActivity() {
    private lateinit var binding: ActivityAddCustomersBinding

    private var customer: Customer? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddCustomersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.AddCustomerScreen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()

        customer = intent.getParcelableExtra("customer")
        if (customer != null) {
            showSupplierDetails(customer!!)
            // Make etxtSupplierCell non-editable
            binding.etxtCustomerCell.isFocusable = false
            binding.etxtCustomerCell.isFocusableInTouchMode = false
            binding.etxtCustomerCell.isCursorVisible = false
        }



        // Handle add supplier button click
        binding.txtAddCustomer.setOnClickListener {

            if (customer == null) {
                saveSupplierData()
            } else {
                updateSupplier()
            }
        }

    }



    private fun showSupplierDetails(customer: Customer) {
        binding.etxtCustomerName.setText(customer.customerName)
        binding.etxtCustomerCell.setText(customer.customerPhone)
        binding.etxtCustomerEmail.setText(customer.customerEmail)
        binding.etxtCustomerAddress.setText(customer.customerAddress)

        binding.txtAddCustomer.text = "Update Customer"


    }

    private fun saveSupplierData() {
        // Collect customer data from input fields
        val customerName = binding.etxtCustomerName.text.toString().trim()
        val customerPhone = binding.etxtCustomerCell.text.toString().trim()
        val customerEmail = binding.etxtCustomerEmail.text.toString().trim()
        val customerAddress = binding.etxtCustomerAddress.text.toString().trim()

        // Validation check
        if (customerName.isEmpty() || customerPhone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if a customer with the same phone number or email already exists
        db.collection("AllCustomers")
            .whereEqualTo("customerPhone", customerPhone)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Customer with the same phone number already exists
                    Toast.makeText(this, "Customer with this phone number already exists.", Toast.LENGTH_SHORT).show()
                } else {
                    // Create a customer object
                    val customer = Customer(
                        customerName = customerName,
                        customerPhone = customerPhone,
                        customerEmail = if (customerEmail.isNotEmpty()) customerEmail else null,
                        customerAddress = if (customerAddress.isNotEmpty()) customerAddress else null
                    )

                    // Save to Firestore
                    db.collection("AllCustomers")
                        .add(customer)
                        .addOnSuccessListener { documentReference ->
                            val documentId = documentReference.id
                            val customerWithId = customer.copy(id = documentId)

                            db.collection("AllCustomers").document(documentId)
                                .set(customerWithId)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Customer added successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish() // Close the activity after successful save
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Failed to update Customer: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add Customer: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking for existing customer: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

/*
    private fun saveSupplierData() {
        // Collect supplier data from input fields
        val customerName = binding.etxtCustomerName.text.toString().trim()
        val customerPhone = binding.etxtCustomerCell.text.toString().trim()
        val customerEmail = binding.etxtCustomerEmail.text.toString().trim()
        val customerAddress = binding.etxtCustomerAddress.text.toString().trim()

        // Validation check
        if (customerName.isEmpty() || customerPhone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a supplier object
        val customer = (if (customerEmail.isNotEmpty()) customerEmail else null)?.let {
            (if (customerAddress.isNotEmpty()) customerAddress else null)?.let { it1 ->
                Customer(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerEmail = it,
                    customerAddress = it1
                )
            }
        }

        // Save to Firestore
        if (customer != null) {
            db.collection("AllCustomers")
                .add(customer)
                .addOnSuccessListener { documentReference ->
                    // Retrieve the generated document ID
                    val documentId = documentReference.id

                    // Create a new supplier object with the ID
                    val supplierWithId = customer.copy(id = documentId)

                    // Update the Firestore document with the new supplier object containing the ID
                    db.collection("AllCustomers").document(documentId)
                        .set(supplierWithId)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Customer added successfully with ID",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish() // Close the activity after successful save
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to update Customer: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add Customer: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

 */



    private fun updateSupplier() {
        // Collect supplier data from input fields
        val customerName = binding.etxtCustomerName.text.toString().trim()
        val customerPhone = binding.etxtCustomerCell.text.toString().trim()
        val customerEmail = binding.etxtCustomerEmail.text.toString().trim()
        val customerAddress = binding.etxtCustomerAddress.text.toString().trim()


        // Validation check
        if (customerName.isEmpty() || customerPhone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }



        // Update the supplier object with new data
        customer?.let {
            val updatedCustomer = it.copy(
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = if (customerEmail.isNotEmpty()) customerEmail else null,
                customerAddress = if (customerAddress.isNotEmpty()) customerAddress else null
            )

            // Update Firestore document with the new supplier data
            updatedCustomer.id?.let { it1 ->
                db.collection("AllCustomers")
                    .document(it1)  // Use the existing supplier ID
                    .set(updatedCustomer)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Customer updated successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after successful update
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update customer: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }



}
