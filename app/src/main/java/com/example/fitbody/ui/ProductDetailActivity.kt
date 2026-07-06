package com.example.fitbody.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var imgProductDetail: ImageView
    private lateinit var txtProductNameDetail: TextView
    private lateinit var txtProductPriceDetail: TextView
    private lateinit var btnAddToCart: Button
    private lateinit var btnBuyNow: Button

    private var productId = 0
    private var productName = ""
    private var productPrice = 0
    private var productImage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        initViews()
        setupData()

        btnBack.setOnClickListener { finish() }
        btnAddToCart.setOnClickListener { showQuantitySheet(isBuyNow = false) }
        btnBuyNow.setOnClickListener { showQuantitySheet(isBuyNow = true) }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        imgProductDetail = findViewById(R.id.imgProductDetail)
        txtProductNameDetail = findViewById(R.id.txtProductNameDetail)
        txtProductPriceDetail = findViewById(R.id.txtProductPriceDetail)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        btnBuyNow = findViewById(R.id.btnBuyNow)
    }

    private fun setupData() {
        productId = intent.getIntExtra("product_id", 0)
        productName = intent.getStringExtra("product_name") ?: ""
        productPrice = intent.getIntExtra("product_price", 0)
        productImage = intent.getStringExtra("product_image") ?: ""
        
        txtProductNameDetail.text = productName
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        txtProductPriceDetail.text = formatter.format(productPrice) + "đ"

        val resId = resources.getIdentifier(productImage, "drawable", packageName)
        Glide.with(this).load(if (resId != 0) resId else productImage).into(imgProductDetail)
    }

    private fun showQuantitySheet(isBuyNow: Boolean) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_add_to_cart_sheet, null)
        
        val img = view.findViewById<ImageView>(R.id.imgSheetProduct)
        val txtPrice = view.findViewById<TextView>(R.id.txtSheetPrice)
        val txtQty = view.findViewById<TextView>(R.id.txtSheetQty)
        val btnMinus = view.findViewById<TextView>(R.id.btnSheetMinus)
        val btnPlus = view.findViewById<TextView>(R.id.btnSheetPlus)
        val btnConfirm = view.findViewById<Button>(R.id.btnSheetConfirm)

        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        txtPrice.text = formatter.format(productPrice) + "đ"
        
        val resId = resources.getIdentifier(productImage, "drawable", packageName)
        Glide.with(this).load(if (resId != 0) resId else productImage).into(img)

        var quantity = 1
        btnMinus.setOnClickListener { if (quantity > 1) { quantity--; txtQty.text = quantity.toString() } }
        btnPlus.setOnClickListener { quantity++; txtQty.text = quantity.toString() }

        btnConfirm.setOnClickListener {
            if (isBuyNow) {
                val intent = Intent(this, CheckoutActivity::class.java)
                intent.putExtra("total_price", productPrice * quantity)
                intent.putExtra("direct_buy", true)
                intent.putExtra("prod_id", productId)
                intent.putExtra("prod_qty", quantity)
                startActivity(intent)
            } else {
                addToCart(quantity)
            }
            dialog.dismiss()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun addToCart(qty: Int) {
        val session = SessionManager(this)
        val userId = session.getUserId()
        if (userId == 0) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dbHelper = DatabaseHelper(this)
        if (dbHelper.addToCart(userId, productId, qty)) {
            Toast.makeText(this, "Đã thêm $qty sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
        }
    }
}
