package com.example.fitbody.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.CartItem
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
    private var isConfirmedPaid = false 

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
                updateConfirmButtonState()
            } else {
                layoutPaymentApps.visibility = View.GONE
                btnConfirm.isEnabled = true
                btnConfirm.alpha = 1.0f
            }
        }

        findViewById<ImageButton>(R.id.btnMomo).setOnClickListener {
            openPaymentAppAndVerify("com.mservice.momotransfer", "https://momo.vn")
        }

        findViewById<ImageButton>(R.id.btnBankApp).setOnClickListener {
            openPaymentAppAndVerify("com.vietcombank.mobilebanking", "https://vietcombank.com.vn")
        }
    }

    private fun updateConfirmButtonState() {
        if (rgPayment.checkedRadioButtonId == R.id.rbBank) {
            btnConfirm.isEnabled = isConfirmedPaid
            btnConfirm.alpha = if (isConfirmedPaid) 1.0f else 0.5f
        }
    }

    private fun openPaymentAppAndVerify(packageName: String, webFallback: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        try {
            if (intent != null) startActivity(intent)
            else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webFallback)))
        } catch (e: Exception) {}

        // Khi người dùng quay lại từ app thanh toán, hiện Dialog xác nhận
        window.decorView.postDelayed({
            showPaymentConfirmationDialog()
        }, 1000)
    }

    private fun showPaymentConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage("Bạn đã hoàn tất việc chuyển khoản trên ứng dụng chưa?")
            .setCancelable(false)
            .setPositiveButton("Đã chuyển khoản") { _, _ ->
                isConfirmedPaid = true
                updateConfirmButtonState()
                Toast.makeText(this, "Xác nhận thành công! Bạn có thể đặt hàng.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy / Chưa chuyển") { _, _ ->
                isConfirmedPaid = false
                updateConfirmButtonState()
                Toast.makeText(this, "Thanh toán chưa hoàn tất. Vui lòng thử lại!", Toast.LENGTH_LONG).show()
            }
            .show()
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
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin nhận hàng", Toast.LENGTH_SHORT).show()
            return
        }

        val isBank = rgPayment.checkedRadioButtonId == R.id.rbBank
        if (isBank && !isConfirmedPaid) {
            Toast.makeText(this, "Vui lòng thực hiện chuyển khoản trước!", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentMethod = if (isBank) "Chuyển khoản (Đã xác nhận)" else "Tiền mặt (COD)"
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        
        val isDirectBuy = intent.getBooleanExtra("direct_buy", false)
        val itemsToBuy = if (isDirectBuy) {
            val prodId = intent.getIntExtra("prod_id", 0)
            val prodQty = intent.getIntExtra("prod_qty", 1)
            val allProd = dbHelper.getProductsByPage(1, 100)
            val p = allProd.find { it.id == prodId }
            if (p != null) listOf(CartItem(0, p.id, p.name, p.price, p.image, prodQty)) else emptyList()
        } else {
            dbHelper.getCart(userId).filter { it.isSelected }
        }

        if (itemsToBuy.isEmpty()) return

        val orderId = dbHelper.placeOrder(userId, totalPrice, itemsToBuy, paymentMethod, name, phone, address)

        if (orderId != -1L) {
            dbHelper.updateUserProfile(userId, name, email, null, phone, address)
            val successIntent = Intent(this, OrderSuccessActivity::class.java)
            successIntent.putExtra("order_id", orderId.toInt())
            startActivity(successIntent)
            finish()
        }
    }
}
