package com.example.bharatxupi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OtpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val verifyButton = findViewById<Button>(R.id.verifyOtpButton)!!
        val otpEditText = findViewById<EditText>(R.id.otpEditText)!!

        val otpToken = intent.extras?.getString("otpToken")!!
        val userPhoneNumber = intent.extras?.getString("userPhoneNumber")!!

        verifyButton.setOnClickListener {
            val otpValue = otpEditText.text.toString()
            if (otpValue.length < 4) {
                Toast.makeText(this, "Enter at least 4 digits!", Toast.LENGTH_SHORT).show()
            } else {
                validateOtp(userPhoneNumber, otpToken, otpValue)
            }
        }
    }

    private fun validateOtp(phoneNumber: String, otpToken: String, otpValue: String) {
        GlobalScope.launch {
            if (NetworkManager.validateOtp(phoneNumber, otpToken, otpValue)) {
                setResult(RESULT_OK, intent)
            } else {
                setResult(400, intent)
            }

            finish()
        }
    }
}