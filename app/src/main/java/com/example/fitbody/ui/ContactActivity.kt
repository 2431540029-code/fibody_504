package com.example.fitbody.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitbody.R
import com.example.fitbody.utils.SessionManager

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Áp dụng chế độ màu trước khi tạo giao diện
        val session = SessionManager(this)
        val targetMode = if (session.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        // Cài đặt Spinner địa điểm
        val spinner = findViewById<Spinner>(R.id.spinnerLocation)
        val locations = arrayOf("Chọn Địa Điểm Tập Luyện", "Chi nhánh Tân Phú", "Chi nhánh Quận 10", "Chi nhánh Gò Vấp")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        // 1. Mở Google Maps (Tự động tính khoảng cách khi mở)
        findViewById<TextView>(R.id.txtAddress).setOnClickListener {
            val address = "121A Tân Thắng, Sơn Kỳ, Tân Phú, Thành phố Hồ Chí Minh"
            val gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(address))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Mở trình duyệt nếu không có app GG Maps
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address)))
                startActivity(webIntent)
            }
        }

        // 2. Gửi Email (Hỗ trợ đầy đủ đính kèm/soạn thảo)
        findViewById<TextView>(R.id.txtEmail).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:slifegym@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Yêu cầu tư vấn FitBody")
            }
            startActivity(Intent.createChooser(intent, "Gửi email bằng..."))
        }

        // 3. Gọi điện Hotline (Tự động điền số)
        findViewById<TextView>(R.id.txtHotline).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0902635124"))
            startActivity(intent)
        }

        // 4. Nhắn tin SMS
        findViewById<TextView>(R.id.txtSms).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:0902635124"))
            startActivity(intent)
        }

        // Tải hình ảnh Fanpage mang phong cách Facebook chuyên nghiệp
        val fbCoverUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=500" // Ảnh Gym rực rỡ, chuyên nghiệp
        com.bumptech.glide.Glide.with(this)
            .load(fbCoverUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_facebook)
            .into(findViewById<ImageView>(R.id.imgFacebook))

        // Mở Facebook (Nhấn vào ảnh hoặc tên đều được)
        val openFb = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/slifegym"))
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.imgFacebook).setOnClickListener { openFb() }
        findViewById<TextView>(R.id.txtFbName).setOnClickListener { openFb() }

        // Xử lý nút gửi yêu cầu
        findViewById<Button>(R.id.btnSubmitRequest).setOnClickListener {
            val name = findViewById<EditText>(R.id.edtContactName).text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên để nhận ưu đãi!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gửi yêu cầu thành công! Chúng tôi sẽ gọi lại sớm.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
