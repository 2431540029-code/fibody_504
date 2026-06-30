package com.example.fitbody.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.graphics.drawable.GradientDrawable
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.MainActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var txtSkip: TextView
    private lateinit var progressOnboarding: ProgressBar
    private lateinit var txtQuestion: TextView
    private lateinit var txtSubQuestion: TextView
    private lateinit var layoutOptions: LinearLayout
    private lateinit var btnNext: Button

    private var step = 1

    private var gender = ""
    private val goals = ArrayList<String>()
    private var focusArea = ""
    private var height = ""
    private var weight = ""

    private var weightValue = 65
    private var heightValue = 170

    private var selectedView: TextView? = null
    private var selectedLayout: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        txtSkip = findViewById(R.id.txtSkip)
        progressOnboarding = findViewById(R.id.progressOnboarding)
        txtQuestion = findViewById(R.id.txtQuestion)
        txtSubQuestion = findViewById(R.id.txtSubQuestion)
        layoutOptions = findViewById(R.id.layoutOptions)
        btnNext = findViewById(R.id.btnNext)

        txtSkip.setOnClickListener {
            finishOnboarding()
        }

        btnNext.setOnClickListener {
            handleNext()
        }

        showStep()
    }

    private fun showStep() {
        val root = findViewById<LinearLayout>(R.id.onboardingRoot)
        layoutOptions.removeAllViews()
        layoutOptions.orientation = LinearLayout.VERTICAL
        layoutOptions.gravity = Gravity.CENTER
        layoutOptions.setPadding(0, 0, 0, 0)

        progressOnboarding.progress = step
        btnNext.text = "TIẾP THEO"
        btnNext.visibility = android.view.View.VISIBLE
        txtSkip.visibility = android.view.View.VISIBLE

        selectedView = null
        selectedLayout = null

        // Cập nhật giao diện theo step để sang trọng hơn
        if (step == 6) {
            root?.setBackgroundColor(Color.parseColor("#121212"))
            txtQuestion.setTextColor(Color.WHITE)
            txtSubQuestion.setTextColor(Color.parseColor("#AAAAAA"))
            txtSkip.setTextColor(Color.parseColor("#888888"))
        } else {
            root?.setBackgroundColor(Color.WHITE)
            txtQuestion.setTextColor(Color.BLACK)
            txtSubQuestion.setTextColor(Color.parseColor("#222222"))
            txtSkip.setTextColor(Color.parseColor("#666666"))
        }

        when (step) {
            1 -> showGenderStep()
            2 -> showGoalStep()
            3 -> showFocusStep()
            4 -> showBodyInfoStep()
            5 -> showProcessingStep()
            6 -> showPlanReadyStep()
        }
    }

    private fun handleNext() {
        when (step) {
            1 -> {
                if (gender.isEmpty()) {
                    showMessage("Vui lòng chọn giới tính")
                } else {
                    step++
                    showStep()
                }
            }

            2 -> {
                if (goals.isEmpty()) {
                    showMessage("Vui lòng chọn ít nhất một mục tiêu")
                } else {
                    step++
                    showStep()
                }
            }

            3 -> {
                if (focusArea.isEmpty()) {
                    showMessage("Vui lòng chọn vùng tập")
                } else {
                    step++
                    showStep()
                }
            }

            4 -> {
                weight = weightValue.toString()
                height = heightValue.toString()
                step++
                showStep()
            }

            6 -> {
                finishOnboarding()
            }
        }
    }

    private fun showProcessingStep() {
        txtQuestion.text = "ĐANG TẠO KẾ HOẠCH\nCHO BẠN"
        txtSubQuestion.text = "Đang chuẩn bị kế hoạch dựa trên mục tiêu của bạn..."
        btnNext.visibility = android.view.View.GONE
        txtSkip.visibility = android.view.View.GONE

        val progressContainer = LinearLayout(this)
        progressContainer.orientation = LinearLayout.VERTICAL
        progressContainer.gravity = Gravity.CENTER

        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val percentText = TextView(this)
        percentText.textSize = 32f // Giảm nhẹ kích thước để an toàn hơn
        percentText.setTypeface(null, Typeface.BOLD)
        percentText.setTextColor(if (step == 6) Color.WHITE else Color.BLACK)
        percentText.setPadding(0, 30, 0, 0)
        percentText.text = "0%"

        progressContainer.addView(progressBar)
        progressContainer.addView(percentText)
        layoutOptions.addView(progressContainer)

        var p = 0
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (p <= 100) {
                    percentText.text = "$p%"
                    p += 2
                    handler.postDelayed(this, 30)
                } else {
                    step++
                    showStep()
                }
            }
        }
        handler.post(runnable)
    }

    private fun showGenderStep() {
        txtQuestion.text = "Giới tính của bạn là gì?"
        txtSubQuestion.text = "Hãy cho chúng tôi biết thêm về bạn"

        layoutOptions.orientation = LinearLayout.HORIZONTAL
        layoutOptions.gravity = Gravity.CENTER
        layoutOptions.setPadding(0, 0, 0, 10)

        addGenderOption("Nam", R.drawable.male) {
            gender = "Nam"
        }

        addGenderOption("Nữ", R.drawable.female) {
            gender = "Nữ"
        }
    }

    private fun showGoalStep() {
        txtQuestion.text = "Mục tiêu chính của bạn là gì?"
        txtSubQuestion.text = "Bạn có thể chọn nhiều mục tiêu"

        addMultiOption(
            "🔥 Giảm cân",
            "Đốt mỡ và giảm trọng lượng cơ thể",
            "Giảm cân"
        )

        addMultiOption(
            "💪 Xây dựng cơ bắp",
            "Tăng cơ và vóc dáng săn chắc",
            "Xây dựng cơ bắp"
        )

        addMultiOption(
            "🏃 Giữ dáng",
            "Duy trì vóc dáng hiện tại",
            "Giữ dáng"
        )

        addMultiOption(
            "❤️ Cải thiện sức khỏe",
            "Tăng cường thể lực mỗi ngày",
            "Cải thiện sức khỏe"
        )

        addMultiOption(
            "⚡ Tăng sức bền",
            "Tập lâu hơn và ít mệt hơn",
            "Tăng sức bền"
        )
    }

    private fun showFocusStep() {
        txtQuestion.text = "Hãy chọn vùng tập trung của bạn"
        txtSubQuestion.text = "Bạn muốn tập trung vào phần nào?"

        addOption("🎯  Toàn thân") {
            focusArea = "Toàn thân"
        }

        addOption("💪  Cánh tay") {
            focusArea = "Cánh tay"
        }

        addOption("🏋️  Ngực") {
            focusArea = "Ngực"
        }

        addOption("🔥  Bụng") {
            focusArea = "Bụng"
        }

        addOption("🦵  Chân") {
            focusArea = "Chân"
        }
    }

    private fun showBodyInfoStep() {
        txtQuestion.text = "Hãy cho chúng tôi biết thêm về bạn"
        txtSubQuestion.text = "Vuốt để chọn chỉ số cơ thể của bạn"

        layoutOptions.removeAllViews()
        layoutOptions.orientation = LinearLayout.VERTICAL
        layoutOptions.gravity = Gravity.CENTER

        val weightTitle = createTitle("Cân nặng")

        val pickerWeight = NumberPicker(this)
        pickerWeight.minValue = 30
        pickerWeight.maxValue = 200
        pickerWeight.value = weightValue
        pickerWeight.wrapSelectorWheel = true

        val heightTitle = createTitle("Chiều cao")

        val pickerHeight = NumberPicker(this)
        pickerHeight.minValue = 120
        pickerHeight.maxValue = 220
        pickerHeight.value = heightValue
        pickerHeight.wrapSelectorWheel = true


        // Thiết lập màu chữ đậm hơn và kích thước to hơn (dành cho Android 10 trở lên)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            pickerWeight.textColor = Color.BLACK
            pickerWeight.textSize = 80f // Làm số to lên

            pickerHeight.textColor = Color.BLACK
            pickerHeight.textSize = 80f // Làm số to lên
        }

        pickerWeight.setOnValueChangedListener { _, _, newVal ->
            weightValue = newVal
        }

        pickerHeight.setOnValueChangedListener { _, _, newVal ->
            heightValue = newVal
        }

        layoutOptions.addView(weightTitle)
        layoutOptions.addView(
            pickerWeight,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                500 // Tăng chiều cao lên một chút để chứa số to
            )
        )

        layoutOptions.addView(heightTitle)
        layoutOptions.addView(
            pickerHeight,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                500 // Tăng chiều cao lên một chút để chứa số to
            )
        )
    }

    private fun createTitle(text: String): TextView {
        val title = TextView(this)
        title.text = text
        title.textSize = 28f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(Color.BLACK)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 30, 0, 16)
        return title
    }


    private fun showPlanReadyStep() {
        txtQuestion.text = "Kế hoạch của bạn đã\nsẵn sàng!"
        txtSubQuestion.text = "Dựa trên mục tiêu và chỉ số cơ thể của bạn"

        layoutOptions.removeAllViews()

        // Card container với Gradient sang trọng
        val card = android.widget.FrameLayout(this)
        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 40, 0, 40)
        
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.parseColor("#1E3C72"), Color.parseColor("#2A5298"), Color.parseColor("#7028E4"))
        )
        gradient.cornerRadius = 80f
        card.background = gradient
        card.setPadding(60, 80, 60, 80)
        card.elevation = 30f

        val contentLayout = LinearLayout(this)
        contentLayout.orientation = LinearLayout.VERTICAL

        val badge = TextView(this)
        badge.text = "PREMIUM PLAN"
        badge.textSize = 11f
        badge.letterSpacing = 0.1f
        badge.setTextColor(Color.WHITE)
        badge.setTypeface(null, Typeface.BOLD)
        badge.background = createRoundedBackground(Color.argb(80, 255, 255, 255), 100f)
        badge.setPadding(35, 12, 35, 12)

        val badgeParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        badgeParams.setMargins(0, 0, 0, 40)

        val planTitle = TextView(this)
        planTitle.text = "TẬP ${focusArea.uppercase()}"
        planTitle.textSize = 36f
        planTitle.setTextColor(Color.WHITE)
        planTitle.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD))

        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL
        grid.setPadding(0, 60, 0, 0)

        grid.addView(createStatRow("⏱  15-25 phút", "🔥  ${if (gender == "Nam") "Hardcore" else "Fitness"}"))
        grid.addView(createStatRow("🎯  $focusArea", "✅  Full Focus"))

        contentLayout.addView(badge, badgeParams)
        contentLayout.addView(planTitle)
        contentLayout.addView(grid)

        // Ảnh nhân vật tinh tế hơn
        val imageView = ImageView(this)
        imageView.setImageResource(if (gender == "Nam") R.drawable.male else R.drawable.female)
        imageView.alpha = 0.5f
        val imgParams = android.widget.FrameLayout.LayoutParams(600, 600)
        imgParams.gravity = Gravity.END or Gravity.BOTTOM
        imgParams.setMargins(0, 0, -50, -50) 

        card.addView(imageView, imgParams)
        card.addView(contentLayout)

        layoutOptions.addView(card, cardParams)

        // CHỨC NĂNG NÂNG CAO: Phân tích chỉ số cơ thể (BMI)
        val analysisContainer = LinearLayout(this)
        analysisContainer.orientation = LinearLayout.VERTICAL
        analysisContainer.setPadding(20, 30, 20, 50)
        
        val hM = heightValue.toDouble() / 100
        val bmi = weightValue.toDouble() / (hM * hM)
        val bmiStatus = when {
            bmi < 18.5 -> "Cần tăng cân"
            bmi < 25 -> "Cân đối"
            else -> "Cần giảm mỡ"
        }

        val txtBmi = TextView(this).apply {
            text = "BMI: ${String.format("%.1f", bmi)} ($bmiStatus)"
            setTextColor(Color.parseColor("#4CAF50"))
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        analysisContainer.addView(txtBmi)
        layoutOptions.addView(analysisContainer)

        // Nâng cấp nút Bắt đầu
        btnNext.text = "BẮT ĐẦU NGAY"
        val btnBg = GradientDrawable()
        btnBg.cornerRadius = 100f
        btnBg.setColor(Color.WHITE)
        btnNext.background = btnBg
        btnNext.setTextColor(Color.BLACK)
    }

    private fun createStatRow(left: String, right: String): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 15, 0, 15) // Tăng khoảng cách giữa các dòng

        val tvLeft = TextView(this)
        tvLeft.text = left
        tvLeft.setTextColor(Color.WHITE)
        tvLeft.textSize = 16f // Tăng cỡ chữ thông số
        tvLeft.setTypeface(null, Typeface.BOLD)

        val tvRight = TextView(this)
        tvRight.text = right
        tvRight.setTextColor(Color.WHITE)
        tvRight.textSize = 16f // Tăng cỡ chữ thông số
        tvRight.setTypeface(null, Typeface.BOLD)
        tvRight.setPadding(60, 0, 0, 0)

        row.addView(tvLeft)
        row.addView(tvRight)
        return row
    }

    private fun addGenderOption(
        text: String,
        imageRes: Int,
        onClick: () -> Unit
    ) {
        val itemLayout = LinearLayout(this)
        itemLayout.orientation = LinearLayout.VERTICAL
        itemLayout.gravity = Gravity.CENTER
        itemLayout.setPadding(14, 14, 14, 18)
        itemLayout.background = createRoundedBackground(
            Color.WHITE,
            34f
        )
        itemLayout.elevation = 12f
        itemLayout.isClickable = true
        itemLayout.isFocusable = true

        val imageView = ImageView(this)
        imageView.setImageResource(imageRes)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.adjustViewBounds = true

        val imageParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            1200
        )

        val textView = TextView(this)
        textView.text = text
        textView.textSize = 28f
        textView.setTextColor(Color.BLACK)
        textView.setTypeface(null, Typeface.BOLD)
        textView.gravity = Gravity.CENTER
        textView.setPadding(0, 14, 0, 0)

        itemLayout.addView(imageView, imageParams)
        itemLayout.addView(textView)

        itemLayout.setOnClickListener {
            selectedLayout?.background = createRoundedBackground(
                Color.WHITE,
                34f
            )

            itemLayout.background = createRoundedBackground(
                Color.rgb(230, 240, 255),
                34f,
                Color.rgb(0, 102, 255),
                4
            )

            textView.setTextColor(Color.rgb(0, 102, 255))

            selectedLayout = itemLayout
            onClick()
        }

        val params = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.weight = 1f
        params.setMargins(8, 0, 8, 0)

        layoutOptions.addView(itemLayout, params)
    }

    private fun addOption(
        text: String,
        onClick: () -> Unit
    ) {
        val option = TextView(this)
        option.text = text
        option.textSize = 20f
        option.setTextColor(Color.BLACK)
        option.setTypeface(null, Typeface.BOLD)
        option.gravity = Gravity.CENTER_VERTICAL
        option.setPadding(34, 0, 34, 0)
        option.background = createRoundedBackground(
            Color.WHITE,
            34f
        )
        option.elevation = 10f

        option.setOnClickListener {
            selectedView?.background = createRoundedBackground(
                Color.WHITE,
                34f
            )
            selectedView?.setTextColor(Color.BLACK)

            option.background = createRoundedBackground(
                Color.rgb(0, 102, 255),
                34f
            )
            option.setTextColor(Color.WHITE)

            selectedView = option
            onClick()
        }

        layoutOptions.addView(option, createOptionParams())
    }

    private fun addMultiOption(
        title: String,
        description: String,
        value: String
    ) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 18, 30, 18)
        layout.background = createRoundedBackground(
            Color.WHITE,
            34f
        )
        layout.elevation = 10f

        val titleView = TextView(this)
        titleView.text = title
        titleView.textSize = 18f
        titleView.setTextColor(Color.BLACK)
        titleView.setTypeface(null, Typeface.BOLD)

        val descView = TextView(this)
        descView.text = description
        descView.textSize = 13f
        descView.setTextColor(Color.GRAY)
        descView.setPadding(0, 6, 0, 0)

        layout.addView(titleView)
        layout.addView(descView)

        layout.setOnClickListener {
            if (goals.contains(value)) {
                goals.remove(value)

                layout.background = createRoundedBackground(
                    Color.WHITE,
                    34f
                )
                titleView.setTextColor(Color.BLACK)
                descView.setTextColor(Color.GRAY)
            } else {
                goals.add(value)

                layout.background = createRoundedBackground(
                    Color.rgb(0, 102, 255),
                    34f
                )
                titleView.setTextColor(Color.WHITE)
                descView.setTextColor(Color.WHITE)
            }
        }

        layoutOptions.addView(layout, createGoalOptionParams())
    }

    private fun createRoundedBackground(
        color: Int,
        radius: Float,
        strokeColor: Int? = null,
        strokeWidth: Int = 0
    ): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.cornerRadius = radius
        drawable.setColor(color)

        if (strokeColor != null) {
            drawable.setStroke(strokeWidth, strokeColor)
        }

        return drawable
    }

    private fun createOptionParams(): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 12, 0, 12)
        return params
    }

    private fun createGoalOptionParams(): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        return params
    }

    private fun saveBodyInfo() {
        val session = SessionManager(this)
        val userId = session.getUserId()

        val sharedPreferences = getSharedPreferences("onboarding_data", MODE_PRIVATE)

        sharedPreferences.edit()
            .putString("weight_$userId", weightValue.toString())
            .putString("height_$userId", heightValue.toString())
            .apply()

        // Save to SQLite
        val dbHelper = DatabaseHelper(this)
        val hM = heightValue.toDouble() / 100
        val bmi = weightValue.toDouble() / (hM * hM)
        dbHelper.saveProgress(userId, weightValue.toDouble(), heightValue.toDouble(), bmi)
    }

    private fun finishOnboarding() {
        saveBodyInfo()

        val session = SessionManager(this)
        val userId = session.getUserId()

        val sharedPreferences =
            getSharedPreferences(
                "onboarding_data",
                Context.MODE_PRIVATE
            )

        val completedKey =
            "is_onboarding_completed_$userId"

        sharedPreferences.edit()
            .putBoolean(completedKey, true)
            .putString("gender_$userId", gender)
            .putString("goal_$userId", goals.joinToString(", "))
            .putString("focus_area_$userId", focusArea)
            .apply()

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )

        intent.putExtra("user_id", userId)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}