package com.example.fitbody.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import kotlin.math.abs

class ProgressFragment : Fragment() {

    private lateinit var btnBack: TextView
    private lateinit var txtCurrentWeight: TextView
    private lateinit var txtBMI: TextView
    private lateinit var txtGoalWeight: TextView
    private lateinit var txtGoalProgress: TextView
    private lateinit var txtAdvice: TextView
    private lateinit var progressGoal: ProgressBar

    private var userId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_progress,
            container,
            false
        )

        btnBack = view.findViewById(R.id.btnBack)
        txtCurrentWeight = view.findViewById(R.id.txtCurrentWeight)
        txtBMI = view.findViewById(R.id.txtBMI)
        txtGoalWeight = view.findViewById(R.id.txtGoalWeight)
        txtGoalProgress = view.findViewById(R.id.txtGoalProgress)
        txtAdvice = view.findViewById(R.id.txtAdvice)
        progressGoal = view.findViewById(R.id.progressGoal)

        val session = SessionManager(requireContext())
        userId = session.getUserId()

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        loadBodyProgress()

        return view
    }

    private fun loadBodyProgress() {
        val dbHelper = DatabaseHelper(requireContext())
        val cursor = dbHelper.getLatestProgress(userId)

        var heightCm = 0.0
        var weightKg = 0.0

        if (cursor.moveToFirst()) {
            heightCm = cursor.getDouble(cursor.getColumnIndexOrThrow("height"))
            weightKg = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"))
        }
        cursor.close()

        if (heightCm <= 0 || weightKg <= 0) {
            // Fallback to SharedPreferences if SQLite is empty
            val sharedPreferences = requireContext().getSharedPreferences("onboarding_data", Context.MODE_PRIVATE)
            heightCm = sharedPreferences.getString("height_$userId", "")?.toDoubleOrNull() ?: 0.0
            weightKg = sharedPreferences.getString("weight_$userId", "")?.toDoubleOrNull() ?: 0.0
        }

        if (heightCm <= 0 || weightKg <= 0) {
            txtCurrentWeight.text = "Chưa có"
            txtBMI.text = "Chưa có"
            txtGoalWeight.text = "Chưa có"
            txtGoalProgress.text = "0% đạt mục tiêu"
            txtAdvice.text = "Bạn cần cập nhật chiều cao và cân nặng để xem tiến độ cơ thể."
            progressGoal.progress = 0
            return
        }

        val heightM = heightCm / 100
        val bmi = weightKg / (heightM * heightM)
        val goalWeightKg = getDefaultGoalWeight(weightKg)

        val progressPercent = calculateGoalProgress(
            currentWeight = weightKg,
            goalWeight = goalWeightKg
        )

        txtCurrentWeight.text = "${formatNumber(weightKg)} kg"
        txtBMI.text = String.format("%.2f", bmi)
        txtGoalWeight.text = "${formatNumber(goalWeightKg)} kg"
        txtGoalProgress.text = "$progressPercent% đạt mục tiêu"
        progressGoal.progress = progressPercent

        txtAdvice.text = getHealthAdvice(bmi, weightKg, goalWeightKg)
    }

    private fun getDefaultGoalWeight(currentWeight: Double): Double {
        return if (currentWeight > 65) {
            currentWeight - 5
        } else {
            currentWeight + 3
        }
    }

    private fun calculateGoalProgress(
        currentWeight: Double,
        goalWeight: Double
    ): Int {
        val startWeight = if (currentWeight > goalWeight) {
            goalWeight + 5
        } else {
            goalWeight - 3
        }

        val totalDistance = abs(startWeight - goalWeight)
        val currentDistance = abs(currentWeight - goalWeight)

        if (totalDistance == 0.0) return 100

        val progress = ((totalDistance - currentDistance) / totalDistance) * 100

        return progress.toInt().coerceIn(0, 100)
    }

    private fun getHealthAdvice(
        bmi: Double,
        currentWeight: Double,
        goalWeight: Double
    ): String {
        val goalText = if (currentWeight > goalWeight) {
            "giảm cân"
        } else if (currentWeight < goalWeight) {
            "tăng cân"
        } else {
            "duy trì cân nặng"
        }

        return when {
            bmi < 18.5 ->
                "BMI của bạn đang thấp. Bạn nên ăn đủ chất, tăng protein và tập luyện đều để $goalText."

            bmi < 25 ->
                "BMI của bạn đang bình thường. Hãy duy trì thói quen tập luyện và ăn uống hợp lý để $goalText."

            bmi < 30 ->
                "BMI của bạn đang hơi cao. Bạn nên kiểm soát calo, tập cardio và tập sức mạnh đều đặn để $goalText."

            else ->
                "BMI của bạn đang cao. Bạn nên giảm đồ ngọt, đồ chiên dầu và tập luyện đều hơn để cải thiện sức khỏe."
        }
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}