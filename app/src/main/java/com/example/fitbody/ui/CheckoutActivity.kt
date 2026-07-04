package com.example.fitbody.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
    private var isPaid = false // Giả lập trạng thái đã thanh toán online

    override fun onCreate(savedInstanceState: Bundle?) {
        val session = SessionManager(this)
        val targetMode = if (session.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        loadUserData()
        setupTotal()
        setupPaymentLogic()

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
        rgPayment = findViewById(R.id.rgPayment)
        layoutPaymentApps = findViewById(R.id.layoutPaymentApps)
        findViewById<TextView>(R.id.txtTitle).text = "Thanh toán"
    }

    private fun setupPaymentLogic() {
        rgPayment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbBank) {
                layoutPaymentApps.visibility = View.VISIBLE
                btnConfirm.isEnabled = false // Khóa nút nếu chưa nhấn vào app thanh toán
                btnConfirm.alpha = 0.5f
            } else {
                layoutPaymentApps.visibility = View.GONE
                btnConfirm.isEnabled = true
                btnConfirm.alpha = 1.0f
            }
        }

        // Mô phỏng nhấn vào Momo
        findViewById<ImageButton>(R.id.btnMomo).setOnClickListener {
            openPaymentApp("com.mservice.momotransfer", "https://momo.vn")
        }

        // Mô phỏng nhấn vào app Ngân hàng (VCB chẳng hạn)
        findViewById<ImageButton>(R.id.btnBankApp).setOnClickListener {
            openPaymentApp("com.vietcombank.mobilebanking", "https://vietcombank.com.vn")
        }
    }

    private fun openPaymentApp(packageName: String, webFallback: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            // Nếu không có app thì mở trang web
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webFallback)))
        }
        
        // Sau khi mở app, coi như đã thanh toán (để demo)
        isPaid = true
        btnConfirm.isEnabled = true
        btnConfirm.alpha = 1.0f
        Toast.makeText(this, "Vui lòng hoàn tất giao dịch trên ứng dụng và quay lại đây!", Toast.LENGTH_LONG).show()
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
        
        val cartItems = dbHelper.getCart(userId).filter { it.isSelected }
        val orderId = dbHelper.placeOrder(userId, totalPrice, cartItems, paymentMethod, name, phone, address)

        if (orderId != -1L) {
            dbHelper.updateUserProfile(userId, name, email, null, phone, address)
            val intent = Intent(this, OrderSuccessActivity::class.java)
            intent.putExtra("order_id", orderId.toInt())
            startActivity(intent)
            finish()
        }
    }
}
