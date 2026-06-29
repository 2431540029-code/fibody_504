package com.example.fitbody.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.TrainerAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Trainer
import com.example.fitbody.ui.detail.TrainerDetailActivity
import com.example.fitbody.utils.SessionManager

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private lateinit var recyclerFavorite: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerFavorite = view.findViewById(R.id.recyclerFavorite)
        recyclerFavorite.layoutManager = LinearLayoutManager(requireContext())
        loadFavorites()
    }

    private fun loadFavorites() {
        val session = SessionManager(requireContext())
        val userId = session.getUserId()
        val dbHelper = DatabaseHelper(requireContext())
        val list = dbHelper.getFavorites(userId)

        recyclerFavorite.adapter = TrainerAdapter(
            list,
            { trainer ->
                val intent = Intent(requireContext(), TrainerDetailActivity::class.java)
                intent.putExtra("trainer_id", trainer.id)
                intent.putExtra("trainer_name", trainer.name)
                intent.putExtra("trainer_specialty", trainer.specialty)
                intent.putExtra("trainer_image", trainer.image)
                intent.putExtra("trainer_calories", trainer.calories)
                intent.putExtra("trainer_muscle", trainer.muscle)
                intent.putExtra("trainer_schedule", trainer.schedule)
                intent.putExtra("trainer_description", trainer.description)
                startActivity(intent)
            },
            { trainer ->
                removeFavorite(trainer.id)
            },
            { trainer ->
                toggleLike(trainer)
            }
        )
    }

    private fun toggleLike(trainer: Trainer) {
        val session = SessionManager(requireContext())
        val userId = session.getUserId()
        if (userId == 0) return

        val dbHelper = DatabaseHelper(requireContext())
        if (trainer.isLikedByMe) {
            if (dbHelper.removeLike(userId, trainer.id)) {
                Toast.makeText(requireContext(), "Đã bỏ thích", Toast.LENGTH_SHORT).show()
                loadFavorites()
            }
        } else {
            if (dbHelper.addLike(userId, trainer.id)) {
                Toast.makeText(requireContext(), "Cảm ơn bạn đã thích PT! 👍", Toast.LENGTH_SHORT).show()
                loadFavorites()
            }
        }
    }

    private fun removeFavorite(trainerId: Int) {
        val session = SessionManager(requireContext())
        val userId = session.getUserId()
        val dbHelper = DatabaseHelper(requireContext())
        if (dbHelper.removeFavorite(userId, trainerId)) {
            Toast.makeText(requireContext(), "Đã bỏ khỏi yêu thích", Toast.LENGTH_SHORT).show()
            loadFavorites()
        }
    }
}
