package com.example.fitbody.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.OrderItemsAdapter
import com.example.fitbody.database.DatabaseHelper
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var txtStatus: TextView
    private lateinit var txtReceiver: TextView
    private lateinit var txtAddress: TextView
    private lateinit var txtEst: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var txtTotal: TextView
    private lateinit var txtPayment: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnRefund: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        initViews()
        loadOrder()

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun initViews() {
        txtStatus = findViewById(R.id.txtOrderStatus)
        txtReceiver = findViewById(R.id.txtReceiverInfo)
        txtAddress = findViewById(R.id.txtReceiverAddress)
        txtEst = findViewById(R.id.txtEstDelivery)
        rvItems = findViewById(R.id.rvOrderItems)
        txtTotal = findViewById(R.id.txtTotal)
        txtPayment = findViewById(R.id.txtPaymentMethod)
        btnCancel = findViewById(R.id.btnCancelOrder)
        btnRefund = findViewById(R.id.btnRefundRequest)
        
        findViewById<TextView>(R.id.txtTitle).text = "Chi tiết đơn hàng"
        rvItems.layoutManager = LinearLayoutManager(this)
    }

    private fun loadOrder() {
        val orderId = intent.getIntExtra("order_id", 0)
        val dbHelper = DatabaseHelper(this)
        val order = dbHelper.getOrderById(orderId)

        order?.let { o ->
            txtStatus.text = "Trạng thái: ${o.status}"
            txtReceiver.text = "${o.receiverName} - ${o.receiverPhone}"
            txtAddress.text = o.receiverAddress
            txtEst.text = "Dự kiến nhận hàng: ${o.estimatedDelivery}"
            
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            txtTotal.text = "${formatter.format(o.totalPrice)}đ"
            findViewById<TextView>(R.id.txtSubtotal).text = "${formatter.format(o.totalPrice)}đ"
            txtPayment.text = "Phương thức: ${o.paymentMethod}"

            if (o.status == "Đang xử lý") {
                btnCancel.visibility = View.VISIBLE
                btnCancel.setOnClickListener { showCancelDialog(o.id) }
                
                if (o.paymentMethod != "Tiền mặt (COD)") {
                    btnRefund.visibility = View.VISIBLE
                    btnRefund.setOnClickListener { showRefundDialog(o.id) }
                }
            } else {
                btnCancel.visibility = View.GONE
                btnRefund.visibility = View.GONE
            }
            
            rvItems.adapter = OrderItemsAdapter(o.items)
        }
    }

    private fun showCancelDialog(orderId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hủy đơn hàng")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
            .setPositiveButton("Xác nhận") { _, _ ->
                DatabaseHelper(this).updateOrderStatus(orderId, "Đã hủy")
                Toast.makeText(this, "Đơn hàng đã được hủy", Toast.LENGTH_SHORT).show()
                loadOrder()
            }
            .setNegativeButton("Quay lại", null)
            .show()
    }

    private fun showRefundDialog(orderId: Int) {
        val reasons = arrayOf("Không còn nhu cầu", "Tìm thấy giá rẻ hơn", "Thời gian giao quá lâu", "Khác")
        AlertDialog.Builder(this)
            .setTitle("Lý do hoàn tiền")
            .setItems(reasons) { _, which ->
                DatabaseHelper(this).updateOrderStatus(orderId, "Yêu cầu hoàn tiền", reasons[which])
                Toast.makeText(this, "Yêu cầu hoàn tiền đã được gửi!", Toast.LENGTH_LONG).show()
                loadOrder()
            }
            .show()
    }
}
