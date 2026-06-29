package com.example.fitbody.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class CheckInActivity : AppCompatActivity() {

    private lateinit var btnCheckIn: Button
    private lateinit var txtLastCheckIn: TextView
    private lateinit var btnBack: TextView

    private val userId = 1

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openQRScanner()
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền camera để quét QR", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkin)

        btnCheckIn = findViewById(R.id.btnCheckIn)
        txtLastCheckIn = findViewById(R.id.txtLastCheckIn)
        btnBack = findViewById(R.id.btnBack)

        txtLastCheckIn.text = "Sẵn sàng quét mã QR để check-in"

        btnCheckIn.setOnClickListener {
            checkCameraPermission()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openQRScanner()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setCaptureActivity(CustomCaptureActivity::class.java)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Quét mã QR phòng gym")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Đã hủy quét QR", Toast.LENGTH_SHORT).show()
            } else {
                sendCheckInToServer(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun sendCheckInToServer(qrCode: String) {
        val session = SessionManager(this)
        val currentUserId = session.getUserId()
        
        txtLastCheckIn.text = "Đang xử lý check-in..."
        
        val dbHelper = DatabaseHelper(this)
        val resultId = dbHelper.addCheckIn(currentUserId, qrCode)

        if (resultId != -1L) {
            Toast.makeText(this, "Check-in thành công!", Toast.LENGTH_SHORT).show()
            txtLastCheckIn.text = "Check-in thành công bằng QR:\n$qrCode"
            
            // Chuyển hướng sang trang lịch sử ngay lập tức
            val intent = Intent(this, CheckInHistoryActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Check-in thất bại", Toast.LENGTH_SHORT).show()
            txtLastCheckIn.text = "Lỗi khi lưu dữ liệu"
        }
    }
}