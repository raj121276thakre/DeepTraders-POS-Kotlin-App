package com.example.deeptraderspos.expense

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityExpenseBinding
import com.example.deeptraderspos.expense.AddExpenseActivity
import com.example.deeptraderspos.expense.ExpenseAdapter
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Expense
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

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
    }

    private fun setupRecyclerview() {
        expenseAdapter = ExpenseAdapter(
            expenses = expensesList,
            this,
            onDeleteClicked = { expense -> deleteExpense(expense) { if (it) expenseAdapter.removeItem(expense) } },
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
                Toast.makeText(this, "Failed to load expenses for $selectedDate", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                fetchExpensesByDate(selectedDate) // Fetch expenses based on selected date
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
        val filteredList = expensesList.filter { it.expenseName?.contains(query, true) == true }.toMutableList()
        expenseAdapter = ExpenseAdapter(
            expenses = filteredList,
            this@ExpenseActivity,
            onDeleteClicked = { expense -> deleteExpense(expense) { if (it) expenseAdapter.removeItem(expense) } },
            onEditClicked = { expense -> editExpense(expense) }
        )
        binding.expensesRecyclerview.adapter = expenseAdapter
    }

    companion object {
        private const val REQUEST_CODE_EDIT_EXPENSE = 1001
    }
}