package com.example.fitbody.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.LeaderboardAdapter
import com.example.fitbody.adapter.TrainerAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Trainer
import com.example.fitbody.ui.BMICalculatorActivity
import com.example.fitbody.ui.CheckInActivity
import com.example.fitbody.ui.PremiumPlanActivity
import com.example.fitbody.ui.ShopActivity
import com.example.fitbody.ui.WorkoutStatsActivity
import com.example.fitbody.ui.detail.TrainerDetailActivity
import com.example.fitbody.ui.viewmodel.HomeViewModel
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var recyclerTrainerTop: RecyclerView
    private lateinit var recyclerTrainerAll: RecyclerView
    private lateinit var recyclerLeaderboard: RecyclerView
    private lateinit var trainerAdapterTop: TrainerAdapter
    private lateinit var trainerAdapterAll: TrainerAdapter
    private val trainerListTop = ArrayList<Trainer>()
    private val trainerListAll = ArrayList<Trainer>()

    private lateinit var viewModel: HomeViewModel
    private lateinit var swipeRefreshHome: androidx.swiperefreshlayout.widget.SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        recyclerTrainerTop = view.findViewById(R.id.recyclerTrainerTop)
        recyclerTrainerAll = view.findViewById(R.id.recyclerTrainerAll)
        recyclerLeaderboard = view.findViewById(R.id.recyclerLeaderboard)
        swipeRefreshHome = view.findViewById(R.id.swipeRefreshHome)
        
        setupTrainerRecyclers()
        setupHomeMenu(view)
        setupObservers()

        // Xử lý nút Liên hệ nổi
        val fabContact = view.findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.fabContact)
        fabContact.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.fitbody.ui.ContactActivity::class.java))
        }

        // Hiệu ứng thu gọn/mở rộng nút khi cuộn trang
        val nestedScroll = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.nestedScrollHome)
        nestedScroll.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && fabContact.isExtended) {
                fabContact.shrink() // Thu nhỏ khi cuộn xuống
            } else if (scrollY < oldScrollY && !fabContact.isExtended) {
                fabContact.extend() // Mở rộng khi cuộn lên
            }
            if (scrollY == 0) fabContact.extend() // Luôn mở rộng khi ở trên cùng
        }

        swipeRefreshHome.setOnRefreshListener {
            val userId = SessionManager(requireContext()).getUserId()
            viewModel.loadData(userId)
        }

        val userId = SessionManager(requireContext()).getUserId()
        viewModel.loadData(userId)
        
        return view
    }

    private fun setupObservers() {
        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            swipeRefreshHome.isRefreshing = isRefreshing
        }

        viewModel.topTrainers.observe(viewLifecycleOwner) { list ->
            trainerListTop.clear()
            trainerListTop.addAll(list)
            trainerAdapterTop.notifyDataSetChanged()
        }

        viewModel.allTrainers.observe(viewLifecycleOwner) { list ->
            trainerListAll.clear()
            trainerListAll.addAll(list)
            trainerAdapterAll.notifyDataSetChanged()
        }

        viewModel.leaderboard.observe(viewLifecycleOwner) { list ->
            recyclerLeaderboard.adapter = LeaderboardAdapter(list)
        }
    }

    private fun setupTrainerRecyclers() {
        trainerAdapterTop = createTrainerAdapter(trainerListTop)
        recyclerTrainerTop.layoutManager = LinearLayoutManager(requireContext())
        recyclerTrainerTop.adapter = trainerAdapterTop

        trainerAdapterAll = createTrainerAdapter(trainerListAll)
        recyclerTrainerAll.layoutManager = LinearLayoutManager(requireContext())
        recyclerTrainerAll.adapter = trainerAdapterAll

        recyclerLeaderboard.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun createTrainerAdapter(list: ArrayList<Trainer>): TrainerAdapter {
        return TrainerAdapter(list, { t ->
            val intent = Intent(requireContext(), TrainerDetailActivity::class.java)
            intent.putExtra("trainer_id", t.id)
            intent.putExtra("trainer_name", t.name)
            intent.putExtra("trainer_image", t.image)
            startActivity(intent)
        }, { t -> addFavorite(t.id) }, { t -> toggleLike(t) })
    }

    private fun setupHomeMenu(view: View) {
        view.findViewById<LinearLayout>(R.id.btnBMIHome).setOnClickListener { startActivity(Intent(requireContext(), BMICalculatorActivity::class.java)) }
        view.findViewById<LinearLayout>(R.id.btnStatsHome).setOnClickListener { startActivity(Intent(requireContext(), WorkoutStatsActivity::class.java)) }
        view.findViewById<LinearLayout>(R.id.btnProgressHome).setOnClickListener { startActivity(Intent(requireContext(), com.example.fitbody.ui.ProgressActivity::class.java)) }
        
        view.findViewById<LinearLayout>(R.id.btnCheckInHome).setOnClickListener { startActivity(Intent(requireContext(), CheckInActivity::class.java)) }
        view.findViewById<LinearLayout>(R.id.btnHistoryHome).setOnClickListener { startActivity(Intent(requireContext(), com.example.fitbody.ui.CheckInHistoryActivity::class.java)) }
        view.findViewById<LinearLayout>(R.id.btnScheduleHome).setOnClickListener { startActivity(Intent(requireContext(), com.example.fitbody.ui.ScheduleActivity::class.java)) }

        view.findViewById<LinearLayout>(R.id.btnPremiumHome).setOnClickListener { startActivity(Intent(requireContext(), PremiumPlanActivity::class.java)) }
        view.findViewById<LinearLayout>(R.id.btnShopHome).setOnClickListener { startActivity(Intent(requireContext(), ShopActivity::class.java)) }
    }

    private fun addFavorite(trainerId: Int) {
        val userId = SessionManager(requireContext()).getUserId()
        if (userId == 0) return
        if (DatabaseHelper(requireContext()).addFavorite(userId, trainerId)) {
            Toast.makeText(requireContext(), "Đã thêm vào yêu thích ❤️", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleLike(trainer: Trainer) {
        val userId = SessionManager(requireContext()).getUserId()
        if (userId == 0) return
        val dbHelper = DatabaseHelper(requireContext())
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val success = if (trainer.isLikedByMe) dbHelper.removeLike(userId, trainer.id) else dbHelper.addLike(userId, trainer.id)
            if (success) {
                withContext(Dispatchers.Main) { 
                    viewModel.loadData(userId) 
                }
            }
        }
    }
}
