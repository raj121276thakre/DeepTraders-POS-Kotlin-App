package com.example.deeptraderspos.orders

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.ActivityOrderDetailsBinding
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.ProductOrder
import java.text.DecimalFormat

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter

    private var bm: Bitmap? = null
    private lateinit var f: DecimalFormat
    private lateinit var order: Order
    private var currency: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderDetails)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Retrieve the order from the intent
        order = intent.getParcelableExtra<Order>("order") ?: return

        // Initialize DecimalFormat
        f = DecimalFormat("#0.00")

        // Set up action bar
        supportActionBar?.apply {
            setHomeButtonEnabled(true) // Enable back button
            setDisplayHomeAsUpEnabled(true) // Show back button
            setTitle(R.string.order_details) // Set title
        }

        // Set up RecyclerView with adapter
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailsActivity)
            setHasFixedSize(true)
        }

        // Retrieve products from order and pass to the adapter
        val productList: List<ProductOrder> = order.products
        if (productList.isEmpty()) {
            Toast.makeText(this, R.string.no_data_found, Toast.LENGTH_SHORT).show()
        } else {
            orderDetailsAdapter = OrderDetailsAdapter(this, productList)
            binding.recycler.adapter = orderDetailsAdapter


            // Calculate total price of all products
            val calculatedSubTotalPrice = productList.sumOf { it.productPrice * it.quantity }
           binding.txtSubtotalPrice.text = getString(R.string.sub_total) +" "+getString(R.string.currency_symbol)+ f.format(calculatedSubTotalPrice)
        }

        // Get tax, discount, and currency
        val tax = (order.tax).toDouble() ?: 0.0
        val discount = order.discount.toDoubleOrNull() ?: 0.0
        currency = getString(R.string.currency_symbol)

        // Display tax, discount, total price
        binding.txtTax.text = getString(R.string.total_tax) + " : " + currency + f.format(tax)
        binding.txtDiscount.text =
            getString(R.string.discount) + " : " + currency + f.format(discount)

        val totalPrice = order.totalPrice
        binding.txtTotalCost.text =
            getString(R.string.total_price) + currency + f.format(totalPrice)

        //paid & remaining

        val totalPaid = order.totalPaidAmount
        binding.txtTotalPaid.text =
            getString(R.string.total_paid) + currency + f.format(totalPaid)

        val totalRemaining = order.remainingAmount
        binding.txtTotalRemaining.text =
            getString(R.string.total_remaining) + currency + f.format(totalRemaining)



        val totalCalculedRemaining = totalPrice - totalPaid // Assuming totalPrice is calculated as shown earlier

        if (!order.remainingAmtPaidDate.isNullOrEmpty() && totalRemaining == 0.0) {
            binding.txtRemainingPaidDateTime.visibility = View.VISIBLE
            binding.txtRemainingPaidDateTime.text = "The Remaining Amount " + currency + (totalCalculedRemaining) + " is paid at " + order.orderTime + " " + order.orderDate
        } else {
            binding.txtRemainingPaidDateTime.visibility = View.GONE
        }
        /*
        check if order.remainingAmtPaidDate != null or empty && totalRemaining == 0.00
        then  binding.txtRemainingPaidDateTime.visibility = View.VISIBLE

        binding.txtRemainingPaidDateTime.text =
            "The RemainingAmount" + currency + (totalPrice-totalPaid)+ "is paid at "+ order.orderTime + " " + order.orderDate

            else
            binding.txtRemainingPaidDateTime.visibility = View.GONE

         */



        // Set up button listeners
       setupButtonListeners()


    }


    // Set up button listeners for PDF receipt and thermal printer
    private fun setupButtonListeners() {
        binding.btnPdfReceipt.setOnClickListener {
            // Handle PDF generation logic
        }

    }


}