package com.example.bharatxupi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TransactionConfirmationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var merchantTransactionId: String
    private lateinit var callback: (String) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolveTransactionStatus(merchantTransactionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_confirmation, container, false)
    }

    private fun resolveTransactionStatus(merchantTransactionId: String) {
        lifecycleScope.launch {
            var transactionStatus: String = ""
            while (true) {
                transactionStatus = NetworkManager.getTransactionStatus(merchantTransactionId)
                if (transactionStatus != "PENDING") {
                    requireActivity().runOnUiThread {
                        callback(transactionStatus)
                    }
                    break
                }
                delay(500)
            }
        }
    }

    companion object {
        fun create(merchantTransactionId: String, callback: (String) -> Unit): TransactionConfirmationFragment {
            return TransactionConfirmationFragment().apply {
                this.callback = callback
                this.merchantTransactionId = merchantTransactionId
            }
        }
    }
}
