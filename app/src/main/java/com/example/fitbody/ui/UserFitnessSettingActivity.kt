package com.example.fitbody.ui

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R

class UserFitnessSettingActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView

    private lateinit var radioGender: RadioGroup
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var edtGoal: EditText
    private lateinit var spinnerFocusArea: Spinner
    private lateinit var edtWeight: EditText
    private lateinit var edtHeight: EditText
    private lateinit var btnSaveSetting: Button

    private var userId = 0

    private val focusList =
        arrayOf(
            "Toàn thân",
            "Cánh tay",
            "Ngực",
            "Bụng",
            "Chân"
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_fitness_setting)

        userId = intent.getIntExtra("user_id", 0)

        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)

        radioGender = findViewById(R.id.radioGender)
        radioMale = findViewById(R.id.radioMale)
        radioFemale = findViewById(R.id.radioFemale)
        edtGoal = findViewById(R.id.edtGoal)
        spinnerFocusArea = findViewById(R.id.spinnerFocusArea)
        edtWeight = findViewById(R.id.edtWeight)
        edtHeight = findViewById(R.id.edtHeight)
        btnSaveSetting = findViewById(R.id.btnSaveSetting)

        txtTitle.text = "Cài đặt cá nhân"

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                focusList
            )

        spinnerFocusArea.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        loadCurrentData()

        btnSaveSetting.setOnClickListener {
            saveData()
        }
    }

    private fun loadCurrentData() {
        val sharedPreferences =
            getSharedPreferences(
                "onboarding_data",
                Context.MODE_PRIVATE
            )

        val gender =
            sharedPreferences.getString(
                "gender_$userId",
                ""
            )

        val goal =
            sharedPreferences.getString(
                "goal_$userId",
                ""
            )

        val focusArea =
            sharedPreferences.getString(
                "focus_area_$userId",
                "Toàn thân"
            )

        val weight =
            sharedPreferences.getString(
                "weight_$userId",
                ""
            )

        val height =
            sharedPreferences.getString(
                "height_$userId",
                ""
            )

        if (gender == "Nam") {
            radioMale.isChecked = true
        } else if (gender == "Nữ") {
            radioFemale.isChecked = true
        }

        edtGoal.setText(goal)
        edtWeight.setText(weight)
        edtHeight.setText(height)

        val index =
            focusList.indexOf(focusArea)

        if (index >= 0) {
            spinnerFocusArea.setSelection(index)
        }
    }

    private fun saveData() {
        val gender =
            when (radioGender.checkedRadioButtonId) {
                R.id.radioMale -> "Nam"
                R.id.radioFemale -> "Nữ"
                else -> ""
            }

        val goal =
            edtGoal.text.toString().trim()

        val focusArea =
            spinnerFocusArea.selectedItem.toString()

        val weight =
            edtWeight.text.toString().trim()

        val height =
            edtHeight.text.toString().trim()

        if (
            gender.isEmpty() ||
            goal.isEmpty() ||
            weight.isEmpty() ||
            height.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Vui lòng nhập đầy đủ thông tin",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val sharedPreferences =
            getSharedPreferences(
                "onboarding_data",
                Context.MODE_PRIVATE
            )

        sharedPreferences.edit()
            .putBoolean(
                "is_onboarding_completed_$userId",
                true
            )
            .putString(
                "gender_$userId",
                gender
            )
            .putString(
                "goal_$userId",
                goal
            )
            .putString(
                "focus_area_$userId",
                focusArea
            )
            .putString(
                "weight_$userId",
                weight
            )
            .putString(
                "height_$userId",
                height
            )
            .apply()

        Toast.makeText(
            this,
            "Đã lưu thay đổi",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}