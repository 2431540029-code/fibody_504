package com.example.fitbody.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var edtOldPassword: EditText
    private lateinit var edtNewPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnChangePassword: Button

    private val userId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        btnBack = findViewById(R.id.btnBack)
        edtOldPassword = findViewById(R.id.edtOldPassword)
        edtNewPassword = findViewById(R.id.edtNewPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        btnBack.setOnClickListener {
            finish()
        }

        btnChangePassword.setOnClickListener {
            validateAndChangePassword()
        }
    }

    private fun validateAndChangePassword() {
        val oldPassword = edtOldPassword.text.toString().trim()
        val newPassword = edtNewPassword.text.toString().trim()
        val confirmPassword = edtConfirmPassword.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Mật khẩu mới phải từ 6 ký tự", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        changePasswordApi(oldPassword, newPassword)
    }

    private fun changePasswordApi(oldPassword: String, newPassword: String) {
        val session = SessionManager(this)
        val currentUserId = session.getUserId()
        val dbHelper = DatabaseHelper(this)
        
        // Simple logic for SQLite: check old password and update to new one
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id FROM tbl_users WHERE id = ? AND password = ?", 
            arrayOf(currentUserId.toString(), oldPassword))
        
        if (cursor.moveToFirst()) {
            cursor.close()
            val updateValues = android.content.ContentValues().apply { put("password", newPassword) }
            val success = dbHelper.writableDatabase.update("tbl_users", updateValues, "id = ?", arrayOf(currentUserId.toString())) > 0
            if (success) {
                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            cursor.close()
            Toast.makeText(this, "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show()
        }
    }
}