package com.example.deeptraderspos.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityOrdersMainBinding
import com.example.deeptraderspos.orders.customerOrders.OrdersActivity
import com.example.deeptraderspos.orders.supplierOrders.OrdersSupplierActivity

class OrdersMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrdersMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrdersMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.allMainOrders)) { v, insets ->
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

        setCardClickListener(binding.cardCustomerOrders, OrdersActivity::class.java)
        setCardClickListener(binding.cardSupplierOrders, OrdersSupplierActivity::class.java)

    }


    private fun setCardClickListener(cardView: View, targetActivity: Class<*>) {
        cardView.setOnClickListener {
            val intent = Intent(this@OrdersMainActivity, targetActivity)
            startActivity(intent)
        }
    }

}