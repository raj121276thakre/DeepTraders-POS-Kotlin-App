package com.example.deeptraderspos.setting.order_type

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.databinding.OrderTypeItemBinding
import com.example.deeptraderspos.models.OrderType

class OrderTypeAdapter(
    private val orderTypes: MutableList<OrderType>,
    private val context: Context,
    private val onDeleteClicked: (OrderType) -> Unit,
) : RecyclerView.Adapter<OrderTypeAdapter.OrderTypeViewHolder>() {

    inner class OrderTypeViewHolder(private val binding: OrderTypeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderType: OrderType) {
            with(binding) {
                // Set order type data to the views
                txtTypeName.text = orderType.orderTypeName

                // Load image (if applicable), using a placeholder if the image is null
                val imageResource = context.resources.getIdentifier(
                    orderType.orderTypeImage ?: "default_order_type", // Use default if null
                    "drawable",
                    context.packageName
                )

                // Set delete button listener
                imgDelete.setOnClickListener {
                    onDeleteClicked(orderType)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderTypeViewHolder {
        val binding = OrderTypeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderTypeViewHolder, position: Int) {
        holder.bind(orderTypes[position])
    }

    override fun getItemCount(): Int {
        return orderTypes.size
    }

    // Function to remove item from the list
    fun removeItem(orderType: OrderType) {
        val position = orderTypes.indexOf(orderType)
        if (position != -1) {
            orderTypes.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
