package com.example.fitbody.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.ui.ChangePasswordActivity
import com.example.fitbody.ui.EditProfileActivity
import com.example.fitbody.ui.auth.LoginActivity
import com.example.fitbody.utils.SessionManager
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var btnLogout: Button
    private lateinit var txtProfileName: TextView
    private lateinit var txtProfileRole: TextView
    private lateinit var imgAvatarProfile: CircleImageView
    
    private lateinit var txtProfileBMI: TextView
    private lateinit var txtProfileGoal: TextView
    
    private lateinit var layoutDarkMode: View
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var txtChangePassword: TextView
    private lateinit var txtContactSupport: TextView
    private lateinit var txtClearCache: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadProfileInfo()
        setupListeners(view)
    }

    private fun initViews(view: View) {
        btnLogout = view.findViewById(R.id.btnLogout)
        txtProfileName = view.findViewById(R.id.txtProfileName)
        txtProfileRole = view.findViewById(R.id.txtProfileRole)
        imgAvatarProfile = view.findViewById(R.id.imgAvatarProfile)
        
        txtProfileBMI = view.findViewById(R.id.txtProfileBMI)
        txtProfileGoal = view.findViewById(R.id.txtProfileGoal)
        
        layoutDarkMode = view.findViewById(R.id.layoutDarkMode)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        txtChangePassword = view.findViewById(R.id.txtChangePassword)
        txtContactSupport = view.findViewById(R.id.txtContactSupport)
        
        // Thêm TextView xóa cache vào Layout bằng code (không cần sửa XML)
        txtClearCache = TextView(requireContext()).apply {
            text = "🗑 Xóa bộ nhớ đệm (Reset dữ liệu)"
            setTextColor(android.graphics.Color.parseColor("#888888"))
            textSize = 14f
            setPadding(16, 40, 16, 40)
            gravity = android.view.Gravity.CENTER
        }
        (view.findViewById<View>(R.id.btnLogout).parent as android.widget.LinearLayout).addView(txtClearCache)

        val session = SessionManager(requireContext())
        switchNotifications.isChecked = session.isRemindersEnabled()
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.txtEditProfile)?.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        layoutDarkMode.setOnClickListener { toggleDarkMode() }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            SessionManager(requireContext()).setRemindersEnabled(isChecked)
            val msg = if (isChecked) "Đã bật nhắc nhở hàng ngày! 🔔" else "Đã tắt nhắc nhở."
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        txtChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        txtContactSupport.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.fitbody.ui.ContactActivity::class.java))
        }
        
        txtClearCache.setOnClickListener {
            showClearCacheDialog()
        }

        btnLogout.setOnClickListener { logout() }
    }

    private fun showClearCacheDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Làm sạch ứng dụng")
            .setMessage("Mọi dữ liệu lịch sử tập, đơn hàng và giỏ hàng sẽ bị xóa. Bạn có chắc chắn?")
            .setPositiveButton("Xóa hết") { _, _ ->
                DatabaseHelper(requireContext()).clearAllData()
                Toast.makeText(requireContext(), "Đã làm sạch ứng dụng!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun toggleDarkMode() {
        val session = SessionManager(requireContext())
        val newMode = !session.isDarkMode()
        session.setDarkMode(newMode)
        AppCompatDelegate.setDefaultNightMode(if (newMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onResume() {
        super.onResume()
        loadProfileInfo()
    }

    private fun loadProfileInfo() {
        val session = SessionManager(requireContext())
        val userId = session.getUserId()
        val dbHelper = DatabaseHelper(requireContext())

        // 1. Thông tin cơ bản
        val cursor = dbHelper.getUserProfile(userId)
        if (cursor.moveToFirst()) {
            txtProfileName.text = cursor.getString(0)
            txtProfileRole.text = cursor.getString(1)
            val avatarPath = cursor.getString(2)
            if (!avatarPath.isNullOrEmpty()) Glide.with(this).load(File(avatarPath)).into(imgAvatarProfile)
        }
        cursor.close()

        // 2. Chỉ số Fitness từ Onboarding (SharedPreferences)
        val sp = requireActivity().getSharedPreferences("onboarding_data", Context.MODE_PRIVATE)
        val weight = sp.getString("weight_$userId", "0")?.toDoubleOrNull() ?: 0.0
        val height = sp.getString("height_$userId", "0")?.toDoubleOrNull() ?: 1.0
        val goal = sp.getString("goal_$userId", "Chưa thiết lập")
        
        if (weight > 0 && height > 0) {
            val hM = height / 100
            val bmi = weight / (hM * hM)
            txtProfileBMI.text = String.format("%.1f", bmi)
        }
        txtProfileGoal.text = goal
    }

    private fun logout() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Đăng xuất")
            .setMessage("Hẹn gặp lại bạn vào buổi tập tới!")
            .setPositiveButton("Đăng xuất") { _, _ ->
                SessionManager(requireContext()).logout()
                startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .setNegativeButton("Ở lại", null)
            .show()
    }
}
