package com.example.fitbody.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R

class PremiumPlanActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var btnBuyPremium: Button
    private lateinit var txtPremiumStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_plan)

        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        btnBuyPremium = findViewById(R.id.btnBuyPremium)
        txtPremiumStatus = findViewById(R.id.txtPremiumStatus)

        txtTitle.text = "Premium Plan"

        val sharedPreferences =
            getSharedPreferences("premium_data", MODE_PRIVATE)

        val isPremium =
            sharedPreferences.getBoolean("is_premium", false)

        updatePremiumStatus(isPremium)

        btnBack.setOnClickListener {
            finish()
        }

        btnBuyPremium.setOnClickListener {
            sharedPreferences.edit()
                .putBoolean("is_premium", true)
                .apply()

            updatePremiumStatus(true)

            Toast.makeText(
                this,
                "Đăng ký Premium thành công",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updatePremiumStatus(isPremium: Boolean) {
        if (isPremium) {
            txtPremiumStatus.text = "Trạng thái: Đã đăng ký Premium"
            btnBuyPremium.text = "ĐÃ KÍCH HOẠT PREMIUM"
            btnBuyPremium.isEnabled = false
        } else {
            txtPremiumStatus.text = "Trạng thái: Chưa đăng ký Premium"
            btnBuyPremium.text = "ĐĂNG KÝ PREMIUM"
            btnBuyPremium.isEnabled = true
        }
    }
}