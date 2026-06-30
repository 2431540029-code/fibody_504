package com.example.fitbody.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
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
    private lateinit var rgPayment: RadioGroup
    private lateinit var layoutPaymentApps: LinearLayout

    private var totalPrice = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        loadUserData()
        setupTotal()

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        
        rgPayment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbBank) {
                layoutPaymentApps.visibility = View.VISIBLE
            } else {
                layoutPaymentApps.visibility = View.GONE
            }
        }

        btnConfirm.setOnClickListener { processOrder() }
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtCheckoutName)
        edtEmail = findViewById(R.id.edtCheckoutEmail)
        edtPhone = findViewById(R.id.edtCheckoutPhone)
        edtAddress = findViewById(R.id.edtCheckoutAddress)
        txtTotal = findViewById(R.id.txtCheckoutTotal)
        btnConfirm = findViewById(R.id.btnConfirmOrder)
        rgPayment = findViewById(R.id.rgPayment)
        layoutPaymentApps = findViewById(R.id.layoutPaymentApps)
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
        totalPrice = intent.getIntExtra("total_price", 0)
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        txtTotal.text = "${formatter.format(totalPrice)}đ"
    }

    private fun processOrder() {
        val name = edtName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentMethod = if (rgPayment.checkedRadioButtonId == R.id.rbCOD) "Tiền mặt (COD)" else "Chuyển khoản"
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        
        // Save order and clear cart
        // First get items from cart that are selected
        val cartItems = dbHelper.getCart(userId).filter { it.isSelected }
        
        val orderId = dbHelper.placeOrder(userId, totalPrice, cartItems, paymentMethod, name, phone, address)

        if (orderId != -1L) {
            // Update user profile
            dbHelper.updateUserProfile(userId, name, email, null, phone, address)
            
            val intent = Intent(this, OrderSuccessActivity::class.java)
            intent.putExtra("order_id", orderId.toInt())
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi đặt hàng", Toast.LENGTH_SHORT).show()
        }
    }
}
