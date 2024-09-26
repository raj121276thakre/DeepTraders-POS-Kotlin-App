package com.example.deeptraderspos.orders

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.Constants
import com.example.deeptraderspos.R
import com.example.deeptraderspos.models.CustomerWithOrders
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.models.SupplierWithOrders


class OrderAdapter(
    private val context: Context,
    private var entityWithOrdersData: List<Any>,
    private val isSupplier: Boolean,

    ) : RecyclerView.Adapter<OrderAdapter.MyViewHolder>() {


    fun updateEntityWithOrdersData(newEntityWithOrders: List<Any>) {
        entityWithOrdersData = newEntityWithOrders.filter {
            when (it) {
                is CustomerWithOrders -> it.orders.isNotEmpty()
                is SupplierWithOrders -> it.orders.isNotEmpty()
                else -> false
            }
        }
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val entityWithOrders = entityWithOrdersData[position]

        if (isSupplier) {
            val supplierWithOrders = entityWithOrders as SupplierWithOrders
            val supplier = supplierWithOrders.supplier
            val orders = supplierWithOrders.orders
            holder.personImage.setImageResource(R.drawable.supplier)


            bindData(holder, supplier.supplierName, supplier.supplierPhone, orders)
        } else {
            val customerWithOrders = entityWithOrders as CustomerWithOrders
            val customer = customerWithOrders.customer
            val orders = customerWithOrders.orders
            holder.personImage.setImageResource(R.drawable.single_customer)

            bindData(holder, customer.customerName, customer.customerPhone, orders)
        }
    }

    // Common function to bind customer/supplier data with orders
    private fun bindData(holder: MyViewHolder, name: String, phone: String, orders: List<Order>) {
        holder.txtPersonName.text = name
        holder.txtcellNumber.text = phone
        holder.txtTotalOrders.text = "Total Orders: ${orders.size}"

        // Calculate total amount, paid, and remaining
        val totalAmount = orders.sumOf { it.totalPrice }
        val totalPaid = orders.sumOf { it.updatedTotalPaidAmount }
        val totalRemaining = orders.sumOf { it.updatedRemainingAmount }

        holder.txtTotalAmount.text = "Total Amount: ₹$totalAmount"
        holder.txtTotalPaid.text = "Total Paid: ₹$totalPaid"
        holder.txtTotalRemaining.text = "Total Remaining: ₹$totalRemaining"

        // Logic for completed or pending orders
        if (totalRemaining.toInt() == 0) {
            holder.txtOrderStatus.text = Constants.COMPLETED
            holder.txtOrderStatus.setBackgroundColor(Color.parseColor("#43a047"))
            holder.txtOrderStatus.setTextColor(Color.WHITE)
        } else {
            holder.txtOrderStatus.text = Constants.PENDING
            holder.txtOrderStatus.setBackgroundColor(Color.parseColor("#757575"))
            holder.txtOrderStatus.setTextColor(Color.WHITE)
        }

        // Click listener for the item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PersonWiseOrdersActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("isSupplier", isSupplier)
            context.startActivity(intent)
        }

        // Call button click listener
        holder.imgCall.setOnClickListener {
            val phoneUri = "tel:$phone"
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse(phoneUri)
            }
            context.startActivity(callIntent)
        }
    }

    override fun getItemCount(): Int {
        return entityWithOrdersData.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPersonName: TextView = view.findViewById(R.id.text_person_name)
        val txtcellNumber: TextView = view.findViewById(R.id.txt_person_cell)
        val txtTotalOrders: TextView = view.findViewById(R.id.txt_person_total_orders)
        val txtTotalAmount: TextView = view.findViewById(R.id.txt_total_Amount)
        val txtTotalPaid: TextView = view.findViewById(R.id.txt_totalPaid)
        val txtTotalRemaining: TextView = view.findViewById(R.id.txt_totalRemaining)
        val txtOrderStatus: TextView = view.findViewById(R.id.txt_order_status)
        val imgCall: ImageView = view.findViewById(R.id.img_call_btn)
        val personImage: ImageView = view.findViewById(R.id.person_image)
    }
}
