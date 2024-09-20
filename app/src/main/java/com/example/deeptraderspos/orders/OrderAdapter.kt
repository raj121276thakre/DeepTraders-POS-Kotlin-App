package com.example.deeptraderspos.orders

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.Constants
import com.example.deeptraderspos.R
import com.example.deeptraderspos.models.Order
import com.google.firebase.firestore.FirebaseFirestore

class OrderAdapter(
    private val context: Context,
    private var orderData: List<Order>
) : RecyclerView.Adapter<OrderAdapter.MyViewHolder>() {

    /*
    1. Add OrderDetailsActivity
    2. search in OrdersActivity
    3. change order status
     */



    // Update data in the adapter
    fun updateOrderData(newOrders: List<Order>) {
        orderData = newOrders
        notifyDataSetChanged() // Notify adapter of data changes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val order = orderData[position]

        holder.txtCustomerName.text = order.customerName
        holder.txtOrderId.text = context.getString(R.string.order_id) + order.orderId
        holder.txtPaymentMethod.text = context.getString(R.string.payment_method) + order.paymentMethod
        holder.txtOrderType.text = context.getString(R.string.order_type) + order.orderType
        holder.txtDate.text = order.orderTime + " " + order.orderDate
        holder.txtOrderStatus.text = order.orderStatus

        // Logic for completed or canceled orders
        if (order.orderStatus == Constants.COMPLETED) {
            holder.txtOrderStatus.setBackgroundColor(Color.parseColor("#43a047"))
            holder.txtOrderStatus.setTextColor(Color.WHITE)
            holder.imgStatus.visibility = View.GONE
        } else if (order.orderStatus == Constants.CANCEL) {
            holder.txtOrderStatus.setBackgroundColor(Color.parseColor("#e53935"))
            holder.txtOrderStatus.setTextColor(Color.WHITE)
            holder.imgStatus.visibility = View.GONE
        }

//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, OrderDetailsActivity::class.java)
//            intent.putExtra("order_id", order.orderId)
//            context.startActivity(intent)
//        }
    }

    override fun getItemCount(): Int {
        return orderData.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCustomerName: TextView = view.findViewById(R.id.txt_customer_name)
        val txtOrderId: TextView = view.findViewById(R.id.txt_order_id)
        val txtPaymentMethod: TextView = view.findViewById(R.id.txt_payment_method)
        val txtOrderType: TextView = view.findViewById(R.id.txt_order_type)
        val txtDate: TextView = view.findViewById(R.id.txt_date)
        val txtOrderStatus: TextView = view.findViewById(R.id.txt_order_status)
        val imgStatus: ImageView = view.findViewById(R.id.img_status)
    }
}
