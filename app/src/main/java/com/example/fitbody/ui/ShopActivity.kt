package com.example.fitbody.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Product
import com.example.fitbody.adapter.ProductAdapter

class ShopActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var btnCart: ImageButton
    private lateinit var recyclerProducts: RecyclerView
    private lateinit var layoutPagination: LinearLayout

    private val productList = ArrayList<Product>()
    private lateinit var adapter: ProductAdapter
    
    private var currentPage = 1
    private val pageSize = 6 // Hiển thị 6 sản phẩm mỗi trang

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        initViews()
        setupRecyclerView()
        
        btnBack.setOnClickListener { finish() }
        btnCart.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }

        loadPage(1)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        btnCart = findViewById(R.id.btnCart)
        recyclerProducts = findViewById(R.id.recyclerProducts)
        layoutPagination = findViewById(R.id.layoutPagination)
        txtTitle.text = "Cửa Hàng Thực Phẩm"
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(productList) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product_id", product.id)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_price", product.price)
            intent.putExtra("product_original_price", product.originalPrice)
            intent.putExtra("product_image", product.image)
            intent.putExtra("product_description", product.description)
            intent.putExtra("product_category", product.category)
            intent.putExtra("product_available", product.isAvailable)
            intent.putExtra("product_gift", product.hasGift)
            startActivity(intent)
        }
        recyclerProducts.layoutManager = GridLayoutManager(this, 2)
        recyclerProducts.adapter = adapter
    }

    private fun loadPage(page: Int) {
        currentPage = page
        val dbHelper = DatabaseHelper(this)
        val data = dbHelper.getProductsByPage(page, pageSize)

        productList.clear()
        productList.addAll(data)
        adapter.notifyDataSetChanged()
        
        setupPaginationButtons()
    }

    private fun setupPaginationButtons() {
        layoutPagination.removeAllViews()
        val dbHelper = DatabaseHelper(this)
        val totalProducts = dbHelper.getTotalProductCount()
        val totalPages = Math.ceil(totalProducts.toDouble() / pageSize).toInt()

        for (i in 1..totalPages) {
            val btn = Button(this)
            val params = LinearLayout.LayoutParams(100, 100)
            params.setMargins(8, 0, 8, 0)
            btn.layoutParams = params
            btn.text = i.toString()
            btn.textSize = 14f
            btn.gravity = Gravity.CENTER
            btn.setPadding(0, 0, 0, 0)
            
            if (i == currentPage) {
                btn.setBackgroundColor(Color.parseColor("#7C4DFF"))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor(Color.parseColor("#333333"))
                btn.setTextColor(Color.GRAY)
            }

            btn.setOnClickListener { loadPage(i) }
            layoutPagination.addView(btn)
        }
    }
}
