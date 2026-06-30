package com.example.fitbody.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.MainActivity
import com.example.fitbody.R

class OrderSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_success)

        val orderId = intent.getIntExtra("order_id", 0)

        findViewById<Button>(R.id.btnViewOrderDetail).setOnClickListener {
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("order_id", orderId)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnGoHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
