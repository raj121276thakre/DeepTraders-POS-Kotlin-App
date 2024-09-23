package com.example.deeptraderspos.setting.order_type

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityAddOrderTypeBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.OrderType
import com.google.firebase.firestore.FirebaseFirestore

class AddOrderTypeActivity : InternetCheckActivity() {
    private lateinit var binding: ActivityAddOrderTypeBinding

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddOrderTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addOrderType)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()

        // Handle add order type button click
        binding.txtAddOrderType.setOnClickListener {
            saveOrderTypeData()
        }
    }

    private fun saveOrderTypeData() {
        // Collect order type data from the input field
        val orderTypeName = binding.etxtOrderTypeName.text.toString().trim()

        // Validation check
        if (orderTypeName.isEmpty()) {
            Toast.makeText(this, "Please fill in the order type name", Toast.LENGTH_SHORT).show()
            return
        }

        // Create an order type object
        val orderType = OrderType(
            orderTypeName = orderTypeName
        )

        // Save to Firestore
        db.collection("AllOrderTypes")
            .add(orderType)
            .addOnSuccessListener { documentReference ->
                // Retrieve the generated document ID
                val documentId = documentReference.id

                // Create a new order type object with the ID
                val orderTypeWithId = orderType.copy(id = documentId)

                // Update the Firestore document with the new order type object containing the ID
                db.collection("AllOrderTypes").document(documentId)
                    .set(orderTypeWithId)
                    .addOnSuccessListener {
                        Toast.makeText(this, "com.example.deeptraderspos.models.Order type added successfully with ID", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after successful save
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update order type: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add order type: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
