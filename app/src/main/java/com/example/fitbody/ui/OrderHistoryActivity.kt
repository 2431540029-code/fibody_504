package com.example.fitbody.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.OrderAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var recyclerOrderHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        recyclerOrderHistory = findViewById(R.id.recyclerOrderHistory)

        txtTitle.text = "Lịch sử mua hàng"

        btnBack.setOnClickListener { finish() }

        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        
        lifecycleScope.launch(Dispatchers.IO) {
            val orders = dbHelper.getOrderHistory(userId)
            withContext(Dispatchers.Main) {
                recyclerOrderHistory.layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
                recyclerOrderHistory.adapter = OrderAdapter(orders)
            }
        }
    }
}
