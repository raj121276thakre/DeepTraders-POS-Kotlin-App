package com.example.deeptraderspos.setting.payment_method

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.databinding.PaymentMethodItemBinding
import com.example.deeptraderspos.models.PaymentMethod

class PaymentMethodAdapter(
    private val paymentMethods: MutableList<PaymentMethod>,
    private val context: Context,
    private val onDeleteClicked: (PaymentMethod) -> Unit,
) : RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder>() {

    inner class PaymentMethodViewHolder(private val binding: PaymentMethodItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(paymentMethod: PaymentMethod) {
            with(binding) {
                // Set payment method data to the views
                txtPaymentMethodName.text = paymentMethod.paymentMethodName

                // Load image (if applicable), using a placeholder if the image is null
                val imageResource = context.resources.getIdentifier(
                    paymentMethod.paymentMethodImage ?: "payment_method", // Use default if null
                    "drawable",
                    context.packageName
                )

                // Example of setting an image if needed
               // imgPaymentMethod.setImageResource(imageResource)

                // Set delete button listener
                imgDelete.setOnClickListener {
                    onDeleteClicked(paymentMethod)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val binding = PaymentMethodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentMethodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        holder.bind(paymentMethods[position])
    }

    override fun getItemCount(): Int {
        return paymentMethods.size
    }

    // Function to remove item from the list
    fun removeItem(paymentMethod: PaymentMethod) {
        val position = paymentMethods.indexOf(paymentMethod)
        if (position != -1) {
            paymentMethods.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
