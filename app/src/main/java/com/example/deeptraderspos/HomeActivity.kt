package com.example.deeptraderspos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.customers.CustomersActivity
import com.example.deeptraderspos.databinding.ActivityHomeBinding
import com.example.deeptraderspos.expense.ExpenseActivity
import com.example.deeptraderspos.orders.customerOrders.OrdersActivity
import com.example.deeptraderspos.pos.PosActivity
import com.example.deeptraderspos.product.ProductActivity
import com.example.deeptraderspos.report.ReportActivity
import com.example.deeptraderspos.setting.SettingsActivity
import com.example.deeptraderspos.suppliers.SuppliersActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setCardClickListener(binding.cardCustomers, CustomersActivity::class.java)
        setCardClickListener(binding.cardSuppliers, SuppliersActivity::class.java)
        setCardClickListener(binding.cardProducts, ProductActivity::class.java)
        setCardClickListener(binding.cardPos, PosActivity::class.java)
        setCardClickListener(binding.cardOrderList, OrdersActivity::class.java)
        setCardClickListener(binding.cardReport, ReportActivity::class.java)
        setCardClickListener(binding.cardExpense, ExpenseActivity::class.java)
        setCardClickListener(binding.cardSettings, SettingsActivity::class.java)



    }


    private fun setCardClickListener(cardView: View, targetActivity: Class<*>) {
        cardView.setOnClickListener {
            val intent = Intent(this@HomeActivity, targetActivity)
            startActivity(intent)
        }
    }





}