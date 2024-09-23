package com.example.deeptraderspos.setting.payment_method

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
import com.example.deeptraderspos.databinding.ActivityPaymentMethodBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.PaymentMethod
import com.google.firebase.firestore.FirebaseFirestore

class PaymentMethodActivity : InternetCheckActivity() {
    private lateinit var binding: ActivityPaymentMethodBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var paymentMethodAdapter: PaymentMethodAdapter
    private val paymentMethodList = mutableListOf<PaymentMethod>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.paymentMethodLayout)) { v, insets ->
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
            val intent = Intent(this@PaymentMethodActivity, AddPaymentMethodActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        paymentMethodAdapter = PaymentMethodAdapter(
            paymentMethods = paymentMethodList,
            this,
            onDeleteClicked = { paymentMethod ->
                deletePaymentMethod(paymentMethod) { success ->
                    if (success) {
                        paymentMethodAdapter.removeItem(paymentMethod)
                    } else {
                        // Handle error
                    }
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PaymentMethodActivity)
            adapter = paymentMethodAdapter
        }

        // Fetch payment methods from Firestore
        fetchPaymentMethodsFromFirebase()

        // Implement search functionality
        binding.etxtPaymentMethodSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList = paymentMethodList.filter { it.paymentMethodName?.contains(s.toString(), true) == true }
                    .toMutableList()

                paymentMethodAdapter = PaymentMethodAdapter(
                    paymentMethods = filteredList,
                    this@PaymentMethodActivity,
                    onDeleteClicked = { paymentMethod ->
                        deletePaymentMethod(paymentMethod) { success ->
                            if (success) {
                                paymentMethodAdapter.removeItem(paymentMethod)
                            } else {
                                // Handle error
                            }
                        }
                    }
                )
                binding.recyclerView.adapter = paymentMethodAdapter
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchPaymentMethodsFromFirebase() {
        firestore.collection("AllPaymentMethods")
            .get()
            .addOnSuccessListener { result ->
                paymentMethodList.clear() // Clear the list before adding new items
                for (document in result) {
                    val paymentMethod = document.toObject(PaymentMethod::class.java)
                    paymentMethodList.add(paymentMethod)
                }
                paymentMethodAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    fun deletePaymentMethod(paymentMethod: PaymentMethod, callback: (Boolean) -> Unit) {
        val paymentMethodId = paymentMethod.id ?: run {
            Toast.makeText(this, "Payment Method ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        firestore.collection("AllPaymentMethods").document(paymentMethodId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Payment Method deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete payment method: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                callback(false)
            }
    }

    override fun onResume() {
        super.onResume()
        fetchPaymentMethodsFromFirebase() // Refresh the payment methods list
    }
}
