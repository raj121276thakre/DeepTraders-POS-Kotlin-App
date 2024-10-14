package com.example.deeptraderspos.report

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.adapter.SalesReportAdapter
import com.example.deeptraderspos.customers.CustomersActivity
import com.example.deeptraderspos.databinding.ActivitySalesReportBinding
import com.example.deeptraderspos.expense.AddExpenseActivity
import com.example.deeptraderspos.expense.ExpenseAdapter
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Expense
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.Product
import com.example.deeptraderspos.models.ProductOrder
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class SalesReportActivity : InternetCheckActivity() {
    private lateinit var binding: ActivitySalesReportBinding

    private lateinit var orderDetailsAdapter: SalesReportAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var barChart: BarChart
    private var mYear: Int = 0

    private lateinit var expenseAdapter: ExpenseAdapter
    private val expensesList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySalesReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.salesReport)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set status bar color
        Utils.setStatusBarColor(this)

        // Initialize BarChart
        barChart = binding.barchart

        binding.totalOrderBtn.setOnClickListener {
            val intent = Intent(
                this,
                CustomersActivity::class.java
            )  // Replace with your POS Activity class name
            startActivity(intent)
        }

        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }
        firestore = FirebaseFirestore.getInstance()

        // Set up views
        setupViews()
        // chooseYearOnly()

        val currentYear = SimpleDateFormat("yyyy", Locale.ENGLISH).format(Date())
        binding.txtSelectYear.setText(getString(R.string.year) + " " + currentYear)

        //  mYear = currentYear.toInt()

        // Get data from Firestore
        fetchData()
        fetchAndCalculateTotalProfit()
        // fetchAllOrdersForGraph(mYear)

        binding.sortSalesBtn.setOnClickListener {
            showSortMenu(binding.sortSalesBtn)
        }

        binding.addExpenses.setOnClickListener {
//            val intent = Intent(this@SalesReportActivity, AddExpenseActivity::class.java)
//            startActivity(intent)
            showAddExpenseDialog()
        }

        // Set up RecyclerView
        setupRecyclerview()

        // Fetch expenses from Firestore
        fetchExpensesFromFirebase()
        // Set up the calendar icon to select a date
        binding.imgCalendar.setOnClickListener { showDatePicker() }


        binding.addTodaysProfit.setOnClickListener { showAddProfitDialog() }

    }


    private fun fetchAndCalculateTotalProfit(timeFrame: String? = null) {
        val firestore = FirebaseFirestore.getInstance()
        val query = firestore.collection("ProfitsData")

        // Apply filters based on the time frame
        val startDate: String?
        val endDate: String?

        when (timeFrame) {


            "monthly" -> {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time)

                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                endDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time)
            }

            "yearly" -> {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                calendar.set(Calendar.MONTH, 0) // January
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time)

                calendar.set(Calendar.MONTH, 11) // December
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                endDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time)
            }

            else -> {
                // No filtering, fetch all orders
                startDate = null
                endDate = null
            }
        }

        val orderQuery = if (startDate != null && endDate != null) {
            query.whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
        } else {
            query // No filtering
        }



       // val profitsCollection = firestore.collection("ProfitsData")

        orderQuery.get()
            .addOnSuccessListener { querySnapshot ->
                var totalProfit = 0.0 // Initialize total profit

                // Loop through each document and sum the profit amounts
                for (document in querySnapshot.documents) {
                    val profitAmount = document.getDouble("profitAmount") ?: 0.0
                    totalProfit += profitAmount
                }

                // Set total profit to the TextView
                binding.txtProfit.text = "Total Profit: ₹${totalProfit}"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching profits: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    @SuppressLint("MissingInflatedId")
    private fun showAddProfitDialog() {
        // Create the dialog
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_profit, null)
        dialog.setView(dialogView)

        val edtProfitAmount = dialogView.findViewById<EditText>(R.id.edt_profit_amount)
        val btnAddProfit = dialogView.findViewById<Button>(R.id.btn_add_profit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        val alertDialog = dialog.create()

        btnAddProfit.setOnClickListener {
            val profitAmount = edtProfitAmount.text.toString().toDoubleOrNull()

            if (profitAmount != null && profitAmount > 0) {
                // Get current date and time
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                // Create a unique ID for the profit entry
                val profitId = firestore.collection("ProfitsData").document().id

                // Prepare the data to be stored in Firestore
                val profitData = hashMapOf(
                    "id" to profitId,
                    "profitAmount" to profitAmount,
                    "date" to currentDate,
                    "time" to currentTime
                )

                // Store the data in Firestore
                firestore.collection("ProfitsData")
                    .document(profitId)
                    .set(profitData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Today's profit added successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchAndCalculateTotalProfit()
                        alertDialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error adding profit: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "Please enter a valid profit amount", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss() // Close the dialog if canceled
        }

        alertDialog.show()
    }


    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)

        // Get references to the EditText fields in the dialog view
        val etxtExpenseDate = dialogView.findViewById<EditText>(R.id.etxt_date)
        val etxtExpenseTime = dialogView.findViewById<EditText>(R.id.etxt_time)

        // Set current date and time initially
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        val currentTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())

        etxtExpenseDate.setText(currentDate)
        etxtExpenseTime.setText(currentTime)

        // Handle DatePicker for expense date
        etxtExpenseDate.setOnClickListener {
            datePicker(etxtExpenseDate) // Call date picker function
        }

        // Handle TimePicker for expense time
        etxtExpenseTime.setOnClickListener {
            timePicker(etxtExpenseTime) // Call time picker function
        }

        // AlertDialog builder
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Expense")
            .setPositiveButton("Add") { dialog, _ ->
                // Handle the positive button click (Add button)
                val expenseName =
                    dialogView.findViewById<EditText>(R.id.etxt_expense_name).text.toString()
                val expenseAmount =
                    dialogView.findViewById<EditText>(R.id.etxt_expense_amount).text.toString()
                        .toDoubleOrNull() ?: 0.0
                val expenseNote =
                    dialogView.findViewById<EditText>(R.id.etxt_expense_note).text.toString().trim()
                val expenseDate = etxtExpenseDate.text.toString().trim()
                val expenseTime = etxtExpenseTime.text.toString().trim()

                // Validation
                if (expenseName.isEmpty() || expenseAmount == 0.0 || expenseDate.isEmpty() || expenseTime.isEmpty()) {
                    Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }

                // Create an Expense object and save to Firebase (same logic as in AddExpenseActivity)
                val expense = Expense(
                    expenseName = expenseName,
                    expenseNote = if (expenseNote.isNotEmpty()) expenseNote else null,
                    expenseAmount = expenseAmount,
                    expenseDate = expenseDate,
                    expenseTime = expenseTime
                )

                saveExpenseToFirestore(expense)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Cancel button
            }

        // Show the dialog
        dialogBuilder.create().show()
    }

    private fun saveExpenseToFirestore(expense: Expense) {
        // Your Firestore logic for saving the expense
        showProgressBar("Saving Expense information...")
        // Can copy from your saveExpenseData() in AddExpenseActivity
        firestore.collection("AllExpenses")
            .add(expense)
            .addOnSuccessListener { documentReference ->
                hideProgressBar()
                fetchExpensesFromFirebase()
            }
            .addOnFailureListener { e ->
                hideProgressBar()

            }
    }


    private fun datePicker(etxtExpenseDate: EditText) {
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
                etxtExpenseDate.setText(date_time)
            }, mYear, mMonth, mDay
        )

        datePickerDialog.show()
    }

    private fun timePicker(etxtExpenseTime: EditText) {
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
                etxtExpenseTime.setText(String.format("%02d:%02d %s", hour, minute, amPm))
            }, mHour, mMinute, false
        )

        timePickerDialog.show()
    }


    private fun chooseYearOnly() {
        findViewById<TextView>(R.id.txt_select_year).setOnClickListener {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val yearsList = (2000..currentYear).toList().reversed()

            val builder = MaterialAlertDialogBuilder(this@SalesReportActivity)
            builder.setTitle(getString(R.string.select_year))

            val yearsArray = yearsList.map { it.toString() }

            // Use custom adapter to style the list items
            val adapter =
                object : ArrayAdapter<String>(this, R.layout.dialog_list_item, yearsArray) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(R.id.textItem)
                        textView.text = yearsArray[position]
                        return view
                    }
                }

            builder.setAdapter(adapter) { _, which ->
                val selectedYear = yearsList[which]
                binding.txtSelectYear.text = getString(R.string.year) + " " + selectedYear
                mYear = selectedYear
                //   fetchAllOrdersForGraph(mYear)
            }

            builder.setPositiveButton(android.R.string.ok, null)
            builder.setNegativeButton(android.R.string.cancel, null)

            val dialog = builder.create()
            dialog.show()
        }


    }


    //expense

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
            layoutManager = LinearLayoutManager(this@SalesReportActivity)
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
                var totalExpenses = 0.0 // Initialize total expenses

                for (document in result) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                    totalExpenses += expense.expenseAmount // Sum up the expense amount
                }
                expensesList.reverse()
                expenseAdapter.notifyDataSetChanged()
                binding.txtExpense.text = "Expenses: ₹${totalExpenses}"
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
                var totalExpenses = 0.0 // Initialize total expenses
                for (document in result) {
                    val expense = document.toObject(Expense::class.java)
                    expensesList.add(expense)
                    totalExpenses += expense.expenseAmount // Sum up the expense amount
                }
                expensesList.reverse()
                expenseAdapter.notifyDataSetChanged()

                binding.txtExpense.text = "Expenses: ₹${totalExpenses}"
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
                // fetchDailyExpenses(selectedDate)
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

    companion object {
        private const val REQUEST_CODE_EDIT_EXPENSE = 1001
    }


// graph

    private fun fetchAllOrdersForGraph(year: Int) {
        val monthlySales = mutableMapOf<String, Double>().withDefault { 0.0 }

        // Fetch orders for the given year from your data source
        getOrdersForYear(year) { orders ->
            // Process each order to sum sales for each month
            for (order in orders) {
                // Extract month from orderDate string (assuming it's in "YYYY-MM-DD" format)
                val month = order.orderDate.substring(5, 7) // Extract month (MM)
                val salesAmount = order.totalPrice // Use totalPrice for sales amount
                monthlySales[month] = monthlySales.getValue(month) + salesAmount
            }

            // Call setupBarChart with the aggregated monthly sales data
            //  setupBarChart(monthlySales, year)
        }
    }

    // Function to fetch orders from Firestore for the specified year
    private fun getOrdersForYear(year: Int, callback: (List<Order>) -> Unit) {
        val ordersList = mutableListOf<Order>()
        val firestore = FirebaseFirestore.getInstance()

        // Adjust the query to filter by year using whereGreaterThanOrEqualTo and whereLessThan
        val startDate = "$year-01-01"
        val endDate = "$year-12-31"

        // Assuming you have an "orders" collection in Firestore
        firestore.collection("AllOrders")
            .whereGreaterThanOrEqualTo("orderDate", startDate)
            .whereLessThanOrEqualTo("orderDate", endDate)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val orderId = document.getString("orderId") ?: ""
                    val orderDate = document.getString("orderDate") ?: ""
                    val orderTime = document.getString("orderTime") ?: ""
                    val orderType = document.getString("orderType") ?: ""
                    val orderStatus = document.getString("orderStatus") ?: ""
                    val paymentMethod = document.getString("paymentMethod") ?: ""
                    val customerName = document.getString("customerName") ?: ""
                    val supplierName = document.getString("supplierName") ?: ""
                    val tax = document.getDouble("tax") ?: 0.0
                    val discount = document.getString("discount") ?: ""
                    val totalPrice = document.getDouble("totalPrice") ?: 0.0
                    val totalPaidAmount = document.getDouble("totalPaidAmount") ?: 0.0
                    val remainingAmount = document.getDouble("remainingAmount") ?: 0.0
                    val remainingAmtPaidDate = document.getString("remainingAmtPaidDate") ?: ""
                    val remainingAmtPaidTime = document.getString("remainingAmtPaidTime") ?: ""

                    // Assuming products is a list of ProductOrder objects
                    val products = document.get("products") as? List<ProductOrder> ?: emptyList()

                    // Create Order object
                    val order = Order(
                        orderId, orderDate, orderTime, orderType, orderStatus, paymentMethod,
                        customerName, supplierName, tax, discount, products, totalPrice,
                        totalPaidAmount, remainingAmount, remainingAmtPaidDate, remainingAmtPaidTime
                    )
                    ordersList.add(order)
                }
                // Invoke the callback with the fetched orders
                callback(ordersList)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore Error", "Error getting documents: ", exception)
                // Invoke the callback with an empty list in case of error
                callback(emptyList())
            }
    }

    // Setup Bar Chart function remains unchanged
    private fun setupBarChart(monthlySales: Map<String, Double>, year: Int) {
        val barEntries = ArrayList<BarEntry>()

        for (i in 1..12) {
            val monthKey = i.toString().padStart(2, '0')
            val sales = monthlySales[monthKey] ?: 0.0
            barEntries.add(BarEntry(i.toFloat(), sales.toFloat()))
        }

        val monthList = arrayOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
        )
        binding.barchart.xAxis.valueFormatter = IndexAxisValueFormatter(monthList)
        binding.barchart.xAxis.setCenterAxisLabels(true)
        binding.barchart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barchart.xAxis.granularity = 1f
        binding.barchart.xAxis.isGranularityEnabled = true
        binding.barchart.xAxis.labelCount = 12

        val dataSet = BarDataSet(barEntries, "Monthly Sales Report for $year")
        //  dataSet.color = ColorTemplate.LIBERTY_COLORS[0]
        //  dataSet.color = Color.RED
        dataSet.colors = listOf(
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

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        binding.barchart.data = barData
        binding.barchart.setScaleEnabled(false)

        binding.barchart.notifyDataSetChanged()
        binding.barchart.invalidate()
    }


// graph......


    // Call this function to start the fetching process
    private fun fetchData(timeFrame: String? = null) {
        fetchAllProducts { productMap ->
            fetchAllOrdersData(productMap, timeFrame)
        }
    }


    private fun fetchAllProducts(callback: (Map<String, Product>) -> Unit) {
        firestore.collection("AllProducts")
            .get()
            .addOnSuccessListener { documents ->
                val productMap = mutableMapOf<String, Product>()
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    productMap[document.id] = product // Map product ID to product object
                }
                callback(productMap)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch products", Toast.LENGTH_SHORT).show()
                callback(emptyMap())
            }
    }

    private fun fetchAllOrdersData(productMap: Map<String, Product>, timeFrame: String?) {

        showProgressBar("fetching sales information...")

        val query = firestore.collection("AllOrders")

        // Apply filters based on the time frame
        val startDate: String?
        val endDate: String?

        when (timeFrame) {
//            "daily" -> {
//                // Get today's date in "yyyy-MM-dd" format
//                val calendar = Calendar.getInstance()
//                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
//
//                startDate = dateFormat.format(calendar.apply {
//                    set(Calendar.HOUR_OF_DAY, 0)
//                    set(Calendar.MINUTE, 0)
//                    set(Calendar.SECOND, 0)
//                }.time)
//
//                endDate = dateFormat.format(calendar.apply {
//                    set(Calendar.HOUR_OF_DAY, 23)
//                    set(Calendar.MINUTE, 59)
//                    set(Calendar.SECOND, 59)
//                }.time)
//            }

            "monthly" -> {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time)

                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                endDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time)
            }

            "yearly" -> {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                calendar.set(Calendar.MONTH, 0) // January
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time)

                calendar.set(Calendar.MONTH, 11) // December
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                endDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time)
            }

            else -> {
                // No filtering, fetch all orders
                startDate = null
                endDate = null
            }
        }

        val orderQuery = if (startDate != null && endDate != null) {
            query.whereGreaterThanOrEqualTo("orderDate", startDate)
                .whereLessThanOrEqualTo("orderDate", endDate)
        } else {
            query // No filtering
        }

        // Create a map to store sales data by month
        val monthlySales = mutableMapOf<String, Double>()


        // firestore.collection("AllOrders")
        orderQuery.get()
            .addOnSuccessListener { documents ->
                hideProgressBar()
                if (documents.isEmpty) {
                    showNoData()
                } else {


                    val totalNumberOfOrders = documents.size()

                    val allProductOrders = mutableListOf<ProductOrder>()
                    val allOrders = mutableListOf<Order>()
                    var totalSales = 0.0
                    var totalTax = 0.0
                    var totalDiscount = 0.0
                    var netSales = 0.0
                    var totalProfit = 0.0
                    var totalLoss = 0.0
                    var subTotalSales = 0.0
                    var totalQuantityOfProducts = 0

                    // Assuming you have fetched products into a map for easy access
                    // productMap = mutableMapOf<String, Product>() // Replace with actual fetching logic

                    for (document in documents) {
                        val orderData = document.data

                        val productsList =
                            (orderData["products"] as List<Map<String, Any>>).map { productData ->
                                ProductOrder(
                                    productId = productData["productId"] as String,
                                    productName = productData["productName"] as String,
                                    productWeight = (productData["productWeight"] as Number).toDouble(),
                                    quantity = (productData["quantity"] as Number).toInt(),
                                    productPrice = (productData["productPrice"] as Number).toDouble()
                                )
                            }

                        val order = Order(
                            orderId = document.id,
                            orderDate = orderData["orderDate"] as? String ?: "",
                            orderTime = orderData["orderTime"] as? String ?: "",
                            orderType = orderData["orderType"] as? String ?: "",
                            orderStatus = orderData["orderStatus"] as? String ?: "",
                            paymentMethod = orderData["paymentMethod"] as? String ?: "",
                            customerName = orderData["customerName"] as? String ?: "",
                            supplierName = orderData["supplierName"] as? String ?: "",
                            tax = (orderData["tax"] as? Number)?.toDouble() ?: 0.0,
                            discount = orderData["discount"] as? String ?: "",
                            products = productsList,
                            totalPrice = (orderData["totalPrice"] as? Number)?.toDouble() ?: 0.0,
                            totalPaidAmount = (orderData["totalPaidAmount"] as? Number)?.toDouble()
                                ?: 0.0,
                            remainingAmount = (orderData["remainingAmount"] as? Number)?.toDouble()
                                ?: 0.0,
                            remainingAmtPaidDate = orderData["remainingAmtPaidDate"] as? String
                                ?: "",
                            remainingAmtPaidTime = orderData["remainingAmtPaidTime"] as? String
                                ?: "",
                            updatedTotalPaidAmount = (orderData["updatedTotalPaidAmount"] as? Number)?.toDouble()
                                ?: 0.0,
                            updatedRemainingAmount = (orderData["updatedRemainingAmount"] as? Number)?.toDouble()
                                ?: 0.0
                        )


                        // Add all products to the main list
                        allProductOrders.addAll(productsList)
                        allOrders.add(order)

                        // Calculate totals
                        totalSales += order.totalPrice
                        totalTax += order.tax

                        val discountValue = order.discount.toDoubleOrNull() ?: 0.0
                        totalDiscount += discountValue

                        // Calculate subTotalSales
                        //  val subPrice = order.totalPrice - discountValue + order.tax
                        val subPrice = (order.totalPrice + discountValue) - order.tax
                        subTotalSales += subPrice

                        // Net sales
                        netSales += order.totalPrice

                        // Calculate totals for the current order
                        var orderSellingPrice = 0.0
                        var orderCostPrice = 0.0
                        var discount = order.discount.toDoubleOrNull() ?: 0.0


                        for (productOrder in productsList) {
                            val product = productMap[productOrder.productId]

                            if (product != null) {
                                // Cost price and selling price calculations
                                val costPrice = product.buyPrice * productOrder.quantity
                                val sellingPrice = productOrder.productPrice * productOrder.quantity

                                orderCostPrice += costPrice
                                orderSellingPrice += sellingPrice
                            }
                        }

                        // Apply discount to total selling price
                        orderSellingPrice -= discount


                        // Calculate profit and loss for the order
                        val profit = orderSellingPrice - orderCostPrice
                        totalProfit += profit


//                        // Calculate profit and loss based on products' buy prices
//                        for (productOrder in productsList) {
//                            val product =
//                                productMap[productOrder.productId] // Ensure productMap is populated
//                            if (product != null) {
//                                val buyCost = product.buyPrice * productOrder.quantity
//                                val sellCost = productOrder.productPrice * productOrder.quantity
//                                totalProfit += (sellCost - buyCost)  // - order.updatedRemainingAmount
//
//                                //totalProfit += (productOrder.productPrice - cost) * productOrder.quantity
//                            }
//                        }

                        // Total loss if there's any unpaid amount
                        totalLoss += if (order.updatedRemainingAmount > 0) order.updatedRemainingAmount else 0.0

                        for (productOrder in productsList) {
                            totalQuantityOfProducts += productOrder.quantity // Add quantity of each product
                        }


                    }
                    // After fetching all orders, set up the chart


                    // Now you have all totals calculated

                    // setupAdapter(allProductOrders)

                    displayTotals(
                        subTotalSales,
                        totalTax,
                        totalDiscount,
                        netSales,
                        totalProfit,
                        totalLoss,
                        totalNumberOfOrders,
                        totalQuantityOfProducts
                    )
                }


            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(this, R.string.no_data_found, Toast.LENGTH_SHORT).show()
                showNoData()
            }
    }


    private fun displayTotals(
        totalSales: Double,
        totalTax: Double,
        totalDiscount: Double,
        netSales: Double,
        totalProfit: Double,
        totalLoss: Double,
        totalOrders: Int,
        totalProductsSold: Int,
    ) {


        binding.txtTotalOrders.text = getString(R.string.total_orders) + " " + totalOrders
        binding.txtTotalProducts.text =
            getString(R.string.total_products_qty) + " " + totalProductsSold

        // Update your UI with the calculated totals
        // Example:
        binding.txtTotalPrice.text =
            getString(R.string.total_sales) + " " + getString(R.string.currency_symbol) + totalSales
        binding.txtTotalTax.text =
            getString(R.string.total_tax) + " " + getString(R.string.currency_symbol) + totalTax.toInt()
        binding.txtTotalDiscount.text =
            getString(R.string.total_discount) + " " + getString(R.string.currency_symbol) + totalDiscount
        binding.txtNetSales.text =
            getString(R.string.net_sales) + " " + getString(R.string.currency_symbol) + netSales.toInt()

        // Check if totalProfit is greater than 0
//        if (totalProfit > 0) {
//            binding.txtProfit.text =
//                getString(R.string.profit) + " " + getString(R.string.currency_symbol) + totalProfit.toInt()
//            // Set background color to green or any desired color for profit
//            binding.txtProfit.setBackgroundColor(getColor(R.color.green))
//
//
//        } else {
//            binding.txtProfit.text =
//                getString(R.string.loss) + " " + getString(R.string.currency_symbol) + totalProfit.toInt()
//            // Set background color to red for loss
//            binding.txtProfit.setBackgroundColor(getColor(R.color.red))
//
//
//        }

// Check if totalLoss is greater than 0
        if (totalLoss.toInt() > 0) {
            binding.txtRemaining.text =
                getString(R.string.remaining) + " " + getString(R.string.currency_symbol) + totalLoss.toInt()
        } else {
            // If totalLoss is 0, null, or empty, hide the text view
            binding.txtRemaining.visibility = View.GONE
            binding.remainingCard.visibility = View.GONE
        }


    }


    private fun setupAdapter(products: List<ProductOrder>) {
        orderDetailsAdapter = SalesReportAdapter(this, products)
        binding.recycler.adapter = orderDetailsAdapter

        binding.recycler.visibility = View.VISIBLE
        binding.imageNoProduct.visibility = View.GONE
        binding.txtNoProducts.visibility = View.GONE
        binding.txtTotalPrice.visibility = View.VISIBLE
    }


    private fun setupViews() {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.setHasFixedSize(true)

        binding.imageNoProduct.visibility = View.GONE
        binding.txtNoProducts.visibility = View.GONE

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.all_sales)
        }
    }


    private fun showNoData() {
        binding.recycler.visibility = View.GONE
        binding.imageNoProduct.visibility = View.VISIBLE
        binding.imageNoProduct.setImageResource(R.drawable.not_found)
        binding.txtNoProducts.visibility = View.VISIBLE
        binding.txtTotalPrice.visibility = View.GONE
    }


    private fun showSortMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.all_sales_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_all_sales -> {
                    setToolbarTitle("All Sales Report")
                    fetchData()
                    fetchAndCalculateTotalProfit()
                    true
                }

//                R.id.menu_daily -> {
//                    setToolbarTitle("Daily Sales Report")
//                   // fetchData("daily")
//                    true
//                }

                R.id.menu_monthly -> {
                    setToolbarTitle("Monthly Sales Report")
                    fetchData("monthly")
                    fetchAndCalculateTotalProfit("monthly")
                    true
                }

                R.id.menu_yearly -> {
                    setToolbarTitle("Yearly Sales Report")
                    fetchData("yearly")
                    fetchAndCalculateTotalProfit("yearly")
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun setToolbarTitle(title: String) {

        binding.toolbarTitle.text = title

    }


}



