package com.example.deeptraderspos.report.expensesReport

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityExpenseGraphDailyBinding
import com.example.deeptraderspos.models.Expense
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExpenseGraphActivityDaily : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseGraphDailyBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseGraphDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()

        // Trigger date picker
        binding.layoutDate.setOnClickListener {
            showDatePicker()
        }

        // Optionally initialize with today's data
        val now = Calendar.getInstance()
        fetchDailyExpenses(now)
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this, { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                fetchDailyExpenses(selectedDate)
                binding.txtSelectDate.text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(selectedDate.time)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun fetchDailyExpenses(selectedDate: Calendar) {
        // Format selected date to "YYYY-MM-DD"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)

        Log.d("ExpenseGraphActivityDaily", "Fetching expenses for date: $formattedDate")

        db.collection("AllExpenses")
            .whereEqualTo("expenseDate", formattedDate)
            .get()
            .addOnSuccessListener { documents ->
                val expensesList = mutableListOf<Expense>()
                var totalExpense = 0.0
                for (document in documents) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                    totalExpense += expense.expenseAmount
                }
                Log.d("ExpenseGraphActivityDaily", "Fetched ${documents.size()} documents.")
                updateBarChart(expensesList)
                binding.txtTotalSales.text = getString(R.string.total_sales) +getString(R.string.currency_symbol) + String.format("%.2f", totalExpense)
            }
            .addOnFailureListener { exception ->
                Log.w("ExpenseGraphActivityDaily", "Error getting documents: ", exception)
            }
    }

    private fun updateBarChart(expensesList: List<Expense>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, expense) in expensesList.withIndex()) {
            entries.add(BarEntry(index.toFloat(), expense.expenseAmount.toFloat()))
            labels.add(expense.expenseName) // Add expense names to labels
        }

        val barDataSet = BarDataSet(entries, "Daily Expenses")
        barDataSet.color = resources.getColor(R.color.colorPrimary)

        val barData = BarData(barDataSet)
        binding.barchart.data = barData
        binding.barchart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                return labels.getOrNull(value.toInt()) ?: ""
            }
        }
        binding.barchart.invalidate() // Refresh the chart
    }
}