package com.example.fitbody.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.ui.auth.LoginActivity
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PTProfileFragment : Fragment() {

    private lateinit var imgPtProfile: ImageView
    private lateinit var txtPtName: TextView
    private lateinit var btnChangeImage: Button
    private lateinit var btnLogout: Button
    private lateinit var dbHelper: DatabaseHelper

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                updateImageInSqlite(imageUri.toString())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pt_profile, container, false)
        
        dbHelper = DatabaseHelper(requireContext())
        imgPtProfile = view.findViewById(R.id.imgPtProfileMain)
        txtPtName = view.findViewById(R.id.txtPtProfileNameMain)
        btnChangeImage = view.findViewById(R.id.btnPtChangeImageMain)
        btnLogout = view.findViewById(R.id.btnPtLogoutMain)
        
        val session = SessionManager(requireContext())
        txtPtName.text = session.getUsername()
        
        loadCurrentProfileImage()
        
        btnChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
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
        
        return view
    }

    private fun loadCurrentProfileImage() {
        val ptId = SessionManager(requireContext()).getUserId()
        lifecycleScope.launch(Dispatchers.IO) {
            val trainers = dbHelper.getAllTrainers(ptId)
            val currentPt = trainers.find { it.id == ptId }
            
            withContext(Dispatchers.Main) {
                if (currentPt != null && currentPt.image.isNotEmpty()) {
                    val imageResId = resources.getIdentifier(currentPt.image, "drawable", requireContext().packageName)
                    if (imageResId != 0) {
                        Glide.with(this@PTProfileFragment)
                            .load(imageResId)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(imgPtProfile)
                    } else {
                        Glide.with(this@PTProfileFragment)
                            .load(currentPt.image)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(imgPtProfile)
                    }
                }
            }
        }
    }

    private fun updateImageInSqlite(imagePath: String) {
        val ptId = SessionManager(requireContext()).getUserId()
        if (dbHelper.updateTrainerImage(ptId, imagePath)) {
            Toast.makeText(requireContext(), "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show()
            Glide.with(this).load(imagePath).into(imgPtProfile)
        }
    }
}
