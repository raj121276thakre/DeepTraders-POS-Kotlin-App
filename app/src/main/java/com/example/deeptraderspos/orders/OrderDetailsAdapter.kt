package com.example.deeptraderspos.orders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.R
import com.example.deeptraderspos.models.ProductOrder
import java.text.DecimalFormat

class OrderDetailsAdapter(
    private val context: Context,
    private val products: List<ProductOrder> // Accept an Order object directly
) : RecyclerView.Adapter<OrderDetailsAdapter.MyViewHolder>() {

    // Create a formatter instance
    private val formatter = DecimalFormat("#0.00")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_details_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Access the product data from the order object
        val product = products[position]

        holder.txtProductName.text = product.productName
        holder.txtProductQty.text = context.getString(R.string.quantity) + product.quantity
        holder.txtProductWeight.text = context.getString(R.string.weight) + product.productWeight

        val unitPrice = product.productPrice.toDouble()
        val qty = product.quantity.toInt()
        val totalCost = unitPrice * qty



        holder.txtTotalCost.text = context.getString(R.string.currency_symbol)+"$unitPrice x $qty = " + context.getString(R.string.currency_symbol) + formatter.format(totalCost)



    }

    override fun getItemCount(): Int {
        return products.size // Assuming Order has a list of products
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtProductName: TextView = view.findViewById(R.id.txt_product_name)
        val txtProductQty: TextView = view.findViewById(R.id.txt_qty)
        val txtProductWeight: TextView = view.findViewById(R.id.txt_weight)
        val txtTotalCost: TextView = view.findViewById(R.id.txt_total_cost)

    }
}
