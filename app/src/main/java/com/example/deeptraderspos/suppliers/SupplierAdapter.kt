package com.example.deeptraderspos.suppliers


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.databinding.SupplierItemBinding
import com.example.deeptraderspos.models.Supplier



class SupplierAdapter(
    private val suppliers: MutableList<Supplier>,
    private  val context : Context,
    private val onDeleteClicked: (Supplier) -> Unit,
    private val onEditClicked: (Supplier) -> Unit // Add this line

) : RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder>() {

    inner class SupplierViewHolder(private val binding: SupplierItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(supplier: Supplier, serialNumber: Int) {
            with(binding) {

                txtSupplierName.text = supplier.supplierName
                txtSupplierCell.text = supplier.supplierPhone
                txtSupplierEmail.text = supplier.supplierEmail
                txtSupplierAddress.text = supplier.supplierAddress


                binding.imgCall.setOnClickListener {
                    val phone = "tel:${supplier.supplierPhone}"
                    val callIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse(phone)
                    }
                    context.startActivity(callIntent)
                }



                binding.imgDelete.setOnClickListener {
                    // delete particular payment from firebase
                    onDeleteClicked(supplier)
                }

                binding.editCardButton.setOnClickListener {
                    onEditClicked(supplier)
                }





            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val binding =
            SupplierItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SupplierViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        holder.bind(suppliers[position], position + 1)

        // Apply animation using AnimationUtils
        //AnimationUtils.applyScaleAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int {
        return suppliers.size
    }


    fun removeItem(supplier: Supplier) {
        val position = suppliers.indexOf(supplier)
        if (position != -1) {
            suppliers.removeAt(position)
            notifyItemRemoved(position)
        }
    }


}
