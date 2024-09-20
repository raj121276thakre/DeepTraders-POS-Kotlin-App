package com.example.deeptraderspos.product

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.ActivityAddProductBinding
import com.example.deeptraderspos.models.Product
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding

    private var product: Product? = null
    private lateinit var db: FirebaseFirestore

    private val productSupplier = mutableListOf<Map<String, String>>()
    private var selectedSupplierID: String = "0" // Store selected supplier ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Addproduct)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        db = FirebaseFirestore.getInstance()

        product = intent.getParcelableExtra("product")
        if (product != null) {
            showProductDetails(product!!)
        }

        // Handle add product button click
        binding.txtAddProduct.setOnClickListener {
            if (product == null) {
                saveProductData()
            } else {
                updateProduct()
            }
        }

        binding.etxtSupplier.setOnClickListener {
            showSuppliersList()
        }





    }


    //private fun showCategoryList()

    //private fun showWeightUnitsList()



    private fun showSuppliersList() {
            val supplierAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)

            // Fetch suppliers from Firestore
            db.collection("AllSuppliers")
                .get()
                .addOnSuccessListener { documents ->
                    val supplierNames = mutableListOf<String>()

                    for (document in documents) {
                        val supplierName = document.getString("supplierName") ?: ""
                        supplierNames.add(supplierName)

                        // Create a new Map<String, String> to store supplier data
                        val supplierData = mutableMapOf<String, String>()
                        for ((key, value) in document.data) {
                            supplierData[key] = value.toString() // Convert each value to String
                        }
                        productSupplier.add(supplierData) // Add the new map to productSupplier
                    }

                    supplierAdapter.addAll(supplierNames)

                    val dialog = AlertDialog.Builder(this)
                    val dialogView = layoutInflater.inflate(R.layout.dialog_list_search, null)
                    dialog.setView(dialogView)
                    dialog.setCancelable(false)

                    val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)
                    val dialogInput = dialogView.findViewById<EditText>(R.id.dialog_input)
                    val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                    val dialogList = dialogView.findViewById<ListView>(R.id.dialog_list)

                    dialogTitle.setText(R.string.suppliers)
                    dialogList.adapter = supplierAdapter

                    dialogInput.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                            supplierAdapter.filter.filter(charSequence)
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })

                    val alertDialog = dialog.create()

                    dialogButton.setOnClickListener {
                        alertDialog.dismiss()
                    }

                    alertDialog.show()

                    dialogList.setOnItemClickListener { parent, view, position, id ->
                        alertDialog.dismiss()
                        val selectedItem = supplierAdapter.getItem(position) ?: return@setOnItemClickListener

                        binding.etxtSupplier.setText(selectedItem)

                        var supplierId = "0"
                        for (i in supplierNames.indices) {
                            if (supplierNames[i].equals(selectedItem, ignoreCase = true)) {
                                supplierId = productSupplier[i]["suppliers_id"] ?: "0"
                            }
                        }

                        selectedSupplierID = supplierId
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch suppliers: ${e.message}", Toast.LENGTH_SHORT).show()
                }

    }



    private fun showProductDetails(product: Product) {
        // Set the product details in the input fields
        binding.etxtProductName.setText(product.productName)
        binding.etxtProductCode.setText(product.productCode)
        binding.etxtProductCategory.setText(product.productCategory)
        binding.etxtProductDescription.setText(product.productDescription)
        binding.etxtBuyPrice.setText(product.buyPrice.toString())
        binding.etxtProductSellPrice.setText(product.sellPrice.toString())
        binding.etxtProductStock.setText(product.stock.toString())
        binding.etxtProductWeight.setText(product.weight.toString())
        binding.etxtProductWeightUnit.setText(product.weightUnit) // If applicable
        binding.etxtSupplier.setText(product.supplier) // If applicable

        // Change the button text to indicate updating the product
        binding.txtAddProduct.text = "Update Product"
    }


    private fun saveProductData() {
        // Collect product data from input fields
        val productName = binding.etxtProductName.text.toString().trim()
        val productCode = binding.etxtProductCode.text.toString().trim()
        val productCategory = binding.etxtProductCategory.text.toString().trim()
        val productDescription = binding.etxtProductDescription.text.toString().trim()
        val buyPrice = binding.etxtBuyPrice.text.toString().toDoubleOrNull() ?: 0.0
        val sellPrice = binding.etxtProductSellPrice.text.toString().toDoubleOrNull() ?: 0.0
        val stock = binding.etxtProductStock.text.toString().toIntOrNull() ?: 0
        val weight = binding.etxtProductWeight.text.toString().toDoubleOrNull() ?: 0.0
        val weightUnit = binding.etxtProductWeightUnit.text.toString().trim()
        val supplier = binding.etxtSupplier.text.toString().trim()

        // Validation check
        if (productName.isEmpty() || productCode.isEmpty() || buyPrice == 0.0 || sellPrice == 0.0 || stock == 0) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a Product object
        val product = Product(
            productName = productName,
            productCode = productCode,
            productCategory = productCategory,
            productDescription = productDescription,
            buyPrice = buyPrice,
            sellPrice = sellPrice,
            stock = stock,
            weight = weight,
            weightUnit = weightUnit,
            supplier = supplier
        )

        // Save to Firestore
        db.collection("AllProducts")
            .add(product)
            .addOnSuccessListener { documentReference ->
                // Retrieve the generated document ID
                val documentId = documentReference.id

                // Create a new product object with the ID
                val productWithId = product.copy(id = documentId)

                // Update the Firestore document with the new product object containing the ID
                db.collection("AllProducts").document(documentId)
                    .set(productWithId)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Product added successfully with ID",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Close the activity after successful save
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to update product: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add product: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    private fun updateProduct() {
        // Collect product data from input fields
        val productName = binding.etxtProductName.text.toString().trim()
        val productCode = binding.etxtProductCode.text.toString().trim()
        val productCategory = binding.etxtProductCategory.text.toString().trim()
        val productDescription = binding.etxtProductDescription.text.toString().trim()
        val buyPrice = binding.etxtBuyPrice.text.toString().toDoubleOrNull() ?: 0.0
        val sellPrice = binding.etxtProductSellPrice.text.toString().toDoubleOrNull() ?: 0.0
        val stock = binding.etxtProductStock.text.toString().toIntOrNull() ?: 0
        val weight = binding.etxtProductWeight.text.toString().toDoubleOrNull() ?: 0.0
        val weightUnit = binding.etxtProductWeightUnit.text.toString().trim()
        val supplier = binding.etxtSupplier.text.toString().trim()

        // Validation check
        if (productName.isEmpty() || productCode.isEmpty() || buyPrice == 0.0 || sellPrice == 0.0 || stock == 0) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Update the product object with new data
        product?.let {
            val updatedProduct = it.copy(
                productName = productName,
                productCode = productCode,
                productCategory = productCategory,
                productDescription = productDescription,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                stock = stock,
                weight = weight,
                weightUnit = weightUnit,
                supplier = supplier
            )

            // Update Firestore document with the new product data
            updatedProduct.id?.let { documentId ->
                db.collection("AllProducts")
                    .document(documentId)  // Use the existing product ID
                    .set(updatedProduct)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT)
                            .show()
                        finish() // Close the activity after successful update
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to update product: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }




}