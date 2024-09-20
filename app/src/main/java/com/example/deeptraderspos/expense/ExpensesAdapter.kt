package com.example.deeptraderspos.expense

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.databinding.ExpenseItemBinding
import com.example.deeptraderspos.models.Expense

class ExpenseAdapter(
    private val expenses: MutableList<Expense>,
    private val context: Context,
    private val onDeleteClicked: (Expense) -> Unit,
    private val onEditClicked: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(private val binding: ExpenseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            with(binding) {
                // Set expense data to the views
                txtExpenseName.text = expense.expenseName
                txtExpenseAmount.text = expense.expenseAmount.toString()
                txtDateTime.text = "${expense.expenseDate} ${expense.expenseTime}"
                txtExpenseNote.text = expense.expenseNote

                // Set delete button listener
                imgDelete.setOnClickListener {
                    onDeleteClicked(expense)
                }

                // Set click listener for editing
                root.setOnClickListener {
                    onEditClicked(expense)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ExpenseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    // Function to remove item from the list
    fun removeItem(expense: Expense) {
        val position = expenses.indexOf(expense)
        if (position != -1) {
            expenses.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
