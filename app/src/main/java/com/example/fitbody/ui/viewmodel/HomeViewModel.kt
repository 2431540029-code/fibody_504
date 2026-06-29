package com.example.fitbody.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.LeaderboardUser
import com.example.fitbody.model.Trainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    private val _topTrainers = MutableLiveData<List<Trainer>>()
    val topTrainers: LiveData<List<Trainer>> get() = _topTrainers

    private val _allTrainers = MutableLiveData<List<Trainer>>()
    val allTrainers: LiveData<List<Trainer>> get() = _allTrainers

    private val _leaderboard = MutableLiveData<List<LeaderboardUser>>()
    val leaderboard: LiveData<List<LeaderboardUser>> get() = _leaderboard

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    fun loadData(userId: Int) {
        _isRefreshing.value = true
        viewModelScope.launch {
            val top = withContext(Dispatchers.IO) {
                dbHelper.getTopFavoriteTrainers(userId)
            }
            _topTrainers.value = top

            val random = withContext(Dispatchers.IO) {
                dbHelper.getRandomTrainers(userId)
            }
            
            // Lọc bỏ những PT đã xuất hiện ở danh sách Top để tránh trùng lặp
            val topIds = top.map { it.id }.toSet()
            val filteredRandom = random.filter { it.id !in topIds }.take(6)
            _allTrainers.value = filteredRandom

            val board = withContext(Dispatchers.IO) {
                dbHelper.getLeaderboard()
            }
            _leaderboard.value = board
            _isRefreshing.value = false
        }
    }
}
