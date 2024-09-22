package com.example.deeptraderspos.setting.shop

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityShopInformationBinding
import com.google.firebase.firestore.FirebaseFirestore

class ShopInformationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopInformationBinding

    private val currencyList = listOf("USD", "EUR", "GBP", "INR", "JPY", "AUD") // Add more currencies as needed

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShopInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.shopinfo)) { v, insets ->
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

        // Initially make all EditTexts not editable
        setEditTextsEditable(false)

        // Fetch shop info from Firestore and display it in the UI
        fetchShopInfo()

        // When the edit button is clicked, enable edit mode
        binding.txtShopEdit.setOnClickListener {
            binding.txtShopEdit.visibility = View.GONE
            binding.txtUpdate.visibility = View.VISIBLE
            setEditTextsEditable(true)
        }

        // Update shop info when the update button is clicked
        binding.txtUpdate.setOnClickListener {
            updateShopInfo()
        }

        // Set up click listener for currency selection
        binding.etxtShopCurrency.setOnClickListener {
            showCurrencyListDialog()
        }

    }

    private fun showCurrencyListDialog() {
        val currencyAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currencyList)

        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_list_search, null)
        dialog.setView(dialogView)
        dialog.setCancelable(false)

        val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)
        val dialogInput = dialogView.findViewById<EditText>(R.id.dialog_input)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogList = dialogView.findViewById<ListView>(R.id.dialog_list)

        dialogTitle.setText(R.string.select_currency)
        dialogList.adapter = currencyAdapter

        dialogInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                currencyAdapter.filter.filter(charSequence)
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
            val selectedCurrency = currencyAdapter.getItem(position) ?: return@setOnItemClickListener
            binding.etxtShopCurrency.setText(selectedCurrency)
        }
    }



    // Function to make EditTexts editable or non-editable
    private fun setEditTextsEditable(isEditable: Boolean) {
        binding.etxtShopName.isEnabled = isEditable
        binding.etxtShopContact.isEnabled = isEditable
        binding.etxtShopEmail.isEnabled = isEditable
        binding.etxtShopAddress.isEnabled = isEditable
        binding.etxtShopCurrency.isEnabled = isEditable
        binding.etxtTax.isEnabled = isEditable
    }

    // Fetch shop info from Firestore and display it in the UI
    private fun fetchShopInfo() {
        firestore.collection("shops").document("shopInfo")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    binding.etxtShopName.setText(document.getString("shopName"))
                    binding.etxtShopContact.setText(document.getString("shopContact"))
                    binding.etxtShopEmail.setText(document.getString("shopEmail"))
                    binding.etxtShopAddress.setText(document.getString("shopAddress"))
                    binding.etxtShopCurrency.setText(document.getString("shopCurrency"))
                    binding.etxtTax.setText(document.getString("shopTax"))
                }
            }
            .addOnFailureListener { e ->
                // Handle failure, show error message
                Toast.makeText(
                    this,
                    "Failed to fetch shop information: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Update shop info in Firestore
    private fun updateShopInfo() {
        val shopInfo = hashMapOf(
            "shopName" to binding.etxtShopName.text.toString(),
            "shopContact" to binding.etxtShopContact.text.toString(),
            "shopEmail" to binding.etxtShopEmail.text.toString(),
            "shopAddress" to binding.etxtShopAddress.text.toString(),
            "shopCurrency" to binding.etxtShopCurrency.text.toString(),
            "shopTax" to binding.etxtTax.text.toString()
        )

        firestore.collection("shops").document("shopInfo")
            .set(shopInfo)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Shop Information Updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish() // Close the activity after successful save
            }
            .addOnFailureListener { e ->
                // Handle failure, show error message
                Toast.makeText(
                    this,
                    "Failed to update shop information: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }




}