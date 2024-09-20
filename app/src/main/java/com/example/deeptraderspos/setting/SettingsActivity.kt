package com.example.deeptraderspos.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.customers.CustomersActivity
import com.example.deeptraderspos.databinding.ActivityExpenseBinding
import com.example.deeptraderspos.databinding.ActivitySettingsBinding
import com.example.deeptraderspos.expense.ExpenseActivity
import com.example.deeptraderspos.expense.ExpenseAdapter
import com.example.deeptraderspos.models.Expense
import com.example.deeptraderspos.orders.OrdersActivity
import com.example.deeptraderspos.pos.PosActivity
import com.example.deeptraderspos.product.ProductActivity
import com.example.deeptraderspos.report.ReportActivity
import com.example.deeptraderspos.setting.categories.CategoriesActivity
import com.example.deeptraderspos.setting.order_type.OrderTypeActivity
import com.example.deeptraderspos.setting.payment_method.PaymentMethodActivity
import com.example.deeptraderspos.setting.shop.ShopInformationActivity
import com.example.deeptraderspos.setting.unit.UnitActivity
import com.example.deeptraderspos.suppliers.SuppliersActivity
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setting)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        setCardClickListener(binding.cardShopInfo, ShopInformationActivity::class.java)
        setCardClickListener(binding.cardCategory, CategoriesActivity::class.java)
        setCardClickListener(binding.cardPaymentMethod, PaymentMethodActivity::class.java)
        setCardClickListener(binding.cardOrderType, OrderTypeActivity::class.java)
        setCardClickListener(binding.cardUnit, UnitActivity::class.java)




    }


    private fun setCardClickListener(cardView: View, targetActivity: Class<*>) {
        cardView.setOnClickListener {
            val intent = Intent(this@SettingsActivity, targetActivity)
            startActivity(intent)
        }
    }


}