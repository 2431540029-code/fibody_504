package com.example.fitbody.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.utils.SessionManager
import java.util.Locale

class BMICalculatorActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var edtHeight: EditText
    private lateinit var edtWeight: EditText
    private lateinit var btnCalculateBMI: Button
    private lateinit var txtBMIResult: TextView
    private lateinit var txtBMIStatus: TextView
    private lateinit var layoutNutrition: LinearLayout
    private lateinit var txtNutritionAdvice: TextView
    private lateinit var txtWaterIntake: TextView
    private lateinit var txtIdealWeight: TextView

    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_calculator)

        initViews()

        val session = SessionManager(this)
        userId = session.getUserId()

        btnBack.setOnClickListener { finish() }
        btnCalculateBMI.setOnClickListener { calculateBMI(saveData = true) }

        loadSavedDataAndCalculate()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        edtHeight = findViewById(R.id.edtHeight)
        edtWeight = findViewById(R.id.edtWeight)
        btnCalculateBMI = findViewById(R.id.btnCalculateBMI)
        txtBMIResult = findViewById(R.id.txtBMIResult)
        txtBMIStatus = findViewById(R.id.txtBMIStatus)
        layoutNutrition = findViewById(R.id.layoutNutrition)
        txtNutritionAdvice = findViewById(R.id.txtNutritionAdvice)
        txtWaterIntake = findViewById(R.id.txtWaterIntake)
        txtIdealWeight = findViewById(R.id.txtIdealWeight)

        txtTitle.text = "Phân tích chỉ số BMI"
    }

    private fun loadSavedDataAndCalculate() {
        val sp = getSharedPreferences("onboarding_data", Context.MODE_PRIVATE)
        val savedWeight = sp.getString("weight_$userId", "")
        val savedHeight = sp.getString("height_$userId", "")

        if (!savedWeight.isNullOrEmpty()) edtWeight.setText(savedWeight)
        if (!savedHeight.isNullOrEmpty()) edtHeight.setText(savedHeight)

        if (!savedWeight.isNullOrEmpty() && !savedHeight.isNullOrEmpty()) {
            calculateBMI(saveData = false)
        }
    }

    private fun calculateBMI(saveData: Boolean) {
        val hStr = edtHeight.text.toString().trim()
        val wStr = edtWeight.text.toString().trim()

        if (hStr.isEmpty() || wStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val hCm = hStr.toDouble()
            val wKg = wStr.toDouble()
            val hM = hCm / 100
            val bmi = wKg / (hM * hM)

            txtBMIResult.text = "BMI của bạn: ${String.format(Locale.getDefault(), "%.2f", bmi)}"
            txtBMIStatus.text = getBMIStatus(bmi)
            
            // Tính toán nâng cao
            calculateWaterIntake(wKg)
            calculateIdealWeight(hCm)
            
            layoutNutrition.visibility = android.view.View.VISIBLE
            txtNutritionAdvice.text = getNutritionAdvice(bmi)

            if (saveData) {
                getSharedPreferences("onboarding_data", Context.MODE_PRIVATE).edit()
                    .putString("height_$userId", hStr)
                    .putString("weight_$userId", wStr)
                    .apply()
            }
        } catch (e: Exception) {}
    }

    private fun calculateWaterIntake(weight: Double) {
        val water = weight * 0.033 // Công thức: 33ml nước cho mỗi kg cân nặng
        txtWaterIntake.text = String.format(Locale.getDefault(), "💧 Lượng nước cần uống: %.1f Lít/ngày", water)
    }

    private fun calculateIdealWeight(heightCm: Double) {
        val hM = heightCm / 100
        val minIdeal = 18.5 * (hM * hM)
        val maxIdeal = 24.9 * (hM * hM)
        txtIdealWeight.text = String.format(Locale.getDefault(), "⚖️ Cân nặng lý tưởng: %.1f - %.1f kg", minIdeal, maxIdeal)
    }

    private fun getBMIStatus(bmi: Double) = when {
        bmi < 18.5 -> "Trạng thái: Gầy"
        bmi < 25 -> "Trạng thái: Bình thường"
        bmi < 30 -> "Trạng thái: Thừa cân"
        else -> "Trạng thái: Béo phì"
    }

    private fun getNutritionAdvice(bmi: Double) = when {
        bmi < 18.5 -> {
            "• Ăn uống: Tăng thêm 500 calo/ngày, ưu tiên thực phẩm giàu đạm.\n" +
            "• Thực đơn: Trứng, Sữa, Các loại hạt, Thịt đỏ.\n" +
            "• Chế độ: Ăn 5-6 bữa nhỏ, không nên bỏ bữa sáng."
        }
        bmi < 25 -> {
            "• Ăn uống: Duy trì cân bằng đạm, tinh bột và chất xơ.\n" +
            "• Thực đơn: Cá, Ức gà, Rau xanh đậm, Trái cây tươi.\n" +
            "• Chế độ: Uống nước đều đặn, hạn chế ăn đêm sau 20h."
        }
        bmi < 30 -> {
            "• Ăn uống: Giảm 300 calo/ngày, hạn chế đường và dầu mỡ.\n" +
            "• Thực đơn: Khoai lang, Gạo lứt, Salad, Các loại đậu.\n" +
            "• Chế độ: Nhai kỹ, ăn nhiều rau xanh trước bữa chính."
        }
        else -> {
            "• Ăn uống: Kiểm soát chặt chẽ calo, thâm hụt 500-700 calo/ngày.\n" +
            "• Thực đơn: Bông cải trắng/xanh, Cá hấp, Nước ép rau củ.\n" +
            "• Chế độ: Tuyệt đối không dùng nước ngọt và thức ăn nhanh."
        }
    }
}
