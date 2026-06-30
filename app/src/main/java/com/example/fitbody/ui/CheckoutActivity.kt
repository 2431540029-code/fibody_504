package com.example.fitbody.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var txtTotal: TextView
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        loadUserData()
        setupTotal()

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        btnConfirm.setOnClickListener { processOrder() }
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtCheckoutName)
        edtEmail = findViewById(R.id.edtCheckoutEmail)
        edtPhone = findViewById(R.id.edtCheckoutPhone)
        edtAddress = findViewById(R.id.edtCheckoutAddress)
        txtTotal = findViewById(R.id.txtCheckoutTotal)
        btnConfirm = findViewById(R.id.btnConfirmOrder)
        findViewById<TextView>(R.id.txtTitle).text = "Thanh toán"
    }

    private fun loadUserData() {
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        val cursor = dbHelper.getUserProfile(userId)
        if (cursor.moveToFirst()) {
            edtName.setText(cursor.getString(0))
            edtEmail.setText(cursor.getString(1))
            edtPhone.setText(cursor.getString(3))
            edtAddress.setText(cursor.getString(4))
        }
        cursor.close()
    }

    private fun setupTotal() {
        val total = intent.getIntExtra("total_price", 0)
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        txtTotal.text = "${formatter.format(total)}đ"
    }

    private fun processOrder() {
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin nhận hàng", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulating order placement
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        
        // Update user info for future use
        dbHelper.updateUserProfile(userId, name, edtEmail.text.toString(), null, phone, address)

        Toast.makeText(this, "Đặt hàng thành công! Cảm ơn bạn đã mua sắm.", Toast.LENGTH_LONG).show()
        finish()
    }
}
