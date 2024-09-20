package com.example.deeptraderspos.product

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.ProductItemBinding
import com.example.deeptraderspos.models.Product

class ProductAdapter(
    private val products: MutableList<Product>,
    private val context: Context,
    private val onDeleteClicked: (Product) -> Unit,
    private val onEditClicked: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            with(binding) {
                // Set product data to the views
                txtProductName.text = product.productName
                txtProductSupplier.text = product.supplier
                txtProductBuyPrice.text = product.buyPrice.toString()
                txtProductSellPrice.text = product.sellPrice.toString()

                // Set delete button listener
                imgDelete.setOnClickListener {
                    onDeleteClicked(product)
                }

                // Set click listener for editing
                root.setOnClickListener {
                    onEditClicked(product)
                }

                // Set product image if needed
                // Load product image using Glide
                if (product.productImage != null) {
                    Glide.with(context)
                        .load(product.productImage) // URL or URI of the image
                        .apply(RequestOptions().placeholder(R.drawable.image_placeholder)) // Placeholder image
                        .into(binding.productImage) // Assuming imgProduct is your ImageView ID
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int {
        return products.size
    }

    // Function to remove item from the list
    fun removeItem(product: Product) {
        val position = products.indexOf(product)
        if (position != -1) {
            products.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
