package com.example.deeptraderspos.report.expensesReport

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityExpenseGraphDailyBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Expense
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExpenseGraphActivityDaily : InternetCheckActivity() {
    private lateinit var binding: ActivityExpenseGraphDailyBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExpenseGraphDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dailyExpenseGraph)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the status bar color
        Utils.setStatusBarColor(this)

        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

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
                updatePieChart(expensesList)
                binding.txtTotalSales.text = getString(R.string.total_sales) + getString(R.string.currency_symbol) + String.format("%.2f", totalExpense)
            }
            .addOnFailureListener { exception ->
                Log.w("ExpenseGraphActivityDaily", "Error getting documents: ", exception)
            }
    }

    private fun updatePieChart(expensesList: List<Expense>) {
        val entries = ArrayList<PieEntry>()

        for (expense in expensesList) {
            entries.add(PieEntry(expense.expenseAmount.toFloat(), expense.expenseName))
        }

        val pieDataSet = PieDataSet(entries, "Daily Expenses")
        pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList() // Set default color template
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 16f

        val pieData = PieData(pieDataSet)
        binding.barchart.data = pieData // Reusing the binding variable for the PieChart
        binding.barchart.invalidate() // Refresh the chart
    }
}