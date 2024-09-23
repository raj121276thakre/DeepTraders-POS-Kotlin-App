package com.example.deeptraderspos.report

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.adapter.SalesReportAdapter
import com.example.deeptraderspos.databinding.ActivitySalesReportBinding
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.Product
import com.example.deeptraderspos.models.ProductOrder
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SalesReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySalesReportBinding

    private lateinit var orderDetailsAdapter: SalesReportAdapter
    private lateinit var firestore: FirebaseFirestore


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

        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }
        firestore = FirebaseFirestore.getInstance()

        // Set up views
        setupViews()

        // Get data from Firestore
        fetchData()

        binding.sortSalesBtn.setOnClickListener {
            showSortMenu(binding.sortSalesBtn)
        }

    }

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

        val query = firestore.collection("AllOrders")

        // Apply filters based on the time frame
        val startDate: String?
        val endDate: String?

        when (timeFrame) {
            "daily" -> {
                // Get today's date in "yyyy-MM-dd" format
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                startDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time)

                endDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time)
            }

            "monthly" -> {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = dateFormat.format(calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time)

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
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

        // firestore.collection("AllOrders")
        orderQuery.get()
            .addOnSuccessListener { documents ->
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
                                ?: ""
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

                        // Calculate profit and loss based on products' buy prices
                        for (productOrder in productsList) {
                            val product =
                                productMap[productOrder.productId] // Ensure productMap is populated
                            if (product != null) {
                                val buyCost = product.buyPrice * productOrder.quantity
                                val sellCost = productOrder.productPrice * productOrder.quantity
                                totalProfit += (sellCost - buyCost) - order.remainingAmount

                                //totalProfit += (productOrder.productPrice - cost) * productOrder.quantity
                            }
                        }

                        // Total loss if there's any unpaid amount
                        totalLoss += if (order.remainingAmount > 0) order.remainingAmount else 0.0

                        for (productOrder in productsList) {
                            totalQuantityOfProducts += productOrder.quantity // Add quantity of each product
                        }
                    }

                    // Now you have all totals calculated
                    setupAdapter(allProductOrders)
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


        binding.txtTotalOrders.text = getString(R.string.total_orders) + " "  + totalOrders
        binding.txtTotalProducts.text = getString(R.string.total_products_qty) + " "  + totalProductsSold

        // Update your UI with the calculated totals
        // Example:
        binding.txtTotalPrice.text =
            getString(R.string.total_sales) + " " + getString(R.string.currency_symbol) + totalSales
        binding.txtTotalTax.text =
            getString(R.string.total_tax) + " " + getString(R.string.currency_symbol) + totalTax
        binding.txtTotalDiscount.text =
            getString(R.string.total_discount) + " " + getString(R.string.currency_symbol) + totalDiscount
        binding.txtNetSales.text =
            getString(R.string.net_sales) + " " + getString(R.string.currency_symbol) + netSales

        // Check if totalProfit is greater than 0
        if (totalProfit > 0) {
            binding.txtProfit.text = getString(R.string.profit) + " " + getString(R.string.currency_symbol) + totalProfit
            // Set background color to green or any desired color for profit
            binding.txtProfit.setBackgroundColor(getColor(R.color.green))
        } else {
            binding.txtProfit.text = getString(R.string.loss) + " " + getString(R.string.currency_symbol) + totalProfit
            // Set background color to red for loss
            binding.txtProfit.setBackgroundColor(getColor(R.color.red))
        }

// Check if totalLoss is greater than 0
        if (totalLoss.toInt() > 0) {
            binding.txtRemaining.text = getString(R.string.remaining) + " " + getString(R.string.currency_symbol) + totalLoss
        } else {
            // If totalLoss is 0, null, or empty, hide the text view
            binding.txtRemaining.visibility = View.GONE
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
                    true
                }

                R.id.menu_daily -> {
                    setToolbarTitle("Daily Sales Report")
                    fetchData("daily")
                    true
                }

                R.id.menu_monthly -> {
                    setToolbarTitle("Monthly Sales Report")
                    fetchData("monthly")
                    true
                }

                R.id.menu_yearly -> {
                    setToolbarTitle("Yearly Sales Report")
                    fetchData("yearly")
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



