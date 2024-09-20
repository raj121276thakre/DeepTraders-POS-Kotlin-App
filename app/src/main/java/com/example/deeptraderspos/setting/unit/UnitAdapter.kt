package com.example.deeptraderspos.setting.unit

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.databinding.UnitItemBinding
import com.example.deeptraderspos.models.Units

class UnitAdapter(
    private val units: MutableList<Units>,
    private val context: Context,
    private val onDeleteClicked: (Units) -> Unit,
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    inner class UnitViewHolder(private val binding: UnitItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(unit: Units) {
            with(binding) {
                // Set unit data to the views
                txtUnitName.text = unit.unitName

                // Load image (if applicable), using a placeholder if the image is null
                val imageResource = context.resources.getIdentifier(
                    unit.unitImage ?: "unit", // Use default if null
                    "drawable",
                    context.packageName
                )
                // Optionally set the image to an ImageView if you have one
                // imgUnit.setImageResource(imageResource)

                // Set delete button listener
                imgDelete.setOnClickListener {
                    onDeleteClicked(unit)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = UnitItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.bind(units[position])
    }

    override fun getItemCount(): Int {
        return units.size
    }

    // Function to remove item from the list
    fun removeItem(unit: Units) {
        val position = units.indexOf(unit)
        if (position != -1) {
            units.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
