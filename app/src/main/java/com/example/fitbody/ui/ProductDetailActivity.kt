package com.example.fitbody.ui

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var imgProductDetail: ImageView
    private lateinit var txtProductNameDetail: TextView
    private lateinit var txtProductStatusDetail: TextView
    private lateinit var txtProductPriceDetail: TextView
    private lateinit var txtOriginalPriceDetail: TextView
    private lateinit var layoutGiftDetail: LinearLayout
    private lateinit var txtProductDescriptionDetail: TextView
    private lateinit var btnAddToCart: Button

    private var productId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        initViews()
        setupData()

        btnBack.setOnClickListener { finish() }
        btnAddToCart.setOnClickListener { addToCart() }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        imgProductDetail = findViewById(R.id.imgProductDetail)
        txtProductNameDetail = findViewById(R.id.txtProductNameDetail)
        txtProductStatusDetail = findViewById(R.id.txtProductStatusDetail)
        txtProductPriceDetail = findViewById(R.id.txtProductPriceDetail)
        txtOriginalPriceDetail = findViewById(R.id.txtOriginalPriceDetail)
        layoutGiftDetail = findViewById(R.id.layoutGiftDetail)
        txtProductDescriptionDetail = findViewById(R.id.txtProductDescriptionDetail)
        btnAddToCart = findViewById(R.id.btnAddToCart)
    }

    private fun setupData() {
        productId = intent.getIntExtra("product_id", 0)
        val name = intent.getStringExtra("product_name") ?: ""
        val price = intent.getIntExtra("product_price", 0)
        val originalPrice = intent.getIntExtra("product_original_price", 0)
        val image = intent.getStringExtra("product_image") ?: ""
        val description = intent.getStringExtra("product_description") ?: ""
        val available = intent.getBooleanExtra("product_available", true)
        val gift = intent.getBooleanExtra("product_gift", false)

        txtProductNameDetail.text = name
        txtProductDescriptionDetail.text = description
        
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        txtProductPriceDetail.text = formatter.format(price) + "đ"
        
        txtOriginalPriceDetail.text = formatter.format(originalPrice) + "đ"
        txtOriginalPriceDetail.paintFlags = txtOriginalPriceDetail.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        
        txtProductStatusDetail.text = if (available) "Tình trạng: Còn hàng" else "Tình trạng: Hết hàng"
        txtProductStatusDetail.setTextColor(if (available) 0xFF4CAF50.toInt() else 0xFFFF5252.toInt())
        
        layoutGiftDetail.visibility = if (gift) View.VISIBLE else View.GONE

        // Sửa logic tải ảnh từ Resource giống trang Shop
        val resId = resources.getIdentifier(image, "drawable", packageName)
        Glide.with(this)
            .load(if (resId != 0) resId else image)
            .placeholder(R.drawable.ic_launcher_background)
            .into(imgProductDetail)
    }

    private fun addToCart() {
        val session = SessionManager(this)
        val currentUserId = session.getUserId()

        if (currentUserId == 0) {
            Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DatabaseHelper(this)
        if (dbHelper.addToCart(currentUserId, productId)) {
            Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
        }
    }
}
