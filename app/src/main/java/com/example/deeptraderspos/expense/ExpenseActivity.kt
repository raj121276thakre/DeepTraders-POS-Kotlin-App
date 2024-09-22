package com.example.deeptraderspos.expense

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
import com.example.deeptraderspos.databinding.ActivityExpenseBinding
import com.example.deeptraderspos.models.Expense
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseActivity : AppCompatActivity() {
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


        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(this@ExpenseActivity, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        expenseAdapter = ExpenseAdapter(
            expenses = expensesList,
            this,
            onDeleteClicked = { expense ->
                deleteExpense(expense) { success ->
                    if (success) {
                        expenseAdapter.removeItem(expense)
                    } else {
                        // Handle error
                    }
                }
            },
            onEditClicked = { expense -> editExpense(expense) }
        )

        binding.expensesRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@ExpenseActivity)
            adapter = expenseAdapter
        }

        // Fetch expenses from Firestore
        fetchExpensesFromFirebase()

        // Implement search functionality
        binding.etxtExpenseSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList =
                    expensesList.filter { it.expenseName?.contains(s.toString(), true) == true }
                        .toMutableList()

                expenseAdapter = ExpenseAdapter(
                    expenses = filteredList,
                    this@ExpenseActivity,
                    onDeleteClicked = { expense ->
                        deleteExpense(expense) { success ->
                            if (success) {
                                expenseAdapter.removeItem(expense)
                            } else {
                                // Handle error
                            }
                        }
                    },
                    onEditClicked = { expense -> editExpense(expense) }
                )
                binding.expensesRecyclerview.adapter = expenseAdapter
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
    }

    private fun fetchExpensesFromFirebase() {
        firestore.collection("AllExpenses")
            .get()
            .addOnSuccessListener { result ->
                expensesList.clear() // Clear the list before adding new items
                for (document in result) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                }
                expenseAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    fun deleteExpense(expense: Expense, callback: (Boolean) -> Unit) {
        val expenseId = expense.id ?: run {
            Toast.makeText(this, "Expense ID is missing", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("AllExpenses").document(expenseId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
                callback(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete expense: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                callback(false)
            }
    }

    private fun editExpense(expense: Expense) {
        val intent = Intent(this, AddExpenseActivity::class.java).apply {
            putExtra("expense", expense)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerview()
        fetchExpensesFromFirebase() // Refresh the expenses list
    }

    private fun setupRecyclerview() {
        // Set up RecyclerView
        expenseAdapter = ExpenseAdapter(
            expenses = expensesList,
            this,
            onDeleteClicked = { expense ->
                deleteExpense(expense) { success ->
                    if (success) {
                        expenseAdapter.removeItem(expense)
                    } else {
                        // Handle error
                    }
                }
            },
            onEditClicked = { expense -> editExpense(expense) }
        )

        binding.expensesRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@ExpenseActivity)
            adapter = expenseAdapter
        }
    }


}