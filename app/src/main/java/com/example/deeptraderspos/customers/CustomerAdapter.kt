package com.example.deeptraderspos.customers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.databinding.CustomerItemBinding
import com.example.deeptraderspos.databinding.SupplierItemBinding
import com.example.deeptraderspos.models.Customer
import com.example.deeptraderspos.models.Supplier

class CustomerAdapter(
    private val customers: MutableList<Customer>,
    private val context: Context,
    private val onDeleteClicked: (Customer) -> Unit,
    private val onEditClicked: (Customer) -> Unit // Add this line //

) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    inner class CustomerViewHolder(private val binding: CustomerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: Customer, serialNumber: Int) {
            with(binding) {

                txtCustomerName.text = customer.customerName
                txtCustomerCell.text = customer.customerPhone
                txtCustomerEmail.text = customer.customerEmail
                txtCustomerAddress.text = customer.customerAddress


                binding.imgCall.setOnClickListener {
                    val phone = "tel:${customer.customerPhone}"
                    val callIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse(phone)
                    }
                    context.startActivity(callIntent)
                }



                binding.imgDelete.setOnClickListener {
                    // delete particular payment from firebase
                    onDeleteClicked(customer)
                }

                binding.editCardButton.setOnClickListener {
                    onEditClicked(customer)
                }


            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding =
            CustomerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(customers[position], position + 1)

        // Apply animation using AnimationUtils
        //AnimationUtils.applyScaleAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int {
        return customers.size
    }


    fun removeItem(customer: Customer) {
        val position = customers.indexOf(customer)
        if (position != -1) {
            customers.removeAt(position)
            notifyItemRemoved(position)
        }
    }


}
