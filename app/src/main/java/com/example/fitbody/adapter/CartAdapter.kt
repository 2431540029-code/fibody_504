package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.model.CartItem
import com.example.fitbody.database.DatabaseHelper
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartList: List<CartItem>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbSelected: CheckBox = itemView.findViewById(R.id.cbSelected)
        val imgCartProduct: ImageView = itemView.findViewById(R.id.imgCartProduct)
        val txtCartName: TextView = itemView.findViewById(R.id.txtCartName)
        val txtCartPrice: TextView = itemView.findViewById(R.id.txtCartPrice)
        val txtCartQuantity: TextView = itemView.findViewById(R.id.txtCartQuantity)
        val btnMinus: TextView = itemView.findViewById(R.id.btnMinus)
        val btnPlus: TextView = itemView.findViewById(R.id.btnPlus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]
        val context = holder.itemView.context
        val dbHelper = DatabaseHelper(context)

        holder.cbSelected.setOnCheckedChangeListener(null)
        holder.cbSelected.isChecked = item.isSelected

        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        holder.txtCartName.text = item.name
        holder.txtCartPrice.text = formatter.format(item.price) + "đ"
        holder.txtCartQuantity.text = item.quantity.toString()

        val resId = context.resources.getIdentifier(item.image, "drawable", context.packageName)
        Glide.with(context)
            .load(if (resId != 0) resId else item.image)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imgCartProduct)

        holder.cbSelected.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            dbHelper.updateCartSelection(item.id, isChecked)
            onDataChanged()
        }

        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity -= 1
                dbHelper.writableDatabase.execSQL("UPDATE tbl_cart SET quantity = ? WHERE id = ?", arrayOf(item.quantity, item.id))
                notifyItemChanged(position)
                onDataChanged()
            } else {
                dbHelper.writableDatabase.delete("tbl_cart", "id = ?", arrayOf(item.id.toString()))
                (cartList as MutableList).removeAt(position)
                notifyDataSetChanged() // Easier to just reload everything on delete
                onDataChanged()
            }
        }

        holder.btnPlus.setOnClickListener {
            item.quantity += 1
            dbHelper.writableDatabase.execSQL("UPDATE tbl_cart SET quantity = ? WHERE id = ?", arrayOf(item.quantity, item.id))
            notifyItemChanged(position)
            onDataChanged()
        }
    }

    override fun getItemCount(): Int = cartList.size
}
