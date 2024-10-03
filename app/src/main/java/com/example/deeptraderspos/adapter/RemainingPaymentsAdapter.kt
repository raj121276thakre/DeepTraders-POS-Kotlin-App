package com.example.deeptraderspos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.R
import com.example.deeptraderspos.models.RemainingPayment


class RemainingPaymentsAdapter(
    private val remainingPayments: List<RemainingPayment>,
    private val currency: String,
    private val onDownloadPdfClick: (RemainingPayment) -> Unit
) : RecyclerView.Adapter<RemainingPaymentsAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtPaidAmount: TextView = itemView.findViewById(R.id.text_paid_amount)
        val textRemainingAmount: TextView = itemView.findViewById(R.id.text_remaining_amount)
        val buttonDownloadPdf: Button = itemView.findViewById(R.id.button_download_pdf)

        fun bind(payment: RemainingPayment) {
            val totalPaidAmount = payment.paidAmount // Adjust as needed
            val totalRemainingAmount = payment.remainingAmount // Adjust as needed
            txtPaidAmount.text = "The Paid Amount $currency$totalPaidAmount is paid at ${payment.paidTime} ${payment.paidDate}"


            textRemainingAmount.text = "Remaining Amount :$currency${totalRemainingAmount.toInt()}"

            buttonDownloadPdf.setOnClickListener {
                onDownloadPdfClick(payment)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_remaining_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(remainingPayments[position])
    }

    override fun getItemCount(): Int {
        return remainingPayments.size
    }
}
