package com.example.deeptraderspos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.SalesReportItemBinding
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.ProductOrder

class SalesReportAdapter(
    private val context: Context,
    private val products: List<ProductOrder> ,
) : RecyclerView.Adapter<SalesReportAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SalesReportItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val productOrder = products[position]


        // Set product name
        holder.binding.txtProductName.text = productOrder.productName
       // holder.binding.txtDate.text = order

        // Set product quantity and weight
        holder.binding.txtQty.text =
            context.getString(R.string.quantity) + productOrder.quantity.toString()
        holder.binding.txtWeight.text =
            context.getString(R.string.weight) + productOrder.productWeight.toString()

        // Calculate and display total cost
        val totalCost = productOrder.productPrice * productOrder.quantity
        holder.binding.txtTotalCost.text =
            "${productOrder.productPrice} x ${productOrder.quantity} = $totalCost"

        // Handle product image from Firestore (base64 string)
        holder.binding.imgProduct.setImageResource(R.drawable.expense) // Default image

    }

    override fun getItemCount(): Int {
        return products.size
    }

    // ViewHolder class with ViewBinding
    inner class MyViewHolder(val binding: SalesReportItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
