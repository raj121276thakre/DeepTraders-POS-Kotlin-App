package com.example.deeptraderspos.report

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityReportBinding
import com.example.deeptraderspos.databinding.ActivitySettingsBinding
import com.example.deeptraderspos.report.expensesReport.ExpenseGraphActivity
import com.example.deeptraderspos.report.expensesReport.ExpenseReportActivity
import com.example.deeptraderspos.setting.categories.CategoriesActivity
import com.example.deeptraderspos.setting.order_type.OrderTypeActivity
import com.example.deeptraderspos.setting.payment_method.PaymentMethodActivity
import com.example.deeptraderspos.setting.shop.ShopInformationActivity
import com.example.deeptraderspos.setting.unit.UnitActivity

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.report)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set status bar color
        Utils.setStatusBarColor(this)

//        // Go Back Button
//        val goBackBtn = binding.menuIcon
//        goBackBtn.setOnClickListener {
//            onBackPressed()  // This will take you back to the previous activity
//        }


        setCardClickListener(binding.cardSalesReport, SalesReportActivity::class.java)
        setCardClickListener(binding.cardExpenseReport, ExpenseReportActivity::class.java)
        setCardClickListener(binding.cardGraphReport, GraphReportActivity::class.java)
        setCardClickListener(binding.cardExpenseGraph, ExpenseGraphActivity::class.java)

    }


    private fun setCardClickListener(cardView: View, targetActivity: Class<*>) {
        cardView.setOnClickListener {
            val intent = Intent(this@ReportActivity, targetActivity)
            startActivity(intent)
        }
    }

}