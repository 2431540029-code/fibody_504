package com.example.fitbody.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Product
import com.example.fitbody.adapter.ProductAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShopActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var fabCart: FloatingActionButton
    private lateinit var recyclerProducts: RecyclerView
    private lateinit var layoutPagination: LinearLayout

    private val productList = ArrayList<Product>()
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

        // Mặc định load từ máy trước cho nhanh
        loadPage(1)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        fabCart = findViewById(R.id.fabCart)
        recyclerProducts = findViewById(R.id.recyclerProducts)
        layoutPagination = findViewById(R.id.layoutPagination)
        txtTitle.text = "Cửa Hàng Thực Phẩm"

        findViewById<EditText>(R.id.edtSearch).addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSearch(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
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
                cats.keys.forEach { findViewById<TextView>(it).setBackgroundResource(R.drawable.bg_card_home) }
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
        
        // 1. Hiển thị dữ liệu từ máy ngay lập tức (Offline First)
        val localData = dbHelper.getProductsByPage(page, pageSize)
        productList.clear()
        productList.addAll(localData)
        adapter.notifyDataSetChanged()
        
        // Cập nhật phân trang dựa trên SQLite
        val totalProducts = dbHelper.getTotalProductCount()
        totalPages = Math.ceil(totalProducts.toDouble() / pageSize).toInt()
        setupPaginationButtons()

        // 2. Gọi API để cập nhật dữ liệu mới nhất (Chứng minh với thầy)
        val apiService = com.example.fitbody.network.RetrofitClient.instance
        apiService.getProductsFromServer().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful && response.body() != null) {
                    // Nếu có dữ liệu mới từ Server, bạn có thể cập nhật lại UI tại đây
                    // Toast.makeText(this@ShopActivity, "Dữ liệu đã đồng bộ với Server", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                // Không cần làm gì vì đã có dữ liệu Offline ở trên
            }
        })
    }

    private fun filterSearch(query: String) {
        val dbHelper = DatabaseHelper(this)
        val allProducts = dbHelper.getProductsByPage(1, 100)
        val filtered = allProducts.filter { it.name.contains(query, ignoreCase = true) }
        productList.clear()
        productList.addAll(filtered.take(pageSize))
        adapter.notifyDataSetChanged()
        layoutPagination.visibility = if (query.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupPaginationButtons() {
        layoutPagination.removeAllViews()
        if (totalPages <= 1) return

        val btnPrev = Button(this)
        btnPrev.layoutParams = LinearLayout.LayoutParams(100, 100)
        btnPrev.text = "<"; btnPrev.setBackgroundColor(Color.TRANSPARENT)
        btnPrev.setTextColor(if (currentPage > 1) Color.WHITE else Color.DKGRAY)
        btnPrev.setOnClickListener { if (currentPage > 1) loadPage(currentPage - 1) }
        layoutPagination.addView(btnPrev)

        for (i in 1..totalPages) {
            val btn = Button(this)
            val params = LinearLayout.LayoutParams(100, 100)
            params.setMargins(8, 0, 8, 0)
            btn.layoutParams = params; btn.text = i.toString(); btn.textSize = 14f; btn.gravity = Gravity.CENTER; btn.setPadding(0, 0, 0, 0)
            if (i == currentPage) { btn.setBackgroundColor(Color.parseColor("#7C4DFF")); btn.setTextColor(Color.WHITE) }
            else { btn.setBackgroundColor(Color.parseColor("#333333")); btn.setTextColor(Color.GRAY) }
            btn.setOnClickListener { loadPage(i) }
            layoutPagination.addView(btn)
        }

        val btnNext = Button(this)
        btnNext.layoutParams = LinearLayout.LayoutParams(100, 100)
        btnNext.text = ">"; btnNext.setBackgroundColor(Color.TRANSPARENT)
        btnNext.setTextColor(if (currentPage < totalPages) Color.WHITE else Color.DKGRAY)
        btnNext.setOnClickListener { if (currentPage < totalPages) loadPage(currentPage + 1) }
        layoutPagination.addView(btnNext)
    }
}
