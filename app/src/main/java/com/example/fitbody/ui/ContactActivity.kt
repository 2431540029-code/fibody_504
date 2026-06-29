package com.example.fitbody.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
        val session = SessionManager(this)
        val targetMode = if (session.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        val spinner = findViewById<Spinner>(R.id.spinnerLocation)
        val locations = arrayOf("Chọn Địa Điểm Tập Luyện", "Chi nhánh Tân Phú", "Chi nhánh Quận 10", "Chi nhánh Gò Vấp")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        // 1. Mở Google Maps (Sửa lỗi không ra khoảng cách)
        findViewById<View>(R.id.itemAddress).setOnClickListener {
            val address = "S'Life Gym Tân Thắng" // Sử dụng tên gợi nhớ để Maps nhận diện điểm POI tốt hơn
            val gmmIntentUri = Uri.parse("geo:10.803730,106.613944?q=" + Uri.encode(address))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                // Nếu không có app Google Maps, mở trình duyệt
                val webUrl = "https://www.google.com/maps/dir/?api=1&destination=10.803730,106.613944"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
            }
        }

        // 2. Gửi Email (Bọc try-catch tránh crash)
        findViewById<View>(R.id.itemEmail).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:slifegym@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "YÊU CẦU TƯ VẤN - FITBODY")
                }
                startActivity(Intent.createChooser(intent, "Chọn ứng dụng Email"))
            } catch (e: Exception) {
                Toast.makeText(this, "Không tìm thấy ứng dụng Email trên máy bạn", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Gọi điện Hotline
        findViewById<View>(R.id.itemHotline).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0902635124"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Nhắn tin SMS (Sửa lỗi out/crash)
        findViewById<View>(R.id.itemSms).setOnClickListener {
            try {
                val uri = Uri.parse("smsto:0902635124")
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                intent.putExtra("sms_body", "Tôi cần tư vấn về khóa học...")
                startActivity(intent)
            } catch (e: Exception) {
                // Cách dự phòng cho một số dòng máy
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("sms:0902635124")
                    startActivity(intent)
                } catch (e2: Exception) {
                    Toast.makeText(this, "Không tìm thấy ứng dụng nhắn tin", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 5. Mở Facebook
        findViewById<View>(R.id.cardFacebook).setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/slifegym")))
            } catch (e: Exception) {}
        }
        
        val fbCoverUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=500"
        com.bumptech.glide.Glide.with(this)
            .load(fbCoverUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_facebook)
            .into(findViewById<ImageView>(R.id.imgFacebook))

        findViewById<Button>(R.id.btnSubmitRequest).setOnClickListener {
            val name = findViewById<EditText>(R.id.edtContactName).text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gửi thành công! Chúng tôi sẽ liên hệ sớm.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
