package com.example.deeptraderspos.product

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
import com.example.deeptraderspos.databinding.ActivityExpenseBinding
import com.example.deeptraderspos.databinding.ActivityProductBinding
import com.example.deeptraderspos.models.Product
import com.google.firebase.firestore.FirebaseFirestore

class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var productAdapter: ProductAdapter
    private val productsList = mutableListOf<Product>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding =ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.products)) { v, insets ->
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

        binding.fabAddProduct.setOnClickListener {
            val intent = Intent(this@ProductActivity, AddProductActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        setupRecyclerView()

        // Fetch products from Firestore
        fetchProductsFromFirebase()

        // Implement search functionality
        binding.etxtProductSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList =
                    productsList.filter { it.productName.contains(s.toString(), true) }
                        .toMutableList()

                productAdapter = ProductAdapter(
                    products = filteredList,
                    this@ProductActivity,
                    onDeleteClicked = { product ->
                        deleteProduct(product) { success ->
                            if (success) {
                                productAdapter.removeItem(product)
                            } else {
                                // Handle error
                            }
                        }
                    },
                    onEditClicked = { product -> editProduct(product) }
                )
                binding.productsRecyclerview.adapter = productAdapter
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })

    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = productsList,
            this,
            onDeleteClicked = { product ->
                deleteProduct(product) { success ->
                    if (success) {
                        productAdapter.removeItem(product)
                    } else {
                        // Handle error
                    }
                }
            },
            onEditClicked = { product -> editProduct(product) }
        )

        binding.productsRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@ProductActivity)
            adapter = productAdapter
        }
    }

    private fun fetchProductsFromFirebase() {
        firestore.collection("AllProducts")
            .get()
            .addOnSuccessListener { result ->
                productsList.clear() // Clear the list before adding new items
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productsList.add(product)
                }
                productAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching products: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProduct(product: Product, callback: (Boolean) -> Unit) {
        val productId = product.id ?: run {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        firestore.collection("AllProducts").document(productId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete product: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun editProduct(product: Product) {
        val intent = Intent(this, AddProductActivity::class.java).apply {
            putExtra("product", product)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        fetchProductsFromFirebase() // Refresh the products list
    }



}