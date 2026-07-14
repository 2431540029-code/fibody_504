package com.example.fitbody.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.TrainerAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private lateinit var recyclerFavorite: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnFind: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerFavorite = view.findViewById(R.id.recyclerFavorite)
        layoutEmpty = view.findViewById(R.id.layoutEmptyFavorite)
        btnFind = view.findViewById(R.id.btnFindTrainer)

        recyclerFavorite.layoutManager = LinearLayoutManager(requireContext())

        btnFind.setOnClickListener {
            // Chuyển sang tab Home
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_home
        }

        loadFavorites()
    }

    private fun loadFavorites() {
        val userId = SessionManager(requireContext()).getUserId()
        val dbHelper = DatabaseHelper(requireContext())

        lifecycleScope.launch(Dispatchers.IO) {
            val list = dbHelper.getFavorites(userId)
            withContext(Dispatchers.Main) {
                if (list.isEmpty()) {
                    recyclerFavorite.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                } else {
                    recyclerFavorite.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    
                    recyclerFavorite.adapter = TrainerAdapter(ArrayList(list), { t ->
                        val intent = Intent(requireContext(), com.example.fitbody.ui.detail.TrainerDetailActivity::class.java)
                        intent.putExtra("trainer_id", t.id)
                        intent.putExtra("trainer_name", t.name)
                        intent.putExtra("trainer_image", t.image)
                        startActivity(intent)
                    }, { t -> 
                        dbHelper.removeFavorite(userId, t.id)
                        loadFavorites()
                    }, { t -> /* Like logic */ })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }
}
