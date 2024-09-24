package com.example.deeptraderspos.setting.payment_method

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityAddPaymentMethodBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.PaymentMethod
import com.google.firebase.firestore.FirebaseFirestore

class AddPaymentMethodActivity : InternetCheckActivity() {
    private lateinit var binding: ActivityAddPaymentMethodBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addPaymentMethod)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()

        // Handle add payment method button click
        binding.txtAddPaymentMethod.setOnClickListener {
            savePaymentMethodData()
        }
    }

    private fun savePaymentMethodData() {
        showProgressBar("Saving Payment Information...")
        // Collect payment method data from the input field
        val methodName = binding.etxtPaymentMethodName.text.toString().trim()

        // Validation check
        if (methodName.isEmpty()) {
            hideProgressBar()
            Toast.makeText(this, "Please fill in the payment method name", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a payment method object
        val paymentMethod = PaymentMethod(
            paymentMethodName = methodName
        )

        // Save to Firestore
        db.collection("AllPaymentMethods")
            .add(paymentMethod)
            .addOnSuccessListener { documentReference ->
                hideProgressBar()
                // Retrieve the generated document ID
                val documentId = documentReference.id

                // Create a new payment method object with the ID
                val paymentMethodWithId = paymentMethod.copy(id = documentId)

                // Update the Firestore document with the new payment method object containing the ID
                db.collection("AllPaymentMethods").document(documentId)
                    .set(paymentMethodWithId)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Payment method added successfully with ID",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Close the activity after successful save
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to update payment method: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                hideProgressBar()
                Toast.makeText(this, "Failed to add payment method: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
