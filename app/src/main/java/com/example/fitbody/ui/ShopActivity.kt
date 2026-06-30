package com.example.fitbody.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Product
import com.example.fitbody.adapter.ProductAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ShopActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var fabCart: FloatingActionButton
    private lateinit var recyclerProducts: RecyclerView
    private lateinit var layoutPagination: LinearLayout

    private val productList = ArrayList<Product>()
    private val allData = ArrayList<Product>() // Lưu toàn bộ data để lọc
    private lateinit var adapter: ProductAdapter
    
    private var currentPage = 1
    private val pageSize = 6 
    private var totalPages = 1
    private var currentCategory = "Tất cả"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        initViews()
        setupRecyclerView()
        setupCategoryFilters()
        
        btnBack.setOnClickListener { finish() }
        fabCart.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }

        loadPage(1)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        fabCart = findViewById(R.id.fabCart)
        recyclerProducts = findViewById(R.id.recyclerProducts)
        layoutPagination = findViewById(R.id.layoutPagination)
        txtTitle.text = "Cửa Hàng Thực Phẩm"
    }

    private fun setupCategoryFilters() {
        val cats = mapOf(
            R.id.catAll to "Tất cả",
            R.id.catProtein to "Protein",
            R.id.catGain to "Tăng cân",
            R.id.catLoss to "Giảm mỡ",
            R.id.catPower to "Tăng sức mạnh"
        )

        cats.forEach { (id, name) ->
            findViewById<TextView>(id).setOnClickListener { view ->
                currentCategory = name
                currentPage = 1
                
                // Cập nhật UI nút được chọn
                cats.keys.forEach { 
                    findViewById<TextView>(it).setBackgroundResource(R.drawable.bg_card_home)
                }
                view.setBackgroundResource(R.drawable.bg_card_service)
                
                loadPage(1)
            }
        }
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
        
        // Logic lấy data theo category và phân trang
        val rawData = dbHelper.getProductsByPage(1, 100) // Lấy hết để lọc logic (hoặc tối ưu query SQL)
        val filtered = if (currentCategory == "Tất cả") rawData 
                      else rawData.filter { it.category.contains(currentCategory, true) }
        
        totalPages = Math.ceil(filtered.size.toDouble() / pageSize).toInt()
        if (totalPages == 0) totalPages = 1
        
        val start = (page - 1) * pageSize
        val end = Math.min(start + pageSize, filtered.size)
        
        productList.clear()
        if (start < filtered.size) {
            productList.addAll(filtered.subList(start, end))
        }
        
        adapter.notifyDataSetChanged()
        setupPaginationButtons()
    }

    private fun setupPaginationButtons() {
        layoutPagination.removeAllViews()
        
        val btnPrev = Button(this)
        btnPrev.layoutParams = LinearLayout.LayoutParams(100, 100)
        btnPrev.text = "<"
        btnPrev.setBackgroundColor(Color.TRANSPARENT)
        btnPrev.setTextColor(if (currentPage > 1) Color.WHITE else Color.DKGRAY)
        btnPrev.setOnClickListener { if (currentPage > 1) loadPage(currentPage - 1) }
        layoutPagination.addView(btnPrev)

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

        val btnNext = Button(this)
        btnNext.layoutParams = LinearLayout.LayoutParams(100, 100)
        btnNext.text = ">"
        btnNext.setBackgroundColor(Color.TRANSPARENT)
        btnNext.setTextColor(if (currentPage < totalPages) Color.WHITE else Color.DKGRAY)
        btnNext.setOnClickListener { if (currentPage < totalPages) loadPage(currentPage + 1) }
        layoutPagination.addView(btnNext)
    }
}
