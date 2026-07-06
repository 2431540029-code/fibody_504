package com.example.fitbody.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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
    private lateinit var layoutEmptyCart: LinearLayout
    private lateinit var btnShopNow: Button

    private val cartList = ArrayList<CartItem>()
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        initViews()
        setupListeners()
        loadCart()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        recyclerCart = findViewById(R.id.recyclerCart)
        txtTotalPrice = findViewById(R.id.txtTotalPrice)
        btnCheckout = findViewById(R.id.btnCheckout)
        btnOrderHistory = findViewById(R.id.btnOrderHistory)
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart)
        btnShopNow = findViewById(R.id.btnShopNow)

        txtTitle.text = "Giỏ hàng"
        recyclerCart.layoutManager = LinearLayoutManager(this)
        
        adapter = CartAdapter(cartList) { calculateTotal() }
        recyclerCart.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnOrderHistory.setOnClickListener { startActivity(Intent(this, OrderHistoryActivity::class.java)) }
        btnShopNow.setOnClickListener { finish() }

        btnCheckout.setOnClickListener {
            val selectedItems = cartList.filter { it.isSelected }
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show()
            } else {
                val total = selectedItems.sumOf { it.price * it.quantity }
                val intent = Intent(this, CheckoutActivity::class.java)
                intent.putExtra("total_price", total)
                startActivity(intent)
            }
        }
    }

    private fun loadCart() {
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        val data = dbHelper.getCart(userId)

        cartList.clear()
        cartList.addAll(data)
        adapter.notifyDataSetChanged()
        
        if (data.isEmpty()) {
            recyclerCart.visibility = View.GONE
            layoutEmptyCart.visibility = View.VISIBLE
            btnCheckout.isEnabled = false
            btnCheckout.alpha = 0.5f
        } else {
            recyclerCart.visibility = View.VISIBLE
            layoutEmptyCart.visibility = View.GONE
            btnCheckout.isEnabled = true
            btnCheckout.alpha = 1.0f
        }
        
        calculateTotal()
    }

    private fun calculateTotal() {
        val total = cartList.filter { it.isSelected }.sumOf { it.price * it.quantity }
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        txtTotalPrice.text = "Tổng tiền: ${formatter.format(total)}đ"
    }
}
