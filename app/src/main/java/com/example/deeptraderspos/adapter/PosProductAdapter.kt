package com.example.deeptraderspos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.deeptraderspos.R
import com.example.deeptraderspos.databinding.PosProductItemBinding
import com.example.deeptraderspos.databinding.ProductItemBinding
import com.example.deeptraderspos.models.Product

class PosProductAdapter(
    private var products: MutableList<Product>,
    private val context: Context,
    private val onAddtocartClick: (Product) -> Unit,
    private val onEditClicked: (Product) -> Unit

) : RecyclerView.Adapter<PosProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: PosProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            with(binding) {
                // Set product data to the views
                txtProductName.text = product.productName
                txtPrice.text = "₹${product.sellPrice}"
                txtWeight.text = "${product.weight} ${product.weightUnit}"
                txtStock.text = "Stock: ${product.stock}"
                //imgProduct.text = product.buyPrice.toString()


                // Set delete button listener
                btnAddCart.setOnClickListener {
                    onAddtocartClick(product)
                }

                cardProduct.setOnClickListener {
                    onEditClicked(product)
                }

                // Set product image if needed
                // Load product image using Glide
                if (product.productImage != null) {
                    Glide.with(context)
                        .load(product.productImage) // URL or URI of the image
                        .apply(RequestOptions().placeholder(R.drawable.image_placeholder)) // Placeholder image
                        .into(binding.imgProduct) // Assuming imgProduct is your ImageView ID
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = PosProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    fun updateProducts(newProducts: MutableList<Product>) {
      //  products.clear()  // Clear the old products
      //  products.addAll(newProducts)  // Add the new products
        products = newProducts
        notifyDataSetChanged()  // Notify the adapter that the data set has changed
    }


}
