package com.example.deeptraderspos.setting.unit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.ActivityAddUnitBinding
import com.example.deeptraderspos.models.Units
import com.google.firebase.firestore.FirebaseFirestore

class AddUnitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddUnitBinding

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddUnitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addUnit)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = FirebaseFirestore.getInstance()

        // Handle add unit button click
        binding.txtAddUnit.setOnClickListener {
            saveUnitData()
        }
    }

    private fun saveUnitData() {
        // Collect unit data from the input field
        val unitName = binding.etxtUnitName.text.toString().trim()

        // Validation check
        if (unitName.isEmpty()) {
            Toast.makeText(this, "Please fill in the unit name", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a unit object
        val unit = Units(
            unitName = unitName
        )

        // Save to Firestore
        db.collection("AllUnits")
            .add(unit)
            .addOnSuccessListener { documentReference ->
                // Retrieve the generated document ID
                val documentId = documentReference.id

                // Create a new unit object with the ID
                val unitWithId = unit.copy(id = documentId)

                // Update the Firestore document with the new unit object containing the ID
                db.collection("AllUnits").document(documentId)
                    .set(unitWithId)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Unit added successfully with ID", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after successful save
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update Unit: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add Unit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
