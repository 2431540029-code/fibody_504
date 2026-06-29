package com.example.fitbody.adapter

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

    class ProductViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imgProduct: ImageView =
            itemView.findViewById(R.id.imgProduct)

        val txtProductName: TextView =
            itemView.findViewById(R.id.txtProductName)

        val txtProductCategory: TextView =
            itemView.findViewById(R.id.txtProductCategory)

        val txtProductPrice: TextView =
            itemView.findViewById(R.id.txtProductPrice)

        val txtProductDescription: TextView =
            itemView.findViewById(R.id.txtProductDescription)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_product,
                    parent,
                    false
                )

        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        val item = productList[position]

        holder.txtProductName.text = item.name
        holder.txtProductCategory.text = item.category
        holder.txtProductDescription.text = item.description

        val formatter =
            NumberFormat.getInstance(Locale("vi", "VN"))

        holder.txtProductPrice.text =
            formatter.format(item.price) + "đ"

        val context = holder.itemView.context
        val resId = context.resources.getIdentifier(
            item.image.replace(".png", "").replace(".jpg", ""),
            "drawable",
            context.packageName
        )

        Glide.with(context)
            .load(if (resId != 0) resId else item.image)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imgProduct)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}