package com.example.fitbody.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.ui.EditProfileActivity
import com.example.fitbody.ui.ChangePasswordActivity
import com.example.fitbody.ui.auth.LoginActivity
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PTProfileFragment : Fragment() {

    private lateinit var imgPtProfile: ImageView
    private lateinit var txtPtName: TextView
    private lateinit var txtPtSpecialty: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var txtTotalStudents: TextView
    private lateinit var txtTotalLikes: TextView
    private lateinit var txtTotalWorkouts: TextView
    private lateinit var layoutDarkMode: View
    private lateinit var txtChangePassword: View
    private lateinit var txtContactSupport: View
    private lateinit var btnLogout: Button
    
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pt_profile, container, false)
        
        dbHelper = DatabaseHelper(requireContext())
        session = SessionManager(requireContext())
        
        initViews(view)
        setupListeners()
        loadPtData()
        
        return view
    }

    private fun initViews(view: View) {
        imgPtProfile = view.findViewById(R.id.imgPtProfileMain)
        txtPtName = view.findViewById(R.id.txtPtProfileNameMain)
        txtPtSpecialty = view.findViewById(R.id.txtPtSpecialty)
        btnEditProfile = view.findViewById(R.id.btnPtChangeImageMain)
        txtTotalStudents = view.findViewById(R.id.txtPtTotalStudents)
        txtTotalLikes = view.findViewById(R.id.txtPtTotalLikes)
        txtTotalWorkouts = view.findViewById(R.id.txtPtTotalWorkouts)
        layoutDarkMode = view.findViewById(R.id.layoutPtDarkMode)
        txtChangePassword = view.findViewById(R.id.txtPtChangePassword)
        txtContactSupport = view.findViewById(R.id.txtPtContactSupport)
        btnLogout = view.findViewById(R.id.btnPtLogoutMain)
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        layoutDarkMode.setOnClickListener {
            val isDark = session.isDarkMode()
            val newMode = !isDark
            session.setDarkMode(newMode)
            
            // Cập nhật text hiển thị ngay lập tức
            val txtDarkMode = layoutDarkMode.findViewById<TextView>(R.id.txtPtDarkMode)
            txtDarkMode.text = if (newMode) "🌙 Chế độ tối" else "☀️ Chế độ sáng"

            // Áp dụng chế độ màu toàn hệ thống
            AppCompatDelegate.setDefaultNightMode(
                if (newMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            
            // Recreate activity để áp dụng theme mới cho layout hiện tại
            activity?.recreate()
        }

        txtChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        txtContactSupport.setOnClickListener {
            Toast.makeText(requireContext(), "Email hỗ trợ: trainer_support@fitbody.com", Toast.LENGTH_LONG).show()
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đồng ý") { _, _ ->
                    session.logout()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadPtData()
    }

    private fun loadPtData() {
        val userId = session.getUserId()
        val username = session.getUsername()
        
        txtPtName.text = username
        
        // Cập nhật text chế độ tối/sáng dựa trên session hiện tại
        val txtDarkMode = layoutDarkMode.findViewById<TextView>(R.id.txtPtDarkMode)
        txtDarkMode.text = if (session.isDarkMode()) "🌙 Chế độ tối" else "☀️ Chế độ sáng"

        lifecycleScope.launch(Dispatchers.IO) {
            // Lấy thông tin user để có Avatar
            val user = dbHelper.getUserById(userId)
            val trainerId = dbHelper.getTrainerIdByUsername(username)
            
            val studentCount = if (trainerId != 0) dbHelper.getTrainerStudentCount(trainerId) else 0
            val workouts = if (trainerId != 0) dbHelper.getWorkoutsByTrainer(trainerId) else emptyList()
            
            // Lấy thông tin chi tiết trainer để có Specialty và Likes
            val trainers = dbHelper.getAllTrainers(0)
            val trainerDetail = if (trainerId != 0) {
                trainers.find { it.id == trainerId }
            } else null

            withContext(Dispatchers.Main) {
                // Xử lý hiển thị Avatar
                if (user != null && !user.avatar.isNullOrEmpty()) {
                    val avatar = user.avatar
                    val resId = resources.getIdentifier(avatar, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        imgPtProfile.setImageResource(resId)
                    } else {
                        // Nếu là đường dẫn tệp (chụp từ camera/gallery)
                        Glide.with(this@PTProfileFragment)
                            .load(File(avatar))
                            .error(R.drawable.male)
                            .into(imgPtProfile)
                    }
                } else if (trainerDetail != null && trainerDetail.image.isNotEmpty()) {
                    // Fallback sang ảnh trong bảng trainer nếu user avatar trống
                    val resId = resources.getIdentifier(trainerDetail.image, "drawable", requireContext().packageName)
                    if (resId != 0) imgPtProfile.setImageResource(resId)
                    else Glide.with(this@PTProfileFragment).load(trainerDetail.image).error(R.drawable.male).into(imgPtProfile)
                } else {
                    imgPtProfile.setImageResource(R.drawable.male)
                }

                trainerDetail?.let { t ->
                    txtPtSpecialty.text = t.specialty
                    txtTotalLikes.text = t.likeCount.toString()
                }

                txtTotalStudents.text = studentCount.toString()
                txtTotalWorkouts.text = workouts.size.toString()
            }
        }
    }
}
