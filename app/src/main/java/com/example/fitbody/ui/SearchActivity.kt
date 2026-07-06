package com.example.fitbody.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.TrainerAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Trainer
import com.example.fitbody.ui.detail.TrainerDetailActivity
import com.example.fitbody.utils.SessionManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var edtSearch: EditText
    private lateinit var rvResults: RecyclerView
    private lateinit var txtCount: TextView
    private lateinit var layoutHotSearch: LinearLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        dbHelper = DatabaseHelper(this)
        userId = SessionManager(this).getUserId()
        
        edtSearch = findViewById(R.id.edtSearchInput)
        rvResults = findViewById(R.id.rvSearchResults)
        txtCount = findViewById(R.id.txtResultCount)
        layoutHotSearch = findViewById(R.id.layoutHotSearch)
        chipGroup = findViewById(R.id.chipGroupHotSearch)
        
        rvResults.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBackSearch).setOnClickListener { finish() }

        setupHotSearch()

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                performSearch(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edtSearch.text.toString().trim())
                true
            } else false
        }
        
        val initialQuery = intent.getStringExtra("query") ?: ""
        if (initialQuery.isNotEmpty()) {
            edtSearch.setText(initialQuery)
        }
    }

    private fun setupHotSearch() {
        // Danh sách từ khóa đảm bảo 100% có kết quả trong app hiện tại
        val hotKeywords = listOf("Ngực", "Chân", "Mông", "Bụng", "Yoga", "Cardio", "Boxing", "Sức mạnh")
        
        hotKeywords.forEach { keyword ->
            val chip = Chip(this).apply {
                text = keyword
                isClickable = true
                isCheckable = false
                setTextColor(android.graphics.Color.WHITE)
                setChipBackgroundColorResource(R.color.card_background)
                setOnClickListener {
                    edtSearch.setText(keyword)
                    edtSearch.setSelection(keyword.length)
                    performSearch(keyword)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            rvResults.adapter = null
            txtCount.visibility = View.GONE
            layoutHotSearch.visibility = View.VISIBLE
            return
        }

        // Ẩn các gợi ý khi đang hiện kết quả
        layoutHotSearch.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            val results = dbHelper.searchTrainers(query, userId)
            withContext(Dispatchers.Main) {
                if (results.isEmpty()) {
                    txtCount.visibility = View.VISIBLE
                    txtCount.text = "Không tìm thấy kết quả nào cho \"$query\""
                    rvResults.adapter = null
                } else {
                    txtCount.visibility = View.VISIBLE
                    txtCount.text = "Tìm thấy ${results.size} huấn luyện viên phù hợp"
                    
                    val adapter = TrainerAdapter(ArrayList(results), { t ->
                        val intent = Intent(this@SearchActivity, TrainerDetailActivity::class.java)
                        intent.putExtra("trainer_id", t.id)
                        intent.putExtra("trainer_name", t.name)
                        intent.putExtra("trainer_image", t.image)
                        startActivity(intent)
                    }, { t -> 
                        if (userId != 0) {
                            if (dbHelper.addFavorite(userId, t.id)) {
                                Toast.makeText(this@SearchActivity, "Đã thêm vào yêu thích ❤️", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, { t -> 
                        toggleLike(t, query)
                    })
                    rvResults.adapter = adapter
                }
            }
        }
    }

    private fun toggleLike(trainer: Trainer, query: String) {
        if (userId == 0) return
        lifecycleScope.launch(Dispatchers.IO) {
            val success = if (trainer.isLikedByMe) {
                dbHelper.removeLike(userId, trainer.id)
            } else {
                dbHelper.addLike(userId, trainer.id)
            }
            if (success) {
                withContext(Dispatchers.Main) {
                    performSearch(query)
                }
            }
        }
    }
}
