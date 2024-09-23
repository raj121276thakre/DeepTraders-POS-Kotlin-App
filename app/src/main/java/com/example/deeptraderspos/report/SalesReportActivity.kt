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
    private fun fetchData() {
        fetchAllProducts { productMap ->
            fetchAllOrdersData(productMap)
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

    private fun fetchAllOrdersData(productMap: Map<String, Product>) {
        firestore.collection("AllOrders")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    showNoData()
                } else {
                    val allProductOrders = mutableListOf<ProductOrder>()
                    val allOrders = mutableListOf<Order>()
                    var totalSales = 0.0
                    var totalTax = 0.0
                    var totalDiscount = 0.0
                    var netSales = 0.0
                    var totalProfit = 0.0
                    var totalLoss = 0.0
                    var subTotalSales = 0.0

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
                    }

                    // Now you have all totals calculated
                    setupAdapter(allProductOrders)
                    displayTotals(
                        subTotalSales,
                        totalTax,
                        totalDiscount,
                        netSales,
                        totalProfit,
                        totalLoss
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
        totalLoss: Double
    ) {
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

        binding.txtProfit.text =
            getString(R.string.profit) + " " + getString(R.string.currency_symbol) + totalProfit
        binding.txtLoss.text =
            getString(R.string.loss) + " " + getString(R.string.currency_symbol) + totalLoss
        // Add more UI updates as needed for profit and loss
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
                    fetchData()
                    true
                }

                R.id.menu_daily -> {
                   // fetchData("daily")
                    true
                }

                R.id.menu_monthly -> {
                   // fetchData("monthly")
                    true
                }

                R.id.menu_yearly -> {
                   // fetchData("yearly")
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }


    private fun getReport(period: String) {
        Toast.makeText(this, period, Toast.LENGTH_SHORT).show()
    }


}



