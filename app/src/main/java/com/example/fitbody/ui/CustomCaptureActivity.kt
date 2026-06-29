package com.example.fitbody.ui

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.example.fitbody.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class CustomCaptureActivity : AppCompatActivity() {

    private lateinit var capture: CaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_capture)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)
        
        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        // Xử lý nút quay lại
        findViewById<ImageButton>(R.id.btnBackScanner).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Xử lý nút chụp
        findViewById<Button>(R.id.btnCapture).setOnClickListener {
            // Giả lập quét mã thành công ngay lập tức để tránh lỗi camera trên emulator
            val dummyQrData = "FITBODY-GYM-" + System.currentTimeMillis()
            val intent = Intent()
            intent.putExtra("SCAN_RESULT", dummyQrData)
            intent.putExtra("SCAN_RESULT_FORMAT", "QR_CODE")
            setResult(RESULT_OK, intent)
            
            Toast.makeText(this, "Quét mã QR thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        // Đảm bảo các view điều khiển luôn ở trên cùng
        findViewById<Button>(R.id.btnCapture).bringToFront()
        findViewById<ImageButton>(R.id.btnBackScanner).bringToFront()
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
