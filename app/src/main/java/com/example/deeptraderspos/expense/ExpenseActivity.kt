package com.example.deeptraderspos.expense

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityExpenseBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Expense
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenseActivity : InternetCheckActivity() {
    private lateinit var binding: ActivityExpenseBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expensesList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.expenses)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Utils.setStatusBarColor(this)

        // Go Back Button
        binding.menuIcon.setOnClickListener { onBackPressed() }

        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(this@ExpenseActivity, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        setupRecyclerview()

        // Fetch expenses from Firestore
        fetchExpensesFromFirebase()

        // Implement search functionality
        binding.etxtExpenseSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterExpenses(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set up the calendar icon to select a date
        binding.imgCalendar.setOnClickListener { showDatePicker() }

        // Optionally initialize with today's data
        val now = Calendar.getInstance()
        fetchDailyExpenses(now)

    }


    private fun fetchDailyExpenses(selectedDate: Calendar) {
        // Format selected date to "YYYY-MM-DD"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)

        Log.d("ExpenseGraphActivityDaily", "Fetching expenses for date: $formattedDate")

        firestore.collection("AllExpenses")
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
                binding.txtSelectDate.text = formattedDate
                updatePieChart(expensesList)
                binding.txtTotalSales.text =
                    getString(R.string.total_sales) + getString(R.string.currency_symbol) + String.format(
                        "%.2f",
                        totalExpense
                    )
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


    private fun setupRecyclerview() {
        expenseAdapter = ExpenseAdapter(
            expenses = expensesList,
            this,
            onDeleteClicked = { expense ->
                deleteExpense(expense) {
                    if (it) expenseAdapter.removeItem(
                        expense
                    )
                }
            },
            onEditClicked = { expense -> editExpense(expense) }
        )

        binding.expensesRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@ExpenseActivity)
            adapter = expenseAdapter
        }
    }

    private fun fetchExpensesFromFirebase() {
        showProgressBar("Loading Expense information...")
        firestore.collection("AllExpenses")
            .get()
            .addOnSuccessListener { result ->
                hideProgressBar()
                expensesList.clear()
                for (document in result) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                }
                expensesList.reverse()
                expenseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchExpensesByDate(selectedDate: String) {
        showProgressBar("Loading expenses for $selectedDate...")
        firestore.collection("AllExpenses")
            .whereEqualTo("expenseDate", selectedDate)
            .get()
            .addOnSuccessListener { result ->
                hideProgressBar()
                expensesList.clear()
                for (document in result) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                }
                expensesList.reverse()
                expenseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(
                    this,
                    "Failed to load expenses for $selectedDate",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val selectedDateString =
                    String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                fetchExpensesByDate(selectedDateString) // Fetch expenses based on selected date
                fetchDailyExpenses(selectedDate)
                binding.txtSelectDate.text =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate.time)


            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun deleteExpense(expense: Expense, callback: (Boolean) -> Unit) {
        val expenseId = expense.id ?: run {
            Toast.makeText(this, "Expense ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        firestore.collection("AllExpenses").document(expenseId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete expense", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun editExpense(expense: Expense) {
        val intent = Intent(this, AddExpenseActivity::class.java).apply {
            putExtra("expense", expense)
        }
        startActivityForResult(intent, REQUEST_CODE_EDIT_EXPENSE)
    }

    override fun onResume() {
        super.onResume()
        fetchExpensesFromFirebase() // Refresh the expenses list after returning from Add/Edit activities
    }

    private fun filterExpenses(query: String) {
        val filteredList =
            expensesList.filter { it.expenseName?.contains(query, true) == true }.toMutableList()
        expenseAdapter = ExpenseAdapter(
            expenses = filteredList,
            this@ExpenseActivity,
            onDeleteClicked = { expense ->
                deleteExpense(expense) {
                    if (it) expenseAdapter.removeItem(
                        expense
                    )
                }
            },
            onEditClicked = { expense -> editExpense(expense) }
        )
        binding.expensesRecyclerview.adapter = expenseAdapter
    }

    companion object {
        private const val REQUEST_CODE_EDIT_EXPENSE = 1001
    }
}