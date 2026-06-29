package com.example.fitbody.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.MainActivity
import com.example.fitbody.PtMainActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.ui.OnboardingActivity
import com.example.fitbody.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

class LoginActivity : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtRegister: TextView
    private lateinit var btnGoogle: ImageButton
    private lateinit var btnFacebook: ImageButton

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val session = SessionManager(this)
        

        try {
            val isDark = session.isDarkMode()
            val targetMode = if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
                AppCompatDelegate.setDefaultNightMode(targetMode)
            }
        } catch (e: Exception) {}

        super.onCreate(savedInstanceState)

        // 2. Kiểm tra đăng nhập và chuyển hướng ngay lập tức nếu đã login
        if (session.isLoggedIn() && !session.isSessionExpired()) {
            val role = session.getRole()
            if (role == "pt") {
                openPt(session.getUserId(), session.getUsername())
            } else {
                openMainOrOnboarding(session.getUserId(), session.getUsername())
            }
            return // Dừng lại ở đây, không load layout login nữa
        }

        // 3. Nếu chưa đăng nhập, hiển thị giao diện
        setContentView(R.layout.activity_login)

        initViews()
        setupSocialLogin()
        
        btnLogin.setOnClickListener { login() }
        txtRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun initViews() {
        edtUsername = findViewById(R.id.edtUsername)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtRegister = findViewById(R.id.txtRegister)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook)
    }

    private fun setupSocialLogin() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)

            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleSocialLogin(result.accessToken.userId, "Facebook", "User Facebook", "")
                }
                override fun onCancel() {}
                override fun onError(error: FacebookException) {
                    Toast.makeText(this@LoginActivity, "Lỗi Facebook: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {}
    }

    private fun login() {
        val username = edtUsername.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DatabaseHelper(this)
        val userId = dbHelper.checkUser(username, password)

        if (userId != -1) {
            val role = dbHelper.getUserRole(username)
            val session = SessionManager(this)
            session.saveLogin(username, role, userId)
            
            if (role == "pt") {
                openPt(userId, username)
            } else {
                openMainOrOnboarding(userId, username)
            }
        } else {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSocialLogin(socialId: String, provider: String, name: String, email: String) {
        val dbHelper = DatabaseHelper(this)
        var userId = dbHelper.getUserBySocialId(socialId, provider)
        if (userId == -1) userId = dbHelper.registerSocialUser(name, email, socialId, provider).toInt()

        if (userId != -1) {
            val session = SessionManager(this)
            session.saveLogin(name, "user", userId)
            openMainOrOnboarding(userId, name)
        }
    }

    private fun openMainOrOnboarding(userId: Int, username: String) {
        val sp = getSharedPreferences("onboarding_data", Context.MODE_PRIVATE)
        val isCompleted = sp.getBoolean("is_onboarding_completed_$userId", false)
        val intent = if (isCompleted) Intent(this, MainActivity::class.java) else Intent(this, OnboardingActivity::class.java)
        
        intent.putExtra("user_id", userId)
        intent.putExtra("username", username)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun openPt(userId: Int, username: String) {
        val intent = Intent(this, PtMainActivity::class.java)
        intent.putExtra("user_id", userId)
        intent.putExtra("username", username)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (::callbackManager.isInitialized) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) handleSocialLogin(account.id ?: "", "Google", account.displayName ?: "User", account.email ?: "")
            } catch (e: Exception) {}
        }
    }
}
