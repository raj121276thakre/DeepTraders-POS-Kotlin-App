package com.example.deeptraderspos.orders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deeptraderspos.Constants
import com.example.deeptraderspos.R
import com.example.deeptraderspos.Utils
import com.example.deeptraderspos.databinding.ActivityOrderDetailsBinding
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.ProductOrder
import com.itextpdf.kernel.colors.ColorConstants
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

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter


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
        // Set status bar color
        Utils.setStatusBarColor(this)

        // Go Back Button
        val goBackBtn = binding.menuIcon
        goBackBtn.setOnClickListener {
            onBackPressed()  // This will take you back to the previous activity
        }


        // Retrieve the order from the intent
        order = intent.getParcelableExtra<Order>("order") ?: return
        val isSupplier = intent.getBooleanExtra("isSupplier", false) // Default is false (customer) if not found

        // Initialize DecimalFormat
        f = DecimalFormat("#0.00")

        val name = if (isSupplier) {
            order.supplierName
        } else {
              order.customerName
        }

        setToolbarDetails(name, order.orderId)

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
        }
        else {
            orderDetailsAdapter = OrderDetailsAdapter(this, productList)
            binding.recycler.adapter = orderDetailsAdapter


            // Calculate total price of all products
            val calculatedSubTotalPrice = productList.sumOf { it.productPrice * it.quantity }
            binding.txtSubtotalPrice.text =
                getString(R.string.sub_total) + " " + getString(R.string.currency_symbol) + f.format(
                    calculatedSubTotalPrice
                )
        }



        setOrderDetails()


        binding.btnPdfReceipt.setOnClickListener {
            // Handle PDF generation logic
            createPdf(this, order,name)
        }


    }

    private fun setOrderDetails() {
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


        val totalCalculedRemaining =
            totalPrice - totalPaid // Assuming totalPrice is calculated as shown earlier

        if (!order.remainingAmtPaidDate.isNullOrEmpty() && totalRemaining == 0.0) {
            binding.txtRemainingPaidDateTime.visibility = View.VISIBLE
            binding.txtRemainingPaidDateTime.text =
                "The Remaining Amount " + currency + (totalCalculedRemaining) + " is paid at " + order.orderTime + " " + order.orderDate
        } else {
            binding.txtRemainingPaidDateTime.visibility = View.GONE
        }

    }


    private fun setToolbarDetails(customerName: String, orderId: String) {
        binding.txtCustomerName.text = customerName
        binding.txtOrderId.text = "Order ID :#$orderId"

    }




    private fun createPdf(context: Context, order: Order, name: String) {
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

            // Header - Company Information with Blue Background
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(4f, 1f)))
                .useAllAvailableWidth()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)

            headerTable.addCell(
                Cell().add(Paragraph("BlueBird Industries")) // shop name
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
            |G no. 328/1 Kaloshi, Khandala
            |Rahimatpur road side, Satara 415002
            |Phone: +919775240422
            |Email: pawaraj87@gmail.com
            """.trimMargin()
                    )
                )
                    .setBorder(Border.NO_BORDER)
            )

            subHeaderTable.addCell(
                Cell().add(Paragraph("Invoice No: ${order.orderId}\nDate: ${order.orderDate}"))
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
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setMarginBottom(10f)


            itemTable.addHeaderCell("Item Name")
            itemTable.addHeaderCell("Quantity")
            itemTable.addHeaderCell("Weight")
            itemTable.addHeaderCell("Price/Unit")
            itemTable.addHeaderCell("Amount")

            // Calculate total price
            var subTotalPrice = 0.0

            // Add item rows from order.products
            order.products.forEach { product ->

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
                Cell().add(Paragraph("Total tax : ${order.tax}"))
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



            if (order.orderStatus == Constants.PENDING) {

                footerTable.addCell(
                    Cell().add(Paragraph("Total paid : ${order.totalPaidAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setBold()
                )

                footerTable.addCell(
                    Cell().add(Paragraph("Total Remaining  Amount : ${order.remainingAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setFontSize(16f)
                        .setBold()
                )

            } else {

                footerTable.addCell(
                    Cell().add(Paragraph("Total paid : ${order.totalPaidAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setBold()
                )

                val totalCalculedRemaining = (order.totalPrice - order.totalPaidAmount).toDouble()

                footerTable.addCell(
                    Cell().add(Paragraph("The Remaining Amount " + currency + (totalCalculedRemaining) + " is paid at " + order.orderTime + " " + order.orderDate))
                        .setBorder(Border.NO_BORDER)
                        .setFontSize(16f)
                        .setBold()
                )

            }



            footerTable.addCell(
                Cell().add(Paragraph("Order Status : ${order.orderStatus}"))
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(16f)
                    .setBold()
            )

            // Add any additional footer details here...

            document.add(footerTable)

            document.close()
            Toast.makeText(context, "Pdf Created: $filePath", Toast.LENGTH_LONG).show()

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


    /*
      private fun createPdf(context: Context, order: Order) {
        val directoryPath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
        if (directoryPath == null) {
            Toast.makeText(context, "Unable to access storage", Toast.LENGTH_SHORT).show()
            return
        }

        val filePath = "$directoryPath/Invoice_${order.orderId}.pdf" // Use a unique identifier from the order
        val file = File(filePath)

        try {
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)

            // Set margins
            document.setMargins(20f, 20f, 20f, 20f)

            // Header - Company Information with Blue Background
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(4f, 1f)))
                .useAllAvailableWidth()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)

            headerTable.addCell(
                Cell().add(Paragraph("BlueBird Industries")) // shop name
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
            |G no. 328/1 Kaloshi, Khandala
            |Rahimatpur road side, Satara 415002
            |Phone: +919775240422
            |Email: pawaraj87@gmail.com
            """.trimMargin()
                    )
                )
                    .setBorder(Border.NO_BORDER)
            )

            subHeaderTable.addCell(
                Cell().add(Paragraph("Invoice No: ${order.orderId}\nDate: ${order.orderDate}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER)
            )

            document.add(subHeaderTable)

            // Bill To Section
            val billToTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
                .useAllAvailableWidth()
                .setMarginBottom(10f)

            billToTable.addCell(
                Cell().add(Paragraph("Bill To\n${order.customerName}")) // Assuming customerName is in Order
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(14f)
                    .setBold()
            )

            document.add(billToTable)

            // Item Table Header
            val itemTable = Table(UnitValue.createPercentArray(floatArrayOf(4f, 2f, 2f, 2f, 2f)))
                .useAllAvailableWidth()
                .setBackgroundColor(ColorConstants.BLUE)
                .setFontColor(ColorConstants.WHITE)
                .setMarginBottom(10f)


            itemTable.addHeaderCell("Item Name")
            itemTable.addHeaderCell("Quantity")
            itemTable.addHeaderCell("Weight")
            itemTable.addHeaderCell("Price/Unit")
            itemTable.addHeaderCell("Amount")

            // Calculate total price
            var subTotalPrice = 0.0

            // Add item rows from order.products
            order.products.forEach { product ->

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
                Cell().add(Paragraph("Total tax : ${order.tax}"))
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
                    .setBold()
            )

            /*
            here check first if
             */

            if (order.orderStatus == Constants.PENDING) {

                footerTable.addCell(
                    Cell().add(Paragraph("Total paid : ${order.totalPaidAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setBold()
                )

                footerTable.addCell(
                    Cell().add(Paragraph("Total Remaining  Amount : ${order.remainingAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setBold()
                )

            } else {

                footerTable.addCell(
                    Cell().add(Paragraph("Total paid : ${order.totalPaidAmount}"))
                        .setBorder(Border.NO_BORDER)
                        .setBold()
                )

                val totalCalculedRemaining = (order.totalPrice - order.totalPaidAmount).toDouble()

                footerTable.addCell(
                    Cell().add(Paragraph("The Remaining Amount " + currency + (totalCalculedRemaining) + " is paid at " + order.orderTime + " " + order.orderDate))
                        .setBorder(Border.NO_BORDER)
                        .setBold()
                )

            }



            footerTable.addCell(
                Cell().add(Paragraph("Order Status : ${order.orderStatus}"))
                    .setBorder(Border.NO_BORDER)
                    .setBold()
            )

            // Add any additional footer details here...

            document.add(footerTable)

            document.close()
            Toast.makeText(context, "Pdf Created: $filePath", Toast.LENGTH_LONG).show()

            // Open the PDF
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Open PDF")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
     */

}