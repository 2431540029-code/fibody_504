package com.example.fitbody.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var txtLogin: TextView
    private lateinit var btnGoogle: ImageButton
    private lateinit var btnFacebook: ImageButton

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        edtUsername = findViewById(R.id.edtRegUsername)
        edtEmail = findViewById(R.id.edtRegEmail)
        edtPassword = findViewById(R.id.edtRegPassword)
        edtConfirmPassword = findViewById(R.id.edtRegConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        txtLogin = findViewById(R.id.txtLogin)
        btnGoogle = findViewById(R.id.btnRegGoogle)
        btnFacebook = findViewById(R.id.btnRegFacebook)

        // Cấu hình Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Cấu hình Facebook Login
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleSocialLogin(result.accessToken.userId, "Facebook", "User Facebook", "")
            }

            override fun onCancel() {
                Toast.makeText(this@RegisterActivity, "Hủy đăng ký Facebook", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@RegisterActivity, "Lỗi Facebook: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        btnRegister.setOnClickListener {
            register()
        }

        txtLogin.setOnClickListener {
            finish()
        }

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 100)
        }

        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    handleSocialLogin(account.id ?: "", "Google", account.displayName ?: "User", account.email ?: "")
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Lỗi Google Sign In: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSocialLogin(socialId: String, provider: String, name: String, email: String) {
        val dbHelper = DatabaseHelper(this)
        var userId = dbHelper.getUserBySocialId(socialId, provider)

        if (userId == -1) {
            userId = dbHelper.registerSocialUser(name, email, socialId, provider).toInt()
        }

        if (userId != -1) {
            val session = SessionManager(this)
            session.saveLogin(name, "user", userId)
            
            // Chuyển đến màn hình kiểm tra onboarding thay vì vào thẳng MainActivity
            val sharedPreferences = getSharedPreferences("onboarding_data", MODE_PRIVATE)
            val isCompleted = sharedPreferences.getBoolean("is_onboarding_completed_$userId", false)
            
            val intent = if (isCompleted) {
                Intent(this, com.example.fitbody.MainActivity::class.java)
            } else {
                Intent(this, com.example.fitbody.ui.OnboardingActivity::class.java)
            }
            
            intent.putExtra("user_id", userId)
            intent.putExtra("username", name)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            Toast.makeText(this, "Đăng ký $provider thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun register() {
        val username = edtUsername.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()
        val confirmPassword = edtConfirmPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DatabaseHelper(this)
        val result = dbHelper.registerUser(username, email, password)

        if (result != -1L) {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Tên đăng nhập đã tồn tại hoặc lỗi", Toast.LENGTH_SHORT).show()
        }
    }
}