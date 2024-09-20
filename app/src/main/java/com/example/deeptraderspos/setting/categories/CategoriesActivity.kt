package com.example.deeptraderspos.setting.categories

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
import com.example.deeptraderspos.databinding.ActivityCategoriesBinding
import com.example.deeptraderspos.databinding.ActivityExpenseBinding
import com.example.deeptraderspos.expense.AddExpenseActivity
import com.example.deeptraderspos.expense.ExpenseAdapter
import com.example.deeptraderspos.models.Category
import com.example.deeptraderspos.models.Expense
import com.google.firebase.firestore.FirebaseFirestore

class CategoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriesBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryList = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.category)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@CategoriesActivity, AddCategoryActivity::class.java)
            startActivity(intent)
        }


        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        categoryAdapter = CategoryAdapter(
            categories = categoryList,
            this,
            onDeleteClicked = { category ->
                deleteCategory(category) { success ->
                    if (success) {
                        categoryAdapter.removeItem(category)
                    } else {
                        // Handle error
                    }
                }
            },

        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoriesActivity)
            adapter = categoryAdapter
        }

        // Fetch categories from Firestore
        fetchCategoriesFromFirebase()

        // Implement search functionality
        binding.etxtCategoriesSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList =
                    categoryList.filter { it.categoryName?.contains(s.toString(), true) == true }
                        .toMutableList()

                categoryAdapter = CategoryAdapter(
                    categories = filteredList,
                    this@CategoriesActivity,
                    onDeleteClicked = { category ->
                        deleteCategory(category) { success ->
                            if (success) {
                                categoryAdapter.removeItem(category)
                            } else {
                                // Handle error
                            }
                        }
                    },

                )
                binding.recyclerView.adapter = categoryAdapter
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchCategoriesFromFirebase() {
        firestore.collection("AllCategories")
            .get()
            .addOnSuccessListener { result ->
                categoryList.clear() // Clear the list before adding new items
                for (document in result) {
                    val category = document.toObject(Category::class.java)
                    categoryList.add(category)
                }
                categoryAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    fun deleteCategory(category: Category, callback: (Boolean) -> Unit) {
        val categoryId = category.id ?: run {
            Toast.makeText(this, "Category ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("AllCategories").document(categoryId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete category: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                callback(false)
            }
    }


    override fun onResume() {
        super.onResume()
        setupRecyclerview()
        fetchCategoriesFromFirebase() // Refresh the categories list
    }

    private fun setupRecyclerview() {
        // Set up RecyclerView
        categoryAdapter = CategoryAdapter(
            categories = categoryList,
            this,
            onDeleteClicked = { category ->
                deleteCategory(category) { success ->
                    if (success) {
                        categoryAdapter.removeItem(category)
                    } else {
                        // Handle error
                    }
                }
            },

        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoriesActivity)
            adapter = categoryAdapter
        }
    }




}