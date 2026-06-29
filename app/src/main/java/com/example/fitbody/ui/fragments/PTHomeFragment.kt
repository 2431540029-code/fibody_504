package com.example.fitbody.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.adapter.WorkoutAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.ui.pt.AddWorkoutActivity
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PTHomeFragment : Fragment() {

    private lateinit var txtPtWelcome: TextView
    private lateinit var txtCurrentDate: TextView
    private lateinit var imgPtAvatar: ImageView
    private lateinit var txtTotalStudents: TextView
    private lateinit var txtTotalWorkouts: TextView
    private lateinit var txtPtIncome: TextView
    private lateinit var rvRecentWorkouts: RecyclerView
    
    // UI Containers
    private lateinit var btnPtAddWorkout: LinearLayout
    private lateinit var btnPtBroadcast: LinearLayout
    private lateinit var btnPtSchedule: LinearLayout
    private lateinit var cardTotalStudents: View
    
    private var currentTrainerId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pt_home, container, false)
        
        initViews(view)
        setupListeners()
        loadDashboardData()
        
        return view
    }

    private fun initViews(view: View) {
        txtPtWelcome = view.findViewById(R.id.txtPtWelcome)
        txtCurrentDate = view.findViewById(R.id.txtCurrentDate)
        imgPtAvatar = view.findViewById(R.id.imgPtAvatar)
        txtTotalStudents = view.findViewById(R.id.txtTotalStudents)
        txtTotalWorkouts = view.findViewById(R.id.txtTotalWorkouts)
        txtPtIncome = view.findViewById(R.id.txtPtIncome)
        rvRecentWorkouts = view.findViewById(R.id.rvRecentWorkouts)
        
        btnPtAddWorkout = view.findViewById(R.id.btnPtAddWorkout)
        btnPtBroadcast = view.findViewById(R.id.btnPtBroadcast)
        btnPtSchedule = view.findViewById(R.id.btnPtSchedule)
        cardTotalStudents = view.findViewById(R.id.cardTotalStudents)

        rvRecentWorkouts.layoutManager = LinearLayoutManager(requireContext())
        
        // Cập nhật ngày hiện tại
        val sdf = SimpleDateFormat("EEEE, dd MMMM", Locale("vi", "VN"))
        txtCurrentDate.text = sdf.format(Date())
    }

    private fun setupListeners() {
        btnPtAddWorkout.setOnClickListener {
            startActivity(Intent(requireContext(), AddWorkoutActivity::class.java))
        }
        
        btnPtBroadcast.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Chức năng thông báo sẽ sớm ra mắt!", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        btnPtSchedule.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Xem lịch dạy chi tiết!", android.widget.Toast.LENGTH_SHORT).show()
        }

        cardTotalStudents.setOnClickListener {
            if (currentTrainerId != 0) {
                val intent = Intent(requireContext(), com.example.fitbody.ui.pt.MyStudentsActivity::class.java)
                intent.putExtra("trainer_id", currentTrainerId)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val session = SessionManager(requireContext())
        val userId = session.getUserId()
        val ptUsername = session.getUsername()
        val dbHelper = DatabaseHelper(requireContext())

        txtPtWelcome.text = "Chào HLV, $ptUsername"

        lifecycleScope.launch(Dispatchers.IO) {
            // Tải thông tin cá nhân
            val cursor = dbHelper.getUserProfile(userId)
            var avatarPath: String? = null
            if (cursor.moveToFirst()) {
                avatarPath = cursor.getString(2)
            }
            cursor.close()

            val realTrainerId = dbHelper.getTrainerIdByUsername(ptUsername)
            currentTrainerId = realTrainerId
            
            withContext(Dispatchers.Main) {
                if (!avatarPath.isNullOrEmpty()) {
                    Glide.with(this@PTHomeFragment).load(File(avatarPath)).into(imgPtAvatar)
                } else {
                    imgPtAvatar.setImageResource(R.drawable.ic_launcher_background)
                }

                if (realTrainerId != 0) {
                    val studentCount = dbHelper.getTrainerStudentCount(realTrainerId)
                    val workouts = dbHelper.getWorkoutsByTrainer(realTrainerId)
                    
                    txtTotalStudents.text = studentCount.toString()
                    txtTotalWorkouts.text = workouts.size.toString()
                    
                    // Thu nhập thật: giả định 500.000đ/học viên theo học
                    val income = studentCount * 500000
                    val formatter = java.text.NumberFormat.getInstance(Locale("vi", "VN"))
                    txtPtIncome.text = "${formatter.format(income)} đ"
                    
                    rvRecentWorkouts.adapter = WorkoutAdapter(workouts.take(5))
                } else {
                    txtTotalStudents.text = "0"
                    txtTotalWorkouts.text = "0"
                    txtPtIncome.text = "0 đ"
                    rvRecentWorkouts.adapter = WorkoutAdapter(emptyList())
                }
            }
        }
    }
}
