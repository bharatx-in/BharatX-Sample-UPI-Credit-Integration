package com.example.bharatxupi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.loginButton)!!
        val phoneNumberEditText = findViewById<EditText>(R.id.phoneNumberEditText)!!

        prefs = getPreferences(Context.MODE_PRIVATE)

        val isAlreadyLoggedIn = prefs.getBoolean("isAlreadyLoggedIn", false)
        if (isAlreadyLoggedIn) {
            val userPhoneNumber = prefs.getString("userPhoneNumber", "")
            moveToQr(userPhoneNumber!!)
        } else {
            findViewById<ConstraintLayout>(R.id.loginRoot)?.apply {
                visibility = View.VISIBLE
            }
        }

        loginButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString()
            if (isPhoneNumberValid(phoneNumber)) {
                attemptLogin(phoneNumber)
            } else {
                Toast.makeText(this, "phone number is invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attemptLogin(phoneNumber: String) {
        GlobalScope.launch {
            if (NetworkManager.isUserRegistered(phoneNumber)) {
                markUserAsLoggedIn(phoneNumber)
                moveToQr(phoneNumber)
            } else {
                val otpToken = NetworkManager.requestForOtp(phoneNumber)
                startActivityForResult(Intent(this@MainActivity, OtpActivity::class.java).apply {
                    putExtra("userPhoneNumber", phoneNumber)
                    putExtra("otpToken", otpToken)
                }, 123)
            }
        }
    }

    private fun moveToQr(phoneNumber: String) {
        startActivity(Intent(this, QrActivity::class.java).apply {
            putExtra("userPhoneNumber", phoneNumber)
        })
    }

    private fun markUserAsLoggedIn(phoneNumber: String) {
        with (prefs.edit()) {
            putBoolean("isAlreadyLoggedIn", true)
            putString("userPhoneNumber", phoneNumber)
            commit()
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.startsWith("+91") && phoneNumber.length == 13
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            123 -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    markUserAsLoggedIn(data?.extras?.getString("userPhoneNumber")!!)
                    moveToQr(data.extras?.getString("userPhoneNumber")!!)
                } else {
                    Toast.makeText(this, "Could not verify OTP!", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
