package com.example.bharatxupi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*

@DelicateCoroutinesApi
class QrActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var amountEditText: EditText
    private lateinit var payButton: Button
    private lateinit var userPhoneNumber: String

    private var currentFragment: Fragment? = null

    private var currentQrData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        amountEditText = findViewById(R.id.amountEditText)
        payButton = findViewById(R.id.payButton)

        payButton.isEnabled = false

        userPhoneNumber = intent.getStringExtra("userPhoneNumber")!!

        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                currentQrData = it.text
                amountEditText.requestFocus()
                imm.showSoftInput(amountEditText, InputMethodManager.SHOW_IMPLICIT)
                payButton.isEnabled = true
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
            currentQrData = ""
            payButton.isEnabled = false
        }

        payButton.setOnClickListener {
            val amount = (amountEditText.text.toString().toFloat() * 100).toInt()
            if (amount <= 100) {
                Toast.makeText(this, "Amount should be greater than Rs 1", Toast.LENGTH_SHORT).show()
            } else {
                val merchantTransactionId = UUID.randomUUID().toString()

                startTransaction(merchantTransactionId, amount, currentQrData)
            }
        }
    }

    private fun openTransactionConfirmationFragment(merchantTransactionId: String) {
        currentFragment = TransactionConfirmationFragment.create(merchantTransactionId) {
            val message = "Transaction was " + when(it) {
                "SUCCESS" -> "successful!"
                else -> "failure!"
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            closeTransactionConfirmationFragment()
        }

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.fragment_container_view, currentFragment!!)
        }
    }

    private fun closeTransactionConfirmationFragment() {
        supportFragmentManager.commit {
            remove(currentFragment!!)
        }
    }

    private fun startTransaction(merchantTransactionId: String, amount: Int, qrData: String) {
        Log.i("BX_API", "starting it")
        lifecycleScope.launch {
            NetworkManager.createTransaction(merchantTransactionId, userPhoneNumber, amount, qrData)
            openTransactionConfirmationFragment(merchantTransactionId)
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}