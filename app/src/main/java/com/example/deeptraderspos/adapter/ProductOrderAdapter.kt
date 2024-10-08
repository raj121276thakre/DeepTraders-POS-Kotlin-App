package com.example.deeptraderspos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.R
import com.example.deeptraderspos.models.ProductOrder
import java.text.DecimalFormat

class ProductOrderAdapter(
    private val context: Context,
    private val productOrders: ArrayList<ProductOrder>,
    private val txtTotalPrice: TextView,
    private val txtTotalAmount: TextView,
    private val txtRemainingAmount: TextView,
    private val paidAmount: Double?,

    ) : RecyclerView.Adapter<ProductOrderAdapter.MyViewHolder>() {

    private val f = DecimalFormat("#0.00")
    private var total_price: Double = 0.0

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtProductName: TextView = view.findViewById(R.id.txt_item_name)
        val txtPrice: TextView = view.findViewById(R.id.txt_price)
//        val txtQuantity: TextView = view.findViewById(R.id.txt_qty)
        val imgDelete: ImageView = view.findViewById(R.id.img_delete)
        val stock: TextView = view.findViewById(R.id.txt_stock)
        val llQuantity: LinearLayout = view.findViewById(R.id.llQuantity)
        val cartImage: ImageView = view.findViewById(R.id.cart_product_image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_product_items, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val productOrder = productOrders[position]

        holder.stock.visibility = View.GONE
        holder.llQuantity.visibility = View.GONE
        holder.cartImage.visibility = View.GONE

        holder.txtProductName.text = productOrder.productName
       // holder.txtQuantity.text = productOrder.quantity.toString()
        val productTotalPrice = productOrder.productPrice * productOrder.quantity
        holder.txtPrice.text = "${productOrder.productPrice} x ${productOrder.quantity} = ₹${f.format(productTotalPrice)}"

////        // Update total price
//        total_price += productTotalPrice
//       txtTotalPrice.text ="₹${f.format(total_price)}"
        recalculateTotalPrice()


        // Delete product from list
        holder.imgDelete.setOnClickListener {
            deleteProductOrder(productOrder, holder.adapterPosition)
        }
    }


    private fun deleteProductOrder(productOrder: ProductOrder, position: Int) {
        productOrders.removeAt(position)
        notifyItemRemoved(position)

        Toast.makeText(context, R.string.product_removed_from_cart, Toast.LENGTH_SHORT).show()
        recalculateTotalPrice()
    }

    private fun recalculateTotalPrice() {
        total_price = 0.0
        productOrders.forEach { productOrder ->
            total_price += productOrder.productPrice * productOrder.quantity
        }
        txtTotalPrice.text = " ₹${f.format(total_price)}"
        txtTotalAmount.text = " ₹${f.format(total_price)}"
        // Calculate the remaining amount
        val remainingAmount = total_price - paidAmount!!
        txtRemainingAmount.text = "₹${String.format("%.2f", remainingAmount)}"

    }

    override fun getItemCount(): Int {
        return productOrders.size
    }

    // Method to get a product at a specific position
    fun getItem(position: Int): ProductOrder {
        return productOrders[position]
    }

    fun getTotalPrice(): Double {
        return total_price
    }
}
