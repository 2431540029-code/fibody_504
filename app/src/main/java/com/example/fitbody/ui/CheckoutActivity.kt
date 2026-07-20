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
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var txtTotal: TextView
    private lateinit var btnNext: Button
    private lateinit var btnConfirm: Button
    private lateinit var rgPayment: RadioGroup
    private lateinit var layoutPaymentApps: LinearLayout
    
    private lateinit var layoutStep1: View
    private lateinit var layoutStep2: View
    private lateinit var layoutStep3: View
    
    private lateinit var step1Indicator: TextView
    private lateinit var step2Indicator: TextView
    private lateinit var step3Indicator: TextView
    private lateinit var line1: View
    private lateinit var line2: View

    private lateinit var txtSummaryUser: TextView
    private lateinit var txtSummaryAddress: TextView
    private lateinit var txtSummaryPayment: TextView
    
    private var totalPrice = 0
    private var isConfirmedPaid = false 
    private var currentStep = 1

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

        findViewById<TextView>(R.id.btnBack).setOnClickListener { 
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            } else {
                finish()
            }
        }

        btnNext.setOnClickListener { handleNextStep() }
        btnConfirm.setOnClickListener { processOrder() }
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtCheckoutName)
        edtPhone = findViewById(R.id.edtCheckoutPhone)
        edtAddress = findViewById(R.id.edtCheckoutAddress)
        txtTotal = findViewById(R.id.txtCheckoutTotal)
        btnNext = findViewById(R.id.btnNextStep)
        btnConfirm = findViewById(R.id.btnConfirmOrder)
        rgPayment = findViewById(R.id.rgPayment)
        layoutPaymentApps = findViewById(R.id.layoutPaymentApps)
        
        layoutStep1 = findViewById(R.id.layoutStep1)
        layoutStep2 = findViewById(R.id.layoutStep2)
        layoutStep3 = findViewById(R.id.layoutStep3)
        
        step1Indicator = findViewById(R.id.step1Indicator)
        step2Indicator = findViewById(R.id.step2Indicator)
        step3Indicator = findViewById(R.id.step3Indicator)
        line1 = findViewById(R.id.line1)
        line2 = findViewById(R.id.line2)

        txtSummaryUser = findViewById(R.id.txtSummaryUser)
        txtSummaryAddress = findViewById(R.id.txtSummaryAddress)
        txtSummaryPayment = findViewById(R.id.txtSummaryPayment)
        
        findViewById<TextView>(R.id.txtTitle).text = "Thanh toán"
    }

    private fun handleNextStep() {
        when (currentStep) {
            1 -> {
                if (edtName.text.isEmpty() || edtPhone.text.isEmpty() || edtAddress.text.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đủ thông tin giao hàng", Toast.LENGTH_SHORT).show()
                    return
                }
                currentStep = 2
                updateStepUI()
            }
            2 -> {
                if (rgPayment.checkedRadioButtonId == R.id.rbBank && !isConfirmedPaid) {
                    Toast.makeText(this, "Vui lòng thực hiện chuyển khoản trước!", Toast.LENGTH_SHORT).show()
                    return
                }
                currentStep = 3
                updateStepUI()
            }
        }
    }

    private fun updateStepUI() {
        layoutStep1.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        layoutStep2.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
        layoutStep3.visibility = if (currentStep == 3) View.VISIBLE else View.GONE
        
        btnNext.visibility = if (currentStep < 3) View.VISIBLE else View.GONE
        btnConfirm.visibility = if (currentStep == 3) View.VISIBLE else View.GONE
        
        // Update Indicator Colors
        val activeColor = android.graphics.Color.parseColor("#7C4DFF")
        val inactiveColor = android.graphics.Color.parseColor("#333333")
        
        step1Indicator.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currentStep >= 1) activeColor else inactiveColor)
        line1.backgroundColor = if (currentStep >= 2) activeColor else inactiveColor
        step2Indicator.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currentStep >= 2) activeColor else inactiveColor)
        line2.backgroundColor = if (currentStep >= 3) activeColor else inactiveColor
        step3Indicator.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currentStep >= 3) activeColor else inactiveColor)
        
        if (currentStep == 3) {
            updateSummary()
        }
    }

    // Helper extension to change background color of a view
    private var View.backgroundColor: Int
        get() = 0 // Not actually used
        set(value) { setBackgroundColor(value) }

    private fun updateSummary() {
        txtSummaryUser.text = "Người nhận: ${edtName.text}"
        txtSummaryAddress.text = "Địa chỉ: ${edtAddress.text}"
        val method = if (rgPayment.checkedRadioButtonId == R.id.rbCOD) "Tiền mặt (COD)" else "Chuyển khoản (Đã xác nhận)"
        txtSummaryPayment.text = "Thanh toán: $method"
    }

    private fun setupPaymentLogic() {
        rgPayment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbBank) {
                layoutPaymentApps.visibility = View.VISIBLE
            } else {
                layoutPaymentApps.visibility = View.GONE
            }
        }

        findViewById<ImageButton>(R.id.btnMomo).setOnClickListener {
            openPaymentAppAndVerify("com.mservice.momotransfer", "https://momo.vn")
        }

        findViewById<ImageButton>(R.id.btnBankApp).setOnClickListener {
            openPaymentAppAndVerify("com.vietcombank.mobilebanking", "https://vietcombank.com.vn")
        }
    }

    private fun openPaymentAppAndVerify(packageName: String, webFallback: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        try {
            if (intent != null) startActivity(intent)
            else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webFallback)))
        } catch (e: Exception) {}

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
                Toast.makeText(this, "Xác nhận thành công! Nhấn Tiếp tục để kiểm tra đơn.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy / Chưa chuyển") { _, _ ->
                isConfirmedPaid = false
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
        val phone = edtPhone.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        val paymentMethod = if (rgPayment.checkedRadioButtonId == R.id.rbBank) "Chuyển khoản (Đã xác nhận)" else "Tiền mặt (COD)"
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
            val successIntent = Intent(this, OrderSuccessActivity::class.java)
            successIntent.putExtra("order_id", orderId.toInt())
            startActivity(successIntent)
            finish()
        }
    }
}
