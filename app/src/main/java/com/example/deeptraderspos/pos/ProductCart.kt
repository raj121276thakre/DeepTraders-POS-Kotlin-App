package com.example.deeptraderspos.pos

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.adapter.CartAdapter
import com.example.deeptraderspos.databinding.ActivityProductCartBinding
import com.example.deeptraderspos.databinding.DialogPaymentBinding
import com.example.deeptraderspos.models.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductCart : AppCompatActivity() {

    private lateinit var binding: ActivityProductCartBinding

    private lateinit var productCartAdapter: CartAdapter
    private lateinit var f: DecimalFormat
    private lateinit var firestore: FirebaseFirestore
    private var customerNames: MutableList<String> = ArrayList()
    private var orderTypeNames: MutableList<String> = ArrayList()
    private var paymentMethodNames: MutableList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cart)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setHomeButtonEnabled(true) // for back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // for back button
        supportActionBar?.title = getString(R.string.product_cart)

        f = DecimalFormat("#0.00")

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        val linearLayoutManager = LinearLayoutManager(this)
        binding.cartRecyclerview.layoutManager = linearLayoutManager
        binding.cartRecyclerview.setHasFixedSize(true)

        // Fetch cart products from Firestore
        fetchCartProducts()

        binding.btnSubmitOrder.setOnClickListener { dialog() }


    }


    private fun fetchCartProducts() {
        firestore.collection("carts")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Handle empty cart UI
                    binding.imageNoProduct.setImageResource(R.drawable.empty_cart)
                    binding.imageNoProduct.visibility = View.VISIBLE
                    binding.txtNoProduct.visibility = View.VISIBLE
                    binding.btnSubmitOrder.visibility = View.GONE
                    binding.cartRecyclerview.visibility = View.GONE
                    binding.linearLayout.visibility = View.GONE
                    binding.txtTotalPrice.visibility = View.GONE
                } else {
                    // Hide "No Product" UI when products exist
                    binding.imageNoProduct.visibility = View.GONE

                    // Create a list to hold CartItem objects
                    val cartProductList = ArrayList<CartItem>()

                    // Loop through Firestore documents and map them to CartItem objects
                    for (document in documents) {

                        val cartItem = CartItem(
                            productId = document.getString("productId") ?: "",
                            productName = document.getString("productName") ?: "",
                            productWeight = document.getDouble("productWeight") ?: 0.0,
                            weightUnitId = document.getString("weightUnitId") ?: "",
                            productPrice = document.getDouble("productPrice") ?: 0.0,
                            quantity = document.getLong("quantity")?.toInt() ?: 1,
                            productStock = document.getLong("productStock")?.toInt() ?: 0
                        )
                        cartProductList.add(cartItem)
                    }

                    // Set up the adapter with the list of CartItem objects
                    productCartAdapter = CartAdapter(
                        this, cartProductList,
                        binding.txtTotalPrice, binding.btnSubmitOrder,
                        binding.imageNoProduct, binding.txtNoProduct
                    )
                    binding.cartRecyclerview.adapter = productCartAdapter
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading cart: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun proceedOrder(type: String, paymentMethod: String, customerName: String, calculatedTax: Double, discount: String) {
        val itemCount = productCartAdapter.itemCount
        if (itemCount > 0) {
            val orderMap = HashMap<String, Any>()
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
            val currentTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())

            orderMap["order_date"] = currentDate
            orderMap["order_time"] = currentTime
            orderMap["order_type"] = type
            orderMap["order_payment_method"] = paymentMethod
            orderMap["customer_name"] = customerName
            orderMap["tax"] = calculatedTax
            orderMap["discount"] = discount

            val productsArray = ArrayList<HashMap<String, Any>>()
            for (i in 0 until itemCount) {
                val cartItem = productCartAdapter.getItem(i)
                val productMap = HashMap<String, Any>().apply {
                    cartItem.productId?.let { put("product_id", it) }
                    cartItem.productName?.let { put("product_name", it) } // Ensure you have this property in your CartItem
                    put("product_weight", cartItem.productWeight)
                    put("product_qty", cartItem.quantity)
                    put("product_price", cartItem.productPrice)
                }
                productsArray.add(productMap)
            }
            orderMap["products"] = productsArray

            // Save order to Firestore
            firestore.collection("orders")
                .add(orderMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Return to previous activity
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error placing order: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, R.string.no_product_in_cart, Toast.LENGTH_SHORT).show()
        }
    }


    private fun dialog() {
        val dialogBinding = DialogPaymentBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.btnSubmit.setOnClickListener {
            // Proceed with the order
            proceedOrder(
                dialogBinding.dialogOrderType.text.toString(),
                dialogBinding.dialogOrderStatus.text.toString(),
                dialogBinding.dialogCustomer.text.toString(),
                calculatedTax = 0.0,  // You can calculate this properly
                discount = ""
            )
            dialog.dismiss()
        }

        dialog.show()
    }


}