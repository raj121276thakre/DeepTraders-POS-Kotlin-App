package com.example.deeptraderspos.pos

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.HomeActivity
import com.example.deeptraderspos.R
import com.example.deeptraderspos.adapter.PosProductAdapter
import com.example.deeptraderspos.adapter.ProductCategoryAdapter
import com.example.deeptraderspos.databinding.ActivityPosBinding
import com.example.deeptraderspos.models.CartItem
import com.example.deeptraderspos.models.Category
import com.example.deeptraderspos.models.Product
import com.example.deeptraderspos.product.AddProductActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class PosActivity : AppCompatActivity() {


    //product added to cart firestore

    private lateinit var binding: ActivityPosBinding
    private lateinit var productAdapter: PosProductAdapter
    private lateinit var categoryAdapter: ProductCategoryAdapter

    private val firestore: FirebaseFirestore = Firebase.firestore
    private var spanCount = 2

    private val productsList = mutableListOf<Product>()
    private val categoryList = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pos)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setHomeButtonEnabled(true) //for back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //for back button
        supportActionBar?.setTitle(R.string.all_product)
        supportActionBar?.hide()

        binding.txtNoProducts.visibility = View.GONE

        // Set up RecyclerViews
        binding.recycler.layoutManager = GridLayoutManager(this, spanCount)
        binding.recycler.setHasFixedSize(true)

        binding.txtReset.setOnClickListener {
            loadProducts()
        }

        binding.categoryRecyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerview.setHasFixedSize(true)

        loadCategories()
        getCartItemCount()

        binding.imgCart.setOnClickListener {
            startActivity(Intent(this, ProductCart::class.java))
        }

        binding.imgBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.etxtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchProducts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        loadProducts()
    }

    private fun loadProducts() {
        firestore.collection("AllProducts").get()
            .addOnSuccessListener { result ->
                productsList.clear() // Clear the list before adding new items
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productsList.add(product)
                }

                if (productsList.isEmpty()) {
                    binding.recycler.visibility = View.GONE
                    binding.imageNoProduct.visibility = View.VISIBLE
                    binding.imageNoProduct.setImageResource(R.drawable.not_found)
                    binding.txtNoProducts.visibility = View.VISIBLE
                } else {
                    binding.recycler.visibility = View.VISIBLE
                    binding.imageNoProduct.visibility = View.GONE
                    binding.txtNoProducts.visibility = View.GONE

                    productAdapter = PosProductAdapter(
                        productsList,
                        this,
                        onAddtocartClick = {product -> addToCartProduct(product)},
                        onEditClicked = { product -> editProduct(product) }
                    )
                    binding.recycler.adapter = productAdapter
                }


                productAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Error", "Error getting products: ", exception)
            }
    }

    private fun addToCartProduct(product: Product) {
        val getStock = product.stock
        val weightUnitId = product.weightUnit
        val productId = product.id
        val productWeight = product.weight
        val productPrice = product.sellPrice
        val productStock = product.stock
        val productName = product.productName

        if (getStock <= 0) {
            Toast.makeText(this, R.string.stock_is_low_please_update_stock, Toast.LENGTH_SHORT).show()
        } else {
            // Check if the product already exists in the cart
            firestore.collection("carts")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        // Product not found, safe to add
                        val cartItem = CartItem(
                            productId = productId,
                            productName = productName,
                            productWeight = productWeight!!,
                            weightUnitId = weightUnitId!!,
                            productPrice = productPrice,
                            quantity = 1,
                            productStock = productStock
                        )

                        if (productId != null) {
                            firestore.collection("carts")
                                .document(productId) // Use productId as the document ID
                                .set(cartItem)
                                .addOnSuccessListener {
                                    Toast.makeText(this, R.string.product_added_to_cart, Toast.LENGTH_SHORT).show()
                                    getCartItemCount() // Update the cart count after adding
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, R.string.product_added_to_cart_failed_try_again, Toast.LENGTH_SHORT).show()
                                    Log.e("Firestore", "Error adding cart item", e)
                                }
                        }
                    } else {
                        // Product already exists in the cart
                        Toast.makeText(this, R.string.product_already_added_to_cart, Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking cart item", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error checking cart item", e)
                }
        }
    }


    private fun getCartItemCount() {
        firestore.collection("carts")
            .get()
            .addOnSuccessListener { result ->
                val count = result.size()
                binding.txtCount.visibility = if (count == 0) View.INVISIBLE else View.VISIBLE
                binding.txtCount.text = count.toString()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting cart item count", e)
            }
    }


    private fun loadCategories() {
        firestore.collection("AllCategories").get()
            .addOnSuccessListener { result ->
                categoryList.clear() // Clear the list before adding new items
                for (document in result) {
                    val category = document.toObject(Category::class.java)
                    categoryList.add(category)
                }


                if (categoryList.isEmpty()) {
                    Toast.makeText(this, R.string.no_data_found, Toast.LENGTH_SHORT).show()
                } else {
                    categoryAdapter = ProductCategoryAdapter(categoryList, this, onClicked = {})
                    binding.categoryRecyclerview.adapter = categoryAdapter
                }

                categoryAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Error", "Error getting categories: ", exception)
            }
    }

    private fun searchProducts(query: String) {
        firestore.collection("AllProducts")
            .whereGreaterThanOrEqualTo("productName", query)
            .whereLessThan("productName", query + '\uf8ff')
            .get()
            .addOnSuccessListener { result ->
                val searchProductList = productsList

                searchProductList.clear() // Clear the list before adding new items
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    searchProductList.add(product)
                }
                if (searchProductList.isEmpty()) {
                    binding.recycler.visibility = View.GONE
                    binding.imageNoProduct.visibility = View.VISIBLE
                    binding.imageNoProduct.setImageResource(R.drawable.not_found)
                    binding.txtNoProducts.visibility = View.VISIBLE
                } else {
                    binding.recycler.visibility = View.VISIBLE
                    binding.imageNoProduct.visibility = View.GONE
                    binding.txtNoProducts.visibility = View.GONE

                    productAdapter = PosProductAdapter(
                        productsList,
                        this,
                        onAddtocartClick = {},
                        onEditClicked = { product -> editProduct(product) })
                    binding.recycler.adapter = productAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Error", "Error searching products: ", exception)
            }
    }

    private fun editProduct(product: Product) {
        val intent = Intent(this, AddProductActivity::class.java).apply {
            putExtra("product", product)
        }
        startActivity(intent)
    }


//    private fun updateCartCount() {
//        // Assume you have a method to get cart item count, update accordingly
//        val count = getCartItemCount()// Replace with your logic to get cart item count
//        binding.txtCount.visibility = if (count == 0) View.INVISIBLE else View.VISIBLE
//        binding.txtCount.text = count.toString()
//    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_cart_button -> {
                startActivity(Intent(this, ProductCart::class.java))
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
       getCartItemCount()
    }
}
