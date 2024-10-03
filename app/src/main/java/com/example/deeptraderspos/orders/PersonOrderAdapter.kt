package com.example.deeptraderspos.orders

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.Constants
import com.example.deeptraderspos.R
import com.example.deeptraderspos.models.Order
import com.example.deeptraderspos.orders.orderDetails.OrderDetailsActivity
import com.google.firebase.firestore.FirebaseFirestore


class PersonOrderAdapter(
    private val context: Context,
    private var orderData: List<Order>,
    private val isSupplier: Boolean
) : RecyclerView.Adapter<PersonOrderAdapter.MyViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    // Update data in the adapter
    fun updatePersonOrderData(newOrders: List<Order>) {
        orderData = newOrders
        notifyDataSetChanged() // Notify adapter of data changes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.person_order_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val order = orderData[position]


        // Set name based on whether it's a supplier or customer
        if (isSupplier) {
            holder.txtPersonName.text = order.supplierName
        } else {
            holder.txtPersonName.text = order.customerName
        }

        holder.txtOrderId.text = context.getString(R.string.order_id) + order.orderId
        holder.txtPaymentMethod.text =
            context.getString(R.string.payment_method) + order.paymentMethod
        holder.txtOrderType.text = context.getString(R.string.order_type) + order.orderType
        holder.txtDate.text = order.orderTime + " " + order.orderDate
        holder.txtOrderStatus.text = order.orderStatus

        // Logic for completed or canceled orders
        if (order.orderStatus == Constants.COMPLETED) {
            holder.txtOrderStatus.setBackgroundColor(Color.parseColor("#43a047"))
            holder.txtOrderStatus.setTextColor(Color.WHITE)
            holder.imgStatus.visibility = View.GONE
        } else if (order.orderStatus == Constants.PENDING) {
            holder.txtOrderStatus.setBackgroundColor(Color.parseColor("#757575"))
            holder.txtOrderStatus.setTextColor(Color.WHITE)
            holder.imgStatus.visibility = View.GONE
        }


//       // goto OrderDetailsActivity
//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, OrderDetailsActivity::class.java)
//            intent.putExtra("order", order)  // Pass the Parcelable Order object
//            intent.putExtra("isSupplier", isSupplier)
//            context.startActivity(intent)
//        }


        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrderDetailsActivity::class.java)

            intent.putExtra("isSupplier", isSupplier)

            intent.putExtra("orderId", order.orderId)
            intent.putExtra("orderDate", order.orderDate)
            intent.putExtra("orderTime", order.orderTime)
            intent.putExtra("orderType", order.orderType)
            intent.putExtra("orderStatus", order.orderStatus)
            intent.putExtra("paymentMethod", order.paymentMethod)
            intent.putExtra("customerName", order.customerName)
            intent.putExtra("supplierName", order.supplierName)
            intent.putExtra("tax", order.tax)
            intent.putExtra("discount", order.discount)
            intent.putExtra("totalPrice", order.totalPrice)
            intent.putExtra("totalPaidAmount", order.totalPaidAmount)
            intent.putExtra("remainingAmount", order.remainingAmount)
            intent.putExtra("remainingAmtPaidDate", order.remainingAmtPaidDate)
            intent.putExtra("remainingAmtPaidTime", order.remainingAmtPaidTime)
            intent.putExtra("updatedRemainingAmount", order.updatedRemainingAmount)
            intent.putExtra("updatedTotalPaidAmount", order.updatedTotalPaidAmount)

// Since ProductOrder and RemainingPayment are lists of Parcelable, you can pass them as ArrayLists
            intent.putParcelableArrayListExtra("products", ArrayList(order.products))
            intent.putParcelableArrayListExtra(
                "remainingPayments",
                ArrayList(order.remainingPayments)
            )

            context.startActivity(intent)


        }




        holder.imgStatus.setOnClickListener {
//            val dialogBuilder = NiftyDialogBuilder.getInstance(context)
//            dialogBuilder
//                .withTitle(context.getString(R.string.change_order_status))
//                .withTitleColor("#FFFFFF")
//                .withMessage(context.getString(R.string.please_change_order_status_to_complete_or_cancel) + "\n\nRemaining Payment amount : ₹${order.remainingAmount}")
//                .withMessageColor("#FFFFFF")
//                .withEffect(Effectstype.Slidetop)
//                .withDialogColor("#04C7FF") // Use color code for dialog
//                .withButton1Text("Cancel\n")
//                .withButtonDrawable(R.drawable.rounded_button)
//                .withButton2Text(context.getString(R.string.pay_remaining))
//                .withDividerColor("#FFFFFF")
//
//                .setButton1Click {
//                    dialogBuilder.dismiss()
//                }
//                .setButton2Click {
//                    updateOrderStatus(order.orderId, Constants.COMPLETED, holder)
//                    dialogBuilder.dismiss()
//                }
//                .show()
        }


    }


//    private fun updateOrderStatus(orderId: String, status: String, holder: MyViewHolder) {
//
//        // Get current date in the required format
//        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
//        val currentTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())
//
//        // Prepare the data to update
//        val updateData = mapOf(
//            "orderStatus" to status,
//            "remainingAmtPaidDate" to currentDate,
//            "remainingAmtPaidTime" to currentTime,
//            "remainingAmount" to 0.0
//        )
//
//        val orderRef = firestore.collection(if (isSupplier) "AllOrdersSuppliers" else "AllOrders")
//
//        orderRef.document(orderId).update(updateData)
//            .addOnSuccessListener {
//                if (status == Constants.COMPLETED) {
//                    Toast.makeText(context, R.string.order_updated, Toast.LENGTH_SHORT).show()
//                    holder.txtOrderStatus.text = Constants.COMPLETED
//                    holder.txtOrderStatus.setBackgroundColor(Color.parseColor("#43a047"))
//                    holder.txtOrderStatus.setTextColor(Color.WHITE)
//                    holder.imgStatus.visibility = View.GONE
//                } else {
//                    Toast.makeText(context, R.string.order_updated, Toast.LENGTH_SHORT).show()
//                    holder.txtOrderStatus.text = Constants.PENDING
//                    holder.txtOrderStatus.setBackgroundColor(Color.parseColor("#757575"))
//                    holder.txtOrderStatus.setTextColor(Color.WHITE)
//                    holder.imgStatus.visibility = View.GONE
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
//            }
//    }


    override fun getItemCount(): Int {
        return orderData.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPersonName: TextView = view.findViewById(R.id.text_person_name)
        val txtOrderId: TextView = view.findViewById(R.id.txt_order_id)
        val txtPaymentMethod: TextView = view.findViewById(R.id.txt_payment_method)
        val txtOrderType: TextView = view.findViewById(R.id.txt_order_type)
        val txtDate: TextView = view.findViewById(R.id.txt_date)
        val txtOrderStatus: TextView = view.findViewById(R.id.txt_order_status)
        val imgStatus: ImageView = view.findViewById(R.id.img_status)
    }
}
