package com.example.fitbody.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.model.Order
import com.example.fitbody.ui.OrderDetailActivity
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(
    private val orderList: List<Order>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtOrderDate: TextView = itemView.findViewById(R.id.txtOrderDate)
        val txtOrderStatus: TextView = itemView.findViewById(R.id.txtOrderStatus)
        val txtOrderTotal: TextView = itemView.findViewById(R.id.txtOrderTotal)
        val txtDeliveryInfo: TextView = itemView.findViewById(R.id.txtDeliveryInfo)
        val layoutOrderItems: LinearLayout = itemView.findViewById(R.id.layoutOrderItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        val context = holder.itemView.context
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

        holder.txtOrderDate.text = "Ngày đặt: ${order.orderDate}"
        holder.txtOrderStatus.text = order.status
        holder.txtOrderTotal.text = "${formatter.format(order.totalPrice)}đ"

        // Hiển thị trạng thái vận chuyển
        when (order.status) {
            "Đang xử lý" -> {
                holder.txtOrderStatus.setTextColor(android.graphics.Color.YELLOW)
                holder.txtDeliveryInfo.text = "📦 Đơn hàng đang được chuẩn bị"
            }
            "Đã hủy" -> {
                holder.txtOrderStatus.setTextColor(android.graphics.Color.RED)
                holder.txtDeliveryInfo.text = "❌ Đơn hàng đã bị hủy"
            }
            "Yêu cầu hoàn tiền" -> {
                holder.txtOrderStatus.setTextColor(android.graphics.Color.MAGENTA)
                holder.txtDeliveryInfo.text = "💰 Đang xem xét hoàn tiền"
            }
            else -> {
                holder.txtOrderStatus.setTextColor(android.graphics.Color.GREEN)
                holder.txtDeliveryInfo.text = "✅ Đơn hàng đã giao thành công"
            }
        }

        // Render danh sách sản phẩm con
        holder.layoutOrderItems.removeAllViews()
        for (item in order.items) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_order_sub_item, holder.layoutOrderItems, false)
            val img = itemView.findViewById<ImageView>(R.id.imgOrderSubItem)
            val name = itemView.findViewById<TextView>(R.id.txtOrderSubItemName)
            val price = itemView.findViewById<TextView>(R.id.txtOrderSubItemPrice)

            name.text = "${item.productName} (x${item.quantity})"
            price.text = "${formatter.format(item.price)}đ"

            val resId = context.resources.getIdentifier(item.productImage, "drawable", context.packageName)
            Glide.with(context).load(if (resId != 0) resId else item.productImage).into(img)
            
            holder.layoutOrderItems.addView(itemView)
        }

        // Click để xem chi tiết
        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrderDetailActivity::class.java)
            intent.putExtra("order_id", order.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orderList.size
}
