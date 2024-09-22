package com.example.deeptraderspos.expense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityAddExpenseBinding
import com.example.deeptraderspos.models.Expense
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding

    private var expense: Expense? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.AddExpense)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set status bar color
        Utils.setStatusBarColor(this)

        db = FirebaseFirestore.getInstance()

        expense = intent.getParcelableExtra("expense")
        if (expense != null) {
            showExpenseDetails(expense!!)
        }


        // Handle add supplier button click
        binding.txtAddExpense.setOnClickListener {

            if (expense == null) {
                saveExpenseData()
            } else {
                updateExpense()
            }
        }


        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        val currentTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())

        binding.etxtDate.setText(currentDate)
        binding.etxtTime.setText(currentTime)


        binding.etxtDate.setOnClickListener {
            datePicker() // Call your date picker function
        }

        binding.etxtTime.setOnClickListener {
            timePicker() // Call your time picker function
        }


    }

    private fun showExpenseDetails(expense: Expense) {
        // Set the expense details in the input fields
        binding.etxtExpenseName.setText(expense.expenseName)
        binding.etxtExpenseNote.setText(expense.expenseNote ?: "") // Optional note
        binding.etxtExpenseAmount.setText(expense.expenseAmount.toString())
        binding.etxtDate.setText(expense.expenseDate)
        binding.etxtTime.setText(expense.expenseTime)

        // Change the button text to indicate updating the expense
        binding.txtAddExpense.text = "Update Expense"
    }


    private fun saveExpenseData() {
        // Collect expense data from input fields
        val expenseName = binding.etxtExpenseName.text.toString().trim()
        val expenseNote = binding.etxtExpenseNote.text.toString().trim()
        val expenseAmount = binding.etxtExpenseAmount.text.toString().toDoubleOrNull() ?: 0.0
        val expenseDate = binding.etxtDate.text.toString().trim()
        val expenseTime = binding.etxtTime.text.toString().trim()

        // Validation check
        if (expenseName.isEmpty() || expenseAmount == 0.0 || expenseDate.isEmpty() || expenseTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create an Expense object
        val expense = Expense(
            expenseName = expenseName,
            expenseNote = if (expenseNote.isNotEmpty()) expenseNote else null, // Optional note
            expenseAmount = expenseAmount,
            expenseDate = expenseDate,
            expenseTime = expenseTime
        )

        // Save to Firestore
        db.collection("AllExpenses")
            .add(expense)
            .addOnSuccessListener { documentReference ->
                // Retrieve the generated document ID
                val documentId = documentReference.id

                // Create a new expense object with the ID
                val expenseWithId = expense.copy(id = documentId)

                // Update the Firestore document with the new expense object containing the ID
                db.collection("AllExpenses").document(documentId)
                    .set(expenseWithId)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Expense added successfully with ID",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Close the activity after successful save
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to update expense: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add expense: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun updateExpense() {
        // Collect expense data from input fields
        val expenseName = binding.etxtExpenseName.text.toString().trim()
        val expenseNote = binding.etxtExpenseNote.text.toString().trim()
        val expenseAmount = binding.etxtExpenseAmount.text.toString().toDoubleOrNull() ?: 0.0
        val expenseDate = binding.etxtDate.text.toString().trim()
        val expenseTime = binding.etxtTime.text.toString().trim()

        // Validation check
        if (expenseName.isEmpty() || expenseAmount == 0.0 || expenseDate.isEmpty() || expenseTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Update the expense object with new data
        expense?.let {
            val updatedExpense = it.copy(
                expenseName = expenseName,
                expenseNote = if (expenseNote.isNotEmpty()) expenseNote else null, // Optional note
                expenseAmount = expenseAmount,
                expenseDate = expenseDate,
                expenseTime = expenseTime
            )

            // Update Firestore document with the new expense data
            updatedExpense.id?.let { documentId ->
                db.collection("AllExpenses")
                    .document(documentId)  // Use the existing expense ID
                    .set(updatedExpense)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT)
                            .show()
                        finish() // Close the activity after successful update
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to update expense: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }


    private fun datePicker() {
        // Get Current Date
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Increment month because monthOfYear is 0-based
                val month = monthOfYear + 1
                val fm = if (month < 10) "0$month" else "$month"
                val fd = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

                // Format the date string
                val date_time = "$year-$fm-$fd"

                // Set the selected date in the EditText
                binding.etxtDate.setText(date_time)
            }, mYear, mMonth, mDay
        )

        datePickerDialog.show()
    }

    private fun timePicker() {
        // Get Current Time
        val c = Calendar.getInstance()
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)

        // Create a TimePickerDialog
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val amPm: String
                val hour: Int

                if (hourOfDay < 12) {
                    amPm = "AM"
                    hour = hourOfDay
                } else {
                    amPm = "PM"
                    hour = hourOfDay - 12
                }

                // Set the selected time in the EditText
                binding.etxtTime.setText(String.format("%02d:%02d %s", hour, minute, amPm))
            }, mHour, mMinute, false
        )

        timePickerDialog.show()
    }




}