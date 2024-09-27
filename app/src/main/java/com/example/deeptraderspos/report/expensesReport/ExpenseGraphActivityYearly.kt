package com.example.deeptraderspos.report.expensesReport

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityExpenseGraphYearlyBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Expense
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
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
        setupWindowInsets()

        // Set the status bar color
        Utils.setStatusBarColor(this)

        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

        db = FirebaseFirestore.getInstance()

        // Trigger year picker
        binding.layoutYear.setOnClickListener { showYearPicker() }

        // Initialize with the current year data
        fetchYearlyExpenses(Calendar.getInstance().get(Calendar.YEAR))
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.yearlyExpenseGraph) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showYearPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Create a NumberPicker for selecting the year
        val yearPicker = NumberPicker(this).apply {
            minValue = currentYear - 10
            maxValue = currentYear + 10
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
                updatePieChart(expensesList)
                binding.txtTotalSales.text = getString(R.string.total_sales) + getString(R.string.currency_symbol) + String.format("%.2f", totalExpense)
            }
            .addOnFailureListener { exception ->
                Log.w("ExpenseGraphActivityYearly", "Error getting documents: ", exception)
                showErrorDialog(exception.message ?: "Error fetching data")
            }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updatePieChart(expensesList: List<Expense>) {
        val entries = expensesList.map { PieEntry(it.expenseAmount.toFloat(), it.expenseName) }

        val pieDataSet = PieDataSet(entries, "Expenses").apply {
            colors = ColorTemplate.createColors(ColorTemplate.VORDIPLOM_COLORS)
            valueTextColor = Color.BLACK
            valueTextSize = 16f
        }

        val pieData = PieData(pieDataSet)

        // Use the correct PieChart reference
        val pieChart: PieChart = binding.piechart as PieChart
        pieChart.data = pieData
        pieChart.invalidate() // Refresh the chart
    }
}