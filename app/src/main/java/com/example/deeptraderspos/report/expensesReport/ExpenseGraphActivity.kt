package com.example.deeptraderspos.report.expensesReport

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityExpenseGraphBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Expense
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExpenseGraphActivity : InternetCheckActivity() {
    private lateinit var binding: ActivityExpenseGraphBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExpenseGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.expenseGraph)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()

        // Trigger month picker
        binding.layoutYear.setOnClickListener {
            showMonthPicker()
        }

        // Optionally initialize with the current month data
        val now = Calendar.getInstance()
        fetchMonthlyExpenses(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
    }

    private fun showMonthPicker() {
        val now = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this, { _, year, month, _ ->
                val selectedMonth = month + 1 // Month is zero-based
                fetchMonthlyExpenses(year, selectedMonth)
                // Set the selected month in the TextView
                binding.txtSelectMonth.text = "${getMonthName(selectedMonth)} $year"
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show() // Show the dialog without trying to hide the day picker
    }

    private fun fetchMonthlyExpenses(year: Int, month: Int) {
        // Format dates for Firestore query
        val startOfMonth = "$year-${String.format("%02d", month)}-01"
        val endOfMonth = if (month == 12) {
            "${year + 1}-01-01" // Start of next year if December
        } else {
            "$year-${String.format("%02d", month + 1)}-01" // Start of next month
        }

        Log.d("ExpenseGraphActivity", "Fetching from $startOfMonth to $endOfMonth")

        db.collection("AllExpenses")
            .whereGreaterThanOrEqualTo("expenseDate", startOfMonth)
            .whereLessThan("expenseDate", endOfMonth) // Ensure exclusive end date
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
                Log.w("ExpenseGraphActivity", "Error getting documents: ", exception)
            }
    }

    private fun updateBarChart(expensesList: List<Expense>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>() // For storing expense names

        for ((index, expense) in expensesList.withIndex()) {
            entries.add(BarEntry(index.toFloat(), expense.expenseAmount.toFloat()))
            labels.add(expense.expenseName) // Add expense names to labels
        }

        val barDataSet = BarDataSet(entries, "Expenses")
        barDataSet.color = resources.getColor(R.color.colorPrimary)

        val barData = BarData(barDataSet)
        binding.barchart.data = barData

        // Set custom value formatter for x-axis to show expense names
        binding.barchart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                return labels.getOrNull(value.toInt()) ?: ""
            }
        }

        binding.barchart.invalidate() // Refresh the chart
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return monthNames[month - 1] // month is 1-based in this context
    }
}