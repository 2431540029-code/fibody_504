package com.example.fitbody.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.CartItem
import com.example.fitbody.adapter.CartAdapter
import com.example.fitbody.utils.SessionManager
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var recyclerCart: RecyclerView
    private lateinit var txtTotalPrice: TextView
    private lateinit var btnCheckout: Button
    private lateinit var btnOrderHistory: Button

    private val cartList = ArrayList<CartItem>()
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        recyclerCart = findViewById(R.id.recyclerCart)
        txtTotalPrice = findViewById(R.id.txtTotalPrice)
        btnCheckout = findViewById(R.id.btnCheckout)
        btnOrderHistory = findViewById(R.id.btnOrderHistory)

        txtTitle.text = "Giỏ hàng"

        adapter = CartAdapter(cartList) {
            calculateTotal()
        }

        recyclerCart.layoutManager = LinearLayoutManager(this)
        recyclerCart.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnOrderHistory.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        btnCheckout.setOnClickListener {
            val selectedItems = cartList.filter { it.isSelected }
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show()
            } else {
                showCheckoutConfirmation(selectedItems)
            }
        }

        loadCart()
    }

    private fun loadCart() {
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        val data = dbHelper.getCart(userId)

        cartList.clear()
        cartList.addAll(data)
        adapter.notifyDataSetChanged()
        calculateTotal()

        if (data.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateTotal() {
        val total = cartList.filter { it.isSelected }.sumOf { it.price * it.quantity }
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        txtTotalPrice.text = "Tổng tiền: ${formatter.format(total)}đ"
    }

    private fun showCheckoutConfirmation(selectedItems: List<CartItem>) {
        val total = selectedItems.sumOf { it.price * it.quantity }
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage("Bạn chọn mua ${selectedItems.size} sản phẩm.\nTổng cộng: ${formatter.format(total)}đ")
            .setPositiveButton("Xác nhận đặt hàng") { _, _ ->
                processCheckout(selectedItems, total)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun processCheckout(selectedItems: List<CartItem>, total: Int) {
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        
        if (dbHelper.placeOrder(userId, total, selectedItems)) {
            Toast.makeText(this, "Đặt hàng thành công! Đang chuẩn bị giao hàng.", Toast.LENGTH_LONG).show()
            loadCart() // Tải lại để mất những món đã mua
        } else {
            Toast.makeText(this, "Lỗi khi xử lý đơn hàng", Toast.LENGTH_SHORT).show()
        }
    }
}
