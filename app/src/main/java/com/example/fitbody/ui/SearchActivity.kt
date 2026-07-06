package com.example.fitbody.ui

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.TrainerAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.ui.detail.TrainerDetailActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var edtSearch: EditText
    private lateinit var rvResults: RecyclerView
    private lateinit var txtCount: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        dbHelper = DatabaseHelper(this)
        
        edtSearch = findViewById(R.id.edtSearchInput)
        rvResults = findViewById(R.id.rvSearchResults)
        txtCount = findViewById(R.id.txtResultCount)
        
        rvResults.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBackSearch).setOnClickListener { finish() }

        // Nhận từ khóa ban đầu từ trang Home (nếu có)
        val initialQuery = intent.getStringExtra("query") ?: ""
        if (initialQuery.isNotEmpty()) {
            edtSearch.setText(initialQuery)
            performSearch(initialQuery)
        }

        // Tìm kiếm khi gõ
        edtSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Tìm kiếm khi nhấn Enter/Nút tìm kiếm trên bàn phím
        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edtSearch.text.toString())
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        if (query.trim().length < 2) {
            rvResults.adapter = null
            txtCount.visibility = android.view.View.GONE
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val results = dbHelper.searchTrainers(query)
            withContext(Dispatchers.Main) {
                txtCount.visibility = android.view.View.VISIBLE
                txtCount.text = "Tìm thấy ${results.size} huấn luyện viên phù hợp"
                
                rvResults.adapter = TrainerAdapter(ArrayList(results), { t ->
                    val intent = Intent(this@SearchActivity, TrainerDetailActivity::class.java)
                    intent.putExtra("trainer_id", t.id)
                    intent.putExtra("trainer_name", t.name)
                    intent.putExtra("trainer_image", t.image)
                    startActivity(intent)
                }, { t -> 
                    dbHelper.addFavorite(1, t.id) // Placeholder userId
                }, { t -> 
                    // Like logic
                })
            }
        }
    }
}
