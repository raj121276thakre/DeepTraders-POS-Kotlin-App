package com.example.deeptraderspos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.deeptraderspos.R
import com.example.deeptraderspos.models.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class CartAdapter(
    private val context: Context,
    private val cartItems: ArrayList<CartItem>,
    private val txtTotalPrice: TextView,
    private val btnSubmitOrder: Button,
    private val imgNoProduct: ImageView,
    private val txtNoProduct: TextView
) : RecyclerView.Adapter<CartAdapter.MyViewHolder>() {

    private val f = DecimalFormat("#0.00")
    private var total_price: Double = 0.0


    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtItemName: TextView = view.findViewById(R.id.txt_item_name)
        val txtPrice: TextView = view.findViewById(R.id.txt_price)
        val txtWeight: TextView = view.findViewById(R.id.txt_weight)
        val txtQtyNumber: TextView = view.findViewById(R.id.txt_number)
        val imgProduct: ImageView = view.findViewById(R.id.cart_product_image)
        val imgDelete: ImageView = view.findViewById(R.id.img_delete)
        val txtPlus: TextView = view.findViewById(R.id.txt_plus)
        val txtMinus: TextView = view.findViewById(R.id.txt_minus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_product_items, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.txtItemName.text = cartItem.productName // Update this based on how you retrieve the product name
        holder.txtWeight.text = "${cartItem.productWeight} ${cartItem.weightUnitId}"
        holder.txtQtyNumber.text = cartItem.quantity.toString()

        val productTotalPrice = cartItem.productPrice * cartItem.quantity
        holder.txtPrice.text = "₹${f.format(productTotalPrice)}"

        // Update total price
        total_price += productTotalPrice
        txtTotalPrice.text = context.getString(R.string.total_price) + "₹${f.format(total_price)}"

        // Delete product from cart
        holder.imgDelete.setOnClickListener {
            deleteProductFromCart(cartItem, holder.adapterPosition)
        }

        // Increase quantity
        holder.txtPlus.setOnClickListener {
            updateProductQuantity(cartItem, cartItem.quantity + 1, holder)
        }

        // Decrease quantity
        holder.txtMinus.setOnClickListener {
            if (cartItem.quantity > 1) {
                updateProductQuantity(cartItem, cartItem.quantity - 1, holder)
            }
        }
    }

    fun getTotalPrice(): Double {
        return total_price
    }


    private fun deleteProductFromCart(cartItem: CartItem, position: Int) {
        val cartId = cartItem.productId // Update this based on how you store the cart ID

        FirebaseFirestore.getInstance().collection("carts")
            .document(cartId!!)
            .delete()
            .addOnSuccessListener {
                cartItems.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, R.string.product_removed_from_cart, Toast.LENGTH_SHORT).show()

                // Update total price and UI if cart is empty
                recalculateTotalPrice()
                if (cartItems.isEmpty()) {
                    imgNoProduct.visibility = View.VISIBLE
                    txtNoProduct.visibility = View.VISIBLE
                    txtTotalPrice.visibility = View.GONE
                    btnSubmitOrder.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, R.string.product_removed_failed, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProductQuantity(cartItem: CartItem, newQty: Int, holder: MyViewHolder) {
        val cartId = cartItem.productId // Update this based on how you store the cart ID

        FirebaseFirestore.getInstance().collection("carts")
            .document(cartId!!)
            .update("quantity", newQty)
            .addOnSuccessListener {
                cartItem.quantity = newQty
                holder.txtQtyNumber.text = newQty.toString()
                val newPrice = cartItem.productPrice * newQty
                holder.txtPrice.text = "₹${f.format(newPrice)}"

                // Recalculate total price
                recalculateTotalPrice()
            }
            .addOnFailureListener {
                Toast.makeText(context, R.string.product_quantity_update_failed, Toast.LENGTH_SHORT).show()
            }
    }

    private fun recalculateTotalPrice() {
        total_price = 0.0
        cartItems.forEach { cartItem ->
            total_price += cartItem.productPrice * cartItem.quantity
        }
        txtTotalPrice.text = context.getString(R.string.total_price) + "₹${f.format(total_price)}"
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    // Method to get the item at the given position
    fun getItem(position: Int): CartItem {
        return cartItems[position]
    }


}
