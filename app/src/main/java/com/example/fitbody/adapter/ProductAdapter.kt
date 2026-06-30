package com.example.fitbody.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val productList: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val txtProductName: TextView = itemView.findViewById(R.id.txtProductName)
        val txtStockStatus: TextView = itemView.findViewById(R.id.txtStockStatus)
        val txtProductPrice: TextView = itemView.findViewById(R.id.txtProductPrice)
        val txtOriginalPrice: TextView = itemView.findViewById(R.id.txtOriginalPrice)
        val badgeGift: TextView = itemView.findViewById(R.id.badgeGift)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = productList[position]
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

        holder.txtProductName.text = item.name
        holder.txtStockStatus.text = if (item.isAvailable) "Còn hàng" else "Hết hàng"
        holder.txtStockStatus.setTextColor(if (item.isAvailable) 0xFF4CAF50.toInt() else 0xFFFF5252.toInt())
        
        holder.txtProductPrice.text = formatter.format(item.price) + "đ"
        
        // Gạch ngang giá cũ
        holder.txtOriginalPrice.text = formatter.format(item.originalPrice) + "đ"
        holder.txtOriginalPrice.paintFlags = holder.txtOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        holder.badgeGift.visibility = if (item.hasGift) View.VISIBLE else View.GONE

        val context = holder.itemView.context
        val resId = context.resources.getIdentifier(item.image, "drawable", context.packageName)

        Glide.with(context)
            .load(if (resId != 0) resId else item.image)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imgProduct)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = productList.size
}
