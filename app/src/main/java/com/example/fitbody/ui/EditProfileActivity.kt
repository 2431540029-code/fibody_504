package com.example.fitbody.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var imgAvatarEdit: CircleImageView
    private lateinit var btnChangeAvatar: ImageButton

    private var avatarPath: String? = null

    // Launcher chọn ảnh từ thư viện
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imgAvatarEdit.setImageURI(it)
            saveImageToInternalStorage(it)
        }
    }

    // Launcher chụp ảnh mới
    private val takePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                imgAvatarEdit.setImageBitmap(it)
                saveBitmapToInternalStorage(it)
            }
        }
    }

    // Launcher xin quyền Camera
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền Camera để chụp ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        loadProfile()

        btnBack.setOnClickListener { finish() }
        btnSaveProfile.setOnClickListener { saveProfile() }
        btnChangeAvatar.setOnClickListener { showImageSourceDialog() }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        edtPhone = findViewById(R.id.edtPhone)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        imgAvatarEdit = findViewById(R.id.imgAvatarEdit)
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar)
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Chọn từ thư viện", "Chụp ảnh mới")
        AlertDialog.Builder(this)
            .setTitle("Thay đổi ảnh đại diện")
            .setItems(options) { _, which ->
                if (which == 0) {
                    pickImage.launch("image/*")
                } else {
                    checkCameraPermissionAndOpen()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhoto.launch(intent)
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            avatarPath = file.absolutePath
        } catch (e: Exception) {}
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap) {
        try {
            val file = File(filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            avatarPath = file.absolutePath
        } catch (e: Exception) {}
    }

    private fun loadProfile() {
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        val cursor = dbHelper.getUserProfile(userId)

        if (cursor.moveToFirst()) {
            val name = cursor.getString(0) ?: ""
            val email = cursor.getString(1) ?: ""
            val avatar = cursor.getString(2)
            val phone = cursor.getString(3) ?: ""

            edtName.setText(name)
            edtEmail.setText(email)
            edtPhone.setText(phone)

            if (!avatar.isNullOrEmpty()) {
                avatarPath = avatar
                Glide.with(this).load(File(avatar)).into(imgAvatarEdit)
            }
        }
        cursor.close()
    }

    private fun saveProfile() {
        val name = edtName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val phone = edtPhone.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên và email", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        
        if (dbHelper.updateUserProfile(userId, name, email, avatarPath, phone)) {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}
