package com.example.deeptraderspos.report.expensesReport

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityExpenseGraphYearlyBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Expense
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ExpenseGraphActivityYearly : InternetCheckActivity() {
    private lateinit var binding: ActivityExpenseGraphYearlyBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExpenseGraphYearlyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.yearlyExpenseGraph)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()

        // Trigger year picker
        binding.layoutYear.setOnClickListener {
            showYearPicker()
        }

        // Initialize with the current year data
        val now = Calendar.getInstance()
        fetchYearlyExpenses(now.get(Calendar.YEAR))
    }

    private fun showYearPicker() {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)

        // Create a NumberPicker for selecting the year
        val yearPicker = NumberPicker(this).apply {
            minValue = currentYear - 10 // Change to desired range
            maxValue = currentYear + 10 // Change to desired range
            value = currentYear
        }

        // Show the dialog
        AlertDialog.Builder(this)
            .setTitle("Select Year")
            .setView(yearPicker)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = yearPicker.value
                fetchYearlyExpenses(selectedYear)
                binding.txtSelectYear.text = "$selectedYear"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchYearlyExpenses(year: Int) {
        val startOfYear = "$year-01-01"
        val endOfYear = "${year + 1}-01-01"

        Log.d("ExpenseGraphActivityYearly", "Fetching from $startOfYear to $endOfYear")

        db.collection("AllExpenses")
            .whereGreaterThanOrEqualTo("expenseDate", startOfYear)
            .whereLessThan("expenseDate", endOfYear)
            .get()
            .addOnSuccessListener { documents ->
                val expensesList = mutableListOf<Expense>()
                var totalExpense = 0.0
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                    totalExpense += expense.expenseAmount
                }
                updateBarChart(expensesList)
                binding.txtTotalSales.text = getString(R.string.total_sales) +getString(R.string.currency_symbol) + String.format("%.2f", totalExpense)
            }
            .addOnFailureListener { exception ->
                Log.w("ExpenseGraphActivityYearly", "Error getting documents: ", exception)
            }
    }

    private fun updateBarChart(expensesList: List<Expense>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, expense) in expensesList.withIndex()) {
            entries.add(BarEntry(index.toFloat(), expense.expenseAmount.toFloat()))
            labels.add(expense.expenseName) // Add expense names to labels
        }

        val barDataSet = BarDataSet(entries, "Expenses")
       // barDataSet.color = resources.getColor(R.color.colorPrimary)
        barDataSet.colors = listOf(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN,
            Color.LTGRAY,
            Color.rgb(255, 165, 0),
            Color.rgb(128, 0, 128),
            Color.rgb(255, 20, 147),
            Color.rgb(0, 128, 128),
            Color.RED
        ) // Set an array of colors


        val barData = BarData(barDataSet)
        binding.barchart.data = barData
        binding.barchart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return labels.getOrNull(value.toInt()) ?: ""
            }
        }
        binding.barchart.invalidate() // Refresh the chart
    }
}