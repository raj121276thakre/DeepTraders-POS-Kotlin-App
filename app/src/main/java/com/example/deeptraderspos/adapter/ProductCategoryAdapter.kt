package com.example.deeptraderspos.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.databinding.ProductCategoryItemBinding
import com.example.deeptraderspos.models.Category


class ProductCategoryAdapter(
    private val categories: MutableList<Category>,
    private val context: Context,
    private val onClicked: (Category) -> Unit,
) : RecyclerView.Adapter<ProductCategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ProductCategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            with(binding) {
                // Set category data to the views
                txtCategoryName.text = category.categoryName

                // Load image (if applicable), using a placeholder if the image is null
                val imageResource = context.resources.getIdentifier(
                    category.categoryImage ?: "category", // Use default if null
                    "drawable",
                    context.packageName
                )

                //image

                // Set delete button listener
                root.setOnClickListener {
                    onClicked(category)
                }


            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ProductCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    // Function to remove item from the list
    fun removeItem(category: Category) {
        val position = categories.indexOf(category)
        if (position != -1) {
            categories.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
