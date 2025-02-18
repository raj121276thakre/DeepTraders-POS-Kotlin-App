package com.example.deeptraderspos.orders.orderDetails

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.Constants
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.adapter.RemainingPaymentsAdapter
import com.example.deeptraderspos.databinding.ActivityOrderDetailsBinding
import com.example.deeptraderspos.internetConnection.InternetCheckActivity
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.ProductOrder
import com.example.deeptraderspos.models.RemainingPayment
import com.example.deeptraderspos.models.ShopInformation
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderDetailsActivity : InternetCheckActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var f: DecimalFormat

    // private lateinit var intentOrder: Order
    private lateinit var firebaseOrder: Order
    private var currency: String = ""
    private var isSupplier: Boolean = false

    private var shopInfo: ShopInformation = ShopInformation()


    private lateinit var orderId: String
    private lateinit var orderDate: String
    private lateinit var orderTime: String
    private lateinit var orderType: String
    private lateinit var orderStatus: String
    private lateinit var paymentMethod: String
    private lateinit var customerName: String
    private lateinit var supplierName: String
    private var tax: Double = 0.0
    private lateinit var discount: String
    private var totalPrice: Double = 0.0
    private var totalPaidAmount: Double = 0.0
    private var remainingAmount: Double = 0.0
    private lateinit var remainingAmtPaidDate: String
    private lateinit var remainingAmtPaidTime: String
    private var updatedRemainingAmount: Double = 0.0
    private var updatedTotalPaidAmount: Double = 0.0
    private lateinit var products: ArrayList<ProductOrder>
    private lateinit var remainingPayments: MutableList<RemainingPayment>


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
        // Set status bar color
        Utils.setStatusBarColor(this)

        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }

        fetchShopInfo()


        // Retrieve each field from the Intent
        orderId = intent.getStringExtra("orderId") ?: ""
        orderDate = intent.getStringExtra("orderDate") ?: ""
        orderTime = intent.getStringExtra("orderTime") ?: ""
        orderType = intent.getStringExtra("orderType") ?: ""
        orderStatus = intent.getStringExtra("orderStatus") ?: ""
        paymentMethod = intent.getStringExtra("paymentMethod") ?: ""
        customerName = intent.getStringExtra("customerName") ?: ""
        supplierName = intent.getStringExtra("supplierName") ?: ""
        tax = intent.getDoubleExtra("tax", 0.0)
        discount = intent.getStringExtra("discount") ?: ""
        totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
        totalPaidAmount = intent.getDoubleExtra("totalPaidAmount", 0.0)
        remainingAmount = intent.getDoubleExtra("remainingAmount", 0.0)
        remainingAmtPaidDate = intent.getStringExtra("remainingAmtPaidDate") ?: ""
        remainingAmtPaidTime = intent.getStringExtra("remainingAmtPaidTime") ?: ""
        updatedRemainingAmount = intent.getDoubleExtra("updatedRemainingAmount", 0.0)
        updatedTotalPaidAmount = intent.getDoubleExtra("updatedTotalPaidAmount", 0.0)

        // Retrieve the Parcelable lists
        products = intent.getParcelableArrayListExtra("products") ?: arrayListOf()
        remainingPayments = intent.getParcelableArrayListExtra("remainingPayments") ?: arrayListOf()
        isSupplier =
            intent.getBooleanExtra("isSupplier", false) // Default is false (customer) if not found

        fetchOrdersByOrderId(isSupplier)


        // Initialize DecimalFormat
        f = DecimalFormat("#0.00")

        val name = if (isSupplier) {
            supplierName
        } else {
            customerName
        }

        setToolbarDetails(name, orderId)


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
        val productList: List<ProductOrder> = products
        if (productList.isEmpty()) {
            Toast.makeText(this, R.string.no_data_found, Toast.LENGTH_SHORT).show()
        } else {
            orderDetailsAdapter = OrderDetailsAdapter(this, productList)
            binding.recycler.adapter = orderDetailsAdapter


            // Calculate total price of all products
            val calculatedSubTotalPrice = productList.sumOf { it.productPrice * it.quantity }
            binding.txtSubtotalPrice.text =
                getString(R.string.sub_total) + " " + getString(R.string.currency_symbol) + f.format(
                    calculatedSubTotalPrice
                )
        }


        // setOrderDetails(order)


        binding.btnPdfReceipt.setOnClickListener {
            // Handle PDF generation logic
            createPdf(this, name)
        }

        binding.btnPayRemaining.setOnClickListener {
            showDialogAndUpdateToFirestore(isSupplier)
        }


    }


    private fun updateOrderStatus(orderId: String, status: String) {

        // Prepare the data to update
        val updateData = mapOf(
            "orderStatus" to status,
        )

        val orderRef = firestore.collection(if (isSupplier) "AllOrdersSuppliers" else "AllOrders")

        orderRef.document(orderId).update(updateData)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }


    private fun showDialogAndUpdateToFirestore(isSupplier: Boolean) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Enter Payment Amount")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        alertDialog.setView(input)

        alertDialog.setPositiveButton("Pay") { dialog, _ ->
            val userPaidAmount = input.text.toString().toDoubleOrNull() ?: 0.0

            // Validate input amount
            if (userPaidAmount <= 0.0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
            val currentTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())

            // Calculate the new remaining amount
            val newRemainingAmount = firebaseOrder.updatedRemainingAmount - userPaidAmount

            val newTotalPaidAmount = firebaseOrder.updatedTotalPaidAmount + userPaidAmount

            // Logging values for debugging
            Log.d(
                "PaymentCheck",
                "User paid: $userPaidAmount, Previous Remaining Amount: ${firebaseOrder.updatedRemainingAmount}, New Remaining Amount: $newRemainingAmount"
            )

            // Check if the user is attempting to pay more than the remaining amount
            if (newRemainingAmount < 0) {
                Toast.makeText(this, "Paid amount exceeds remaining balance", Toast.LENGTH_SHORT)
                    .show()
                return@setPositiveButton
            }

            // Create a new RemainingPayment object
            val newPayment = RemainingPayment(
                paidAmount = userPaidAmount,
                paidDate = currentDate,
                paidTime = currentTime,
                remainingAmount = newRemainingAmount // Remaining after this payment
            )


            if (newRemainingAmount.toInt() == 0) {
                updateOrderStatus(orderId = orderId, Constants.COMPLETED)
            }

            // Create a mutable list to hold existing payments and add the new payment
            val updatedPayments = remainingPayments.toMutableList()
            updatedPayments.add(newPayment)

            // Prepare the data to update Firestore with the new payment and updated remaining amount
            val updateData = mapOf(
                "remainingPayments" to updatedPayments.map {
                    mapOf(
                        "paidAmount" to it.paidAmount,
                        "paidDate" to it.paidDate,
                        "paidTime" to it.paidTime,
                        "remainingAmount" to it.remainingAmount // Include remaining amount here
                    )
                },
                "updatedRemainingAmount" to newRemainingAmount, // Update this to reflect the new total
                "updatedTotalPaidAmount" to newTotalPaidAmount // Update this to reflect the new total
            )
            val collection = if (isSupplier) "AllOrdersSuppliers" else "AllOrders"
            // Update Firestore with the new payment data
            //firestore.collection("AllOrders").document(order.orderId).update(updateData)
            firestore.collection(collection).document(orderId).update(updateData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Payment recorded successfully", Toast.LENGTH_SHORT).show()
                    // Update local order details
                    remainingPayments =
                        updatedPayments // Update the local order's payment list
                    updatedRemainingAmount =
                        newRemainingAmount // Update the local order's remaining amount
                    updatedTotalPaidAmount =
                        newTotalPaidAmount // Update the local order's remaining amount
                    fetchOrdersByOrderId(isSupplier) // Refresh orders from Firestore if needed
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to record payment", Toast.LENGTH_SHORT).show()
                }

            dialog.dismiss()
        }

        alertDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        alertDialog.show()
    }


    private fun fetchOrdersByOrderId(isSupplier: Boolean) {
        val collection = if (isSupplier) "AllOrdersSuppliers" else "AllOrders"

//        firestore.collection("AllOrders")
        firestore.collection(collection)
            .whereEqualTo(
                "orderId",
                orderId
            ) // Adjust this field name based on your Firestore structure
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document =
                        documents.documents[0] // Assuming orderId is unique and will return one document

                    // Extract data from the document
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
                    val updatedRemainingAmount = document.getDouble("updatedRemainingAmount") ?: 0.0
                    val updatedTotalPaidAmount = document.getDouble("updatedTotalPaidAmount") ?: 0.0

                    // Fetch remaining payments
                    val remainingPayments =
                        document.get("remainingPayments") as? List<Map<String, Any>> ?: emptyList()
                    val paymentsList = remainingPayments.map { payment ->
                        RemainingPayment(
                            paidAmount = (payment["paidAmount"] as? Number)?.toDouble() ?: 0.0,
                            paidDate = payment["paidDate"] as? String,
                            paidTime = payment["paidTime"] as? String,
                            remainingAmount = (payment["remainingAmount"] as? Number)?.toDouble()
                                ?: 0.0
                        )
                    }


                    // Sort payments by date and time (latest on top)
                    val sortedPaymentsList = paymentsList.sortedByDescending { payment ->
                        val dateTimeString =
                            "${payment.paidDate} ${payment.paidTime}" // Combine date and time
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(
                            dateTimeString
                        )
                    }


                    // Create the local Order object
                    firebaseOrder = Order(
                        orderId = orderId,
                        orderDate = orderDate,
                        orderTime = orderTime,
                        orderType = orderType,
                        orderStatus = orderStatus,
                        paymentMethod = paymentMethod,
                        customerName = customerName,
                        supplierName = supplierName,
                        tax = tax,
                        discount = discount,
                        totalPrice = totalPrice,
                        totalPaidAmount = totalPaidAmount,
                        remainingAmount = remainingAmount, // Original field remains unchanged
                        remainingAmtPaidDate = remainingAmtPaidDate,
                        remainingAmtPaidTime = remainingAmtPaidTime,
                        remainingPayments = sortedPaymentsList, // List of RemainingPayment objects
                        updatedRemainingAmount = updatedRemainingAmount,// New field for updated remaining amount
                        updatedTotalPaidAmount = updatedTotalPaidAmount // New field for updated remaining amount
                    )

                    // Now you can set the order details
                    setOrderDetails(firebaseOrder)

                } else {
                    Toast.makeText(
                        this,
                        "No order found with the given ID. ${orderId}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching order: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }

    }


    private fun setOrderDetails(order: Order) {
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


        val totalPaid = order.updatedTotalPaidAmount
        binding.txtTotalPaid.text =
            getString(R.string.total_paid) + currency + f.format(totalPaid)


        val totalRemaining = order.updatedRemainingAmount
        binding.txtTotalRemaining.text =
            getString(R.string.total_remaining) + currency + f.format(totalRemaining)

        if (firebaseOrder.orderStatus == Constants.COMPLETED) {
            binding.btnPayRemaining.visibility = View.GONE
            binding.txtOrderStatus.setTextColor(getColor(R.color.green))


        } else {
            binding.btnPayRemaining.visibility = View.VISIBLE
        }
        binding.txtOrderStatus.text =
            getString(R.string.order_status) + " " + "${firebaseOrder.orderStatus}"


//        val totalCalculedRemaining =
//            totalPrice - totalPaid // Assuming totalPrice is calculated as shown earlier

//        if (!order.remainingAmtPaidDate.isNullOrEmpty() && totalRemaining == 0.0) {
//            binding.txtRemainingPaidDateTime.visibility = View.VISIBLE
//            binding.txtRemainingPaidDateTime.text =
//                "The Remaining Amount " + currency + (totalCalculedRemaining) + " is paid at " + order.orderTime + " " + order.orderDate
//        } else {
//            binding.txtRemainingPaidDateTime.visibility = View.GONE
//        }


        // remaining recyclerview

        val recyclerView: RecyclerView =
            findViewById(R.id.recyclerView) // Assuming you have a RecyclerView in your layout
        recyclerView.layoutManager = LinearLayoutManager(this)

        val name = if (isSupplier) {
            order.supplierName
        } else {
            order.customerName
        }
        // Pass the remainingPayments list to the adapter
        val adapter = RemainingPaymentsAdapter(order.remainingPayments, "₹") { payment ->
            // Handle the download PDF click here
            createPdfForPayment(this, order, name, payment)
        }

        recyclerView.adapter = adapter

    }


    private fun createPdfForPayment(
        context: Context,
        order: Order,
        name: String,
        payment: RemainingPayment
    ) {
        val directoryPath =
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
        if (directoryPath == null) {
            Toast.makeText(context, "Unable to access storage", Toast.LENGTH_SHORT).show()
            return
        }

        val filePath =
            "$directoryPath/Invoice_${order.orderId}.pdf" // Use a unique identifier from the order
        val file = File(filePath)

        try {
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)

            // Set margins
            document.setMargins(20f, 20f, 20f, 20f)

            // Retrieve the custom color from resources
            val customBlue = ContextCompat.getColor(this, R.color.pdf_Background_Color)

            // Convert it to RGB for use with iText
            val red = Color.red(customBlue)
            val green = Color.green(customBlue)
            val blue = Color.blue(customBlue)
            val customPdfColor = DeviceRgb(red, green, blue)

            // Header - Company Information with Blue Background
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(4f, 1f)))
                .useAllAvailableWidth()
                .setBackgroundColor(customPdfColor)
                .setFontColor(ColorConstants.BLACK)

            headerTable.addCell(
                Cell().add(Paragraph(shopInfo.shopName)) // shop name
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(20f)
                    .setBold()
                    .setBorder(Border.NO_BORDER)
            )

            headerTable.addCell(
                Cell().add(Paragraph("Invoice"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(24f)
                    .setBold()
                    .setBorder(Border.NO_BORDER)
            )

            document.add(headerTable)

            // Sub-header - Contact Information
            val subHeaderTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 1f, 1f)))
                .useAllAvailableWidth()
                .setMarginBottom(10f)

            subHeaderTable.addCell(
                Cell(1, 2).add(
                    Paragraph(
                        """
            |Name: Dipak Shinde
            |Address: Market Yard, Satara 415002
            |Phone: +917972504022 / 9270004942
            """.trimMargin()
                    )
                )
                    .setBorder(Border.NO_BORDER)
            )

            subHeaderTable.addCell(
                Cell().add(Paragraph("Invoice No: ${order.orderId}\nOrder created Date: ${order.orderDate}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER)
            )

            subHeaderTable.addCell(
                Cell().add(Paragraph("Payment Date: ${payment.paidDate}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER)
            )

            document.add(subHeaderTable)

            // Bill To Section
            val billToTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
                .useAllAvailableWidth()
                .setMarginBottom(10f)

            billToTable.addCell(
                Cell().add(Paragraph("Bill To\n${name}")) // Assuming customerName is in Order
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(14f)
                    .setBold()
            )

            document.add(billToTable)

            // Item Table Header
            val itemTable = Table(UnitValue.createPercentArray(floatArrayOf(4f, 2f, 2f, 2f, 2f)))
                .useAllAvailableWidth()
                .setBackgroundColor(customPdfColor)
                .setFontColor(ColorConstants.BLACK)
                .setMarginBottom(10f)


            itemTable.addHeaderCell("Item Name")
            itemTable.addHeaderCell("Quantity")
            itemTable.addHeaderCell("Weight")
            itemTable.addHeaderCell("Price/Unit")
            itemTable.addHeaderCell("Amount")

            // Calculate total price
            var subTotalPrice = 0.0

            // Add item rows from order.products
            products.forEach { product ->

                val amount = product.quantity * product.productPrice
                subTotalPrice += amount // Accumulate total price

                itemTable.addCell(product.productName) // Assuming ProductOrder has name
                itemTable.addCell(product.quantity.toString())
                // itemTable.addCell("Box") // Assuming unit is Box for simplicity
                itemTable.addCell(product.productWeight.toString()) // Assuming unit is Box for simplicity
                itemTable.addCell(product.productPrice.toString()) // Assuming price is a property in ProductOrder
                itemTable.addCell((product.quantity * product.productPrice).toString()) // Calculate amount
            }

            // Add Total Row
            itemTable.addCell(
                Cell(1, 4).add(Paragraph("Sub Total"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold()
            )
            itemTable.addCell(Cell().add(Paragraph(subTotalPrice.toString()))) // Use calculated totalPrice

            // itemTable.addCell(Cell().add(Paragraph(order.totalPrice.toString()))) // Assuming totalPrice is a property in Order

            document.add(itemTable)

            // Footer - Payment Details
            val footerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
                .useAllAvailableWidth()
                .setMarginTop(20f)

            footerTable.addCell(
                Cell().add(Paragraph("Pay To:"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(
                    Paragraph(
                        """
            |Bank Name: The Satara District Central Co Operative Bank
            |Account No: 01197026000288
            |Bank IFSC code: SDC0001197
            |Account Holder's Name: Pawar Udyog Samuh
            """.trimMargin()
                    )
                )
                    .setBorder(Border.NO_BORDER)
            )

            footerTable.addCell(
                Cell().add(Paragraph("Sub Total Amount : $subTotalPrice"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(Paragraph("Total tax(${shopInfo.taxPercentage}%) : ${order.tax}"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(Paragraph("Discount : ${order.discount}"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(Paragraph("Total Amount : ${order.totalPrice}"))
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(16f)
                    .setBold()
            )





            footerTable.addCell(
                Cell().add(Paragraph("Paid Amount : ${payment.paidAmount}"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(Paragraph("Total Remaining  Amount : ${payment.remainingAmount}"))
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(16f)
                    .setBold()
            )

            val status =
                if (payment.remainingAmount.toInt() == 0) {
                    Constants.COMPLETED
                } else {
                    Constants.PENDING

                }

            footerTable.addCell(
                Cell().add(Paragraph("Order Status : $status"))
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(16f)
                    .setBold()
            )

            // Add any additional footer details here...

            document.add(footerTable)

            document.close()
            Toast.makeText(context, "Bill Pdf Created.", Toast.LENGTH_LONG).show()

            openPdfPreview(this, file)


        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    //original bill pdf
    private fun createPdf(context: Context, name: String) {
        val directoryPath =
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
        if (directoryPath == null) {
            Toast.makeText(context, "Unable to access storage", Toast.LENGTH_SHORT).show()
            return
        }

        val filePath =
            "$directoryPath/Invoice_${orderId}.pdf" // Use a unique identifier from the order
        val file = File(filePath)

        try {
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)

            // Retrieve the custom color from resources
            val customBlue = ContextCompat.getColor(this, R.color.pdf_Background_Color)

            // Convert it to RGB for use with iText
            val red = Color.red(customBlue)
            val green = Color.green(customBlue)
            val blue = Color.blue(customBlue)
            val customPdfColor = DeviceRgb(red, green, blue)

            // Set margins
            document.setMargins(20f, 20f, 20f, 20f)

            // Header - Company Information with Blue Background
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(4f, 1f)))
                .useAllAvailableWidth()
                .setBackgroundColor(customPdfColor)
                .setFontColor(ColorConstants.BLACK)

            headerTable.addCell(
                Cell().add(Paragraph(shopInfo.shopName)) // shop name
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(16f)
                    .setBorder(Border.NO_BORDER)
            )

            headerTable.addCell(
                Cell().add(Paragraph("INVOICE"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(24f)
                    .setBold()
                    .setBorder(Border.NO_BORDER)
            )

            document.add(headerTable)

            // Sub-header - Contact Information
            val subHeaderTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 1f, 1f)))
                .useAllAvailableWidth()
                .setMarginBottom(10f)

            subHeaderTable.addCell(
                Cell(1, 2).add(
                    Paragraph(
                        """
            |Name: Dipak Shinde
            |Address: Market Yard, Satara 415002
            |Phone: +917972504022 / 9270004942
            """.trimMargin()
                    )
                )
                    .setBorder(Border.NO_BORDER)
            )

            subHeaderTable.addCell(
                Cell().add(Paragraph("Invoice No: ${orderId}\nDate: ${orderDate}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER)
            )

            document.add(subHeaderTable)

            // Bill To Section
            val billToTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
                .useAllAvailableWidth()
                .setMarginBottom(10f)

            billToTable.addCell(
                Cell().add(Paragraph("Bill To\n${name}")) // Assuming customerName is in Order
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(14f)
                    .setBold()
            )

            document.add(billToTable)

            // Item Table Header
            val itemTable = Table(UnitValue.createPercentArray(floatArrayOf(4f, 2f, 2f, 2f, 2f)))
                .useAllAvailableWidth()
                .setBackgroundColor(customPdfColor)
                .setFontColor(ColorConstants.BLACK)
                .setMarginBottom(10f)


            itemTable.addHeaderCell("Item Name")
            itemTable.addHeaderCell("Quantity")
            itemTable.addHeaderCell("Weight")
            itemTable.addHeaderCell("Price/Unit")
            itemTable.addHeaderCell("Amount")

            // Calculate total price
            var subTotalPrice = 0.0

            // Add item rows from order.products
            products.forEach { product ->

                val amount = product.quantity * product.productPrice
                subTotalPrice += amount // Accumulate total price

                itemTable.addCell(product.productName) // Assuming ProductOrder has name
                itemTable.addCell(product.quantity.toString())
                // itemTable.addCell("Box") // Assuming unit is Box for simplicity
                itemTable.addCell(product.productWeight.toString()) // Assuming unit is Box for simplicity
                itemTable.addCell(product.productPrice.toString()) // Assuming price is a property in ProductOrder
                itemTable.addCell((product.quantity * product.productPrice).toString()) // Calculate amount
            }

            // Add Total Row
            itemTable.addCell(
                Cell(1, 4).add(Paragraph("Sub Total"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold()
            )
            itemTable.addCell(Cell().add(Paragraph(subTotalPrice.toString()))) // Use calculated totalPrice

            // itemTable.addCell(Cell().add(Paragraph(order.totalPrice.toString()))) // Assuming totalPrice is a property in Order

            document.add(itemTable)

            // Footer - Payment Details
            val footerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
                .useAllAvailableWidth()
                .setMarginTop(20f)

            footerTable.addCell(
                Cell().add(Paragraph("Pay To:"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(
                    Paragraph(
                        """
            |Bank Name: The Satara District Central Co Operative Bank
            |Account No: 01197026000288
            |Bank IFSC code: SDC0001197
            |Account Holder's Name: Pawar Udyog Samuh
            """.trimMargin()
                    )
                )
                    .setBorder(Border.NO_BORDER)
            )

            footerTable.addCell(
                Cell().add(Paragraph("Sub Total Amount : $subTotalPrice"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(Paragraph("Total tax(${shopInfo.taxPercentage}%) : ${tax}"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(Paragraph("Discount : ${discount}"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            footerTable.addCell(
                Cell().add(Paragraph("Total Amount : ${totalPrice}"))
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(16f)
                    .setBold()
            )



            if (orderStatus == Constants.PENDING) {

                footerTable.addCell(
                    Cell().add(Paragraph("Total paid : ${totalPaidAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setBold()
                )

                footerTable.addCell(
                    Cell().add(Paragraph("Total Remaining  Amount : ${remainingAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setFontSize(16f)
                        .setBold()
                )

            } else {

                footerTable.addCell(
                    Cell().add(Paragraph("Total paid : ${totalPaidAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setBold()
                )

                val totalCalculedRemaining = (totalPrice - totalPaidAmount).toDouble()

                footerTable.addCell(
                    Cell().add(Paragraph("The Remaining Amount " + currency + (totalCalculedRemaining) + " is paid at " + orderTime + " " + orderDate))
                        .setBorder(Border.NO_BORDER)
                        .setFontSize(16f)
                        .setBold()
                )

            }



            footerTable.addCell(
                Cell().add(Paragraph("Order Status : ${orderStatus}"))
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(16f)
                    .setBold()
            )

            // Add any additional footer details here...

            document.add(footerTable)

            document.close()
            Toast.makeText(context, "Bill Pdf Created.", Toast.LENGTH_LONG).show()

            openPdfPreview(this, file)


        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun openPdfPreview(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Check if there's an app to handle the intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No application found to view PDF", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setToolbarDetails(customerName: String, orderId: String) {
        binding.txtCustomerName.text = customerName
        binding.txtOrderId.text = "Order ID :#$orderId"

    }

    // not used will be use in future

    private fun fetchShopInfo() {
        firestore.collection("shops").document("shopInfo")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Create an instance of ShopInformation from the fetched data

                    val shopTaxString = document.getString("shopTax") ?: ""
                    shopInfo = ShopInformation(
                        shopName = document.getString("shopName") ?: "",
                        contactNumber = document.getString("shopContact") ?: "",
                        email = document.getString("shopEmail") ?: "",
                        address = document.getString("shopAddress") ?: "",
                        currencySymbol = document.getString("shopCurrency") ?: "",
                        taxPercentage = shopTaxString.toDouble(),
                        id = document.id // Optional ID field
                    )

                }
            }
            .addOnFailureListener { e ->
                // Handle failure, show error message
                Toast.makeText(
                    this,
                    "Failed to fetch shop information: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}