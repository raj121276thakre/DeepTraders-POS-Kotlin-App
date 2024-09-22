package com.example.deeptraderspos.setting.categories

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityAddCategoryBinding
import com.example.deeptraderspos.databinding.ActivityAddCustomersBinding
import com.example.deeptraderspos.databinding.ActivityCategoriesBinding
import com.example.deeptraderspos.models.Category
import com.example.deeptraderspos.models.Customer
import com.google.firebase.firestore.FirebaseFirestore

class AddCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addCategories)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()
        // Handle add supplier button click
        binding.txtAddCategory.setOnClickListener {

            saveCategoryData()

        }

    }


    private fun saveCategoryData() {
        // Collect category data from the input field
        val categoryName = binding.etxtCategoryName.text.toString().trim()

        // Validation check
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Please fill in the category name", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a category object
        val category = Category(
            categoryName = categoryName
        )

        // Save to Firestore
        db.collection("AllCategories")
            .add(category)
            .addOnSuccessListener { documentReference ->
                // Retrieve the generated document ID
                val documentId = documentReference.id

                // Create a new category object with the ID
                val categoryWithId = category.copy(id = documentId)

                // Update the Firestore document with the new category object containing the ID
                db.collection("AllCategories").document(documentId)
                    .set(categoryWithId)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Category added successfully with ID",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Close the activity after successful save
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to update Category: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add Category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



}