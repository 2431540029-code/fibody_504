package com.example.fitbody.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Product
import com.example.fitbody.adapter.ProductAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShopActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var btnCart: Button
    private lateinit var recyclerProducts: RecyclerView

    private val productList = ArrayList<Product>()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        btnCart = findViewById(R.id.btnCart)
        recyclerProducts = findViewById(R.id.recyclerProducts)

        txtTitle.text = "Shop FitBody"

        adapter = ProductAdapter(productList) { product ->
            val intent =
                Intent(
                    this,
                    ProductDetailActivity::class.java
                )

            intent.putExtra("product_id", product.id)
            intent.putExtra("product_name", product.name)
            intent.putExtra("product_price", product.price)
            intent.putExtra("product_image", product.image)
            intent.putExtra("product_description", product.description)
            intent.putExtra("product_category", product.category)

            startActivity(intent)
        }

        recyclerProducts.layoutManager =
            LinearLayoutManager(this)

        recyclerProducts.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        btnCart.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CartActivity::class.java
                )
            )
        }

        loadProducts()
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    private fun loadProducts() {
        val dbHelper = DatabaseHelper(this)
        val data = dbHelper.getAllProducts()

        productList.clear()
        productList.addAll(data)
        adapter.notifyDataSetChanged()

        if (data.isEmpty()) {
            Toast.makeText(this, "Chưa có sản phẩm trong SQLite", Toast.LENGTH_SHORT).show()
        }
    }
}