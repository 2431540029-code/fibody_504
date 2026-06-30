package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.model.OrderItem
import java.text.NumberFormat
import java.util.Locale

class OrderItemsAdapter(private val items: List<OrderItem>) : RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgProduct)
        val txtName: TextView = view.findViewById(R.id.txtProductName)
        val txtPrice: TextView = view.findViewById(R.id.txtProductPrice)
        val txtQty: TextView = view.findViewById(R.id.txtStockStatus) // Reuse this field for qty in list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        
        holder.txtName.text = item.productName
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        holder.txtPrice.text = formatter.format(item.price) + "đ"
        holder.txtQty.text = "Số lượng: ${item.quantity}"

        val resId = context.resources.getIdentifier(item.productImage, "drawable", context.packageName)
        Glide.with(context).load(if (resId != 0) resId else item.productImage).into(holder.img)
        
        // Hide badges for order list
        holder.itemView.findViewById<View>(R.id.badgeBestPrice)?.visibility = View.GONE
        holder.itemView.findViewById<View>(R.id.badgeGift)?.visibility = View.GONE
    }

    override fun getItemCount(): Int = items.size
}
