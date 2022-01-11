package com.example.bharatxupi

import android.util.Log
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.Exception

object NetworkManager {
    private var api: ApiInterface = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://sdk.bharatx.tech/service/")
        .build().create(ApiInterface::class.java)

    private const val authHeader = "Basic dGVzdFVwaVBhcnRuZXI6ekNyR2NaanNHTG5LZ05kU1dwVWpVdkhtUlRTaEZwVXI"

    suspend fun validateOtp(userPhoneNumber: String, otpToken: String, otpValue: String): Boolean {
        Log.i("BX_API", "validateOtp")
        val response = api.validateOtp(ValidateOtpRequest(userPhoneNumber, otpToken, otpValue), authHeader)
        if (!response.isSuccessful) {
            Log.e("BX_API", response.errorBody().toString())
            return false
        }

        Log.i("BX_API", response.body().toString() )
        return true
    }

    suspend fun isUserRegistered(userPhoneNumber: String): Boolean {
        Log.i("BX_API", "isUserRegistered")
        val response = api.getRegistrationStatus(userPhoneNumber, authHeader)
        if (!response.isSuccessful) {
            Log.e("BX_API", response.errorBody().toString())
            throw Exception("Internet error")
        }

        Log.i("BX_API", response.body().toString() )
        return response.body()!!.registrationStatus == "registered"
    }

    suspend fun requestForOtp(userPhoneNumber: String): String {
        Log.i("BX_API", "requestForOtp")
        val response = api.requestOtp(RequestOtpRequest(userPhoneNumber), authHeader)
        if (!response.isSuccessful) {
            Log.e("BX_API", response.errorBody().toString())
            throw Exception("Internet error")
        }

        Log.i("BX_API", response.body().toString() )
        return response.body()!!.otpToken
    }

    suspend fun createTransaction(merchantTransactionId: String, userPhoneNumber: String, amount: Int, qrData: String): String {
        Log.i("BX_API", "createTransaction")

        val response = api.createTransaction(
            CreateTransactionRequest(
                merchantTransactionId,
                amount,
                userPhoneNumber,
                "QR",
                QrData(
                    qrData,
                ),
            ),
            authHeader,
        )

        if (!response.isSuccessful) {
            Log.e("BX_API", response.message())
            Log.e("BX_API", response.code().toString())
            Log.e("BX_API", response.raw().toString())
            Log.e("BX_API", response.errorBody().toString())
            throw Exception("internet error")
        }

        Log.i("BX_API", response.body().toString())
        return response.body()!!.status
    }

    suspend fun getTransactionStatus(merchantTransactionId: String): String {
        Log.i("BX_API", "getTransaction")

        val response = api.checkTransactionStatus(merchantTransactionId, authHeader)

        if (!response.isSuccessful) {
            Log.e("BX_API", response.errorBody().toString())
            throw Exception("internet error")
        }

        Log.i("BX_API", response.body().toString())
        return response.body()!!.status
    }
}

interface ApiInterface {
    @POST("payments/merchant/user/registration/otp/verify")
    suspend fun validateOtp(@Body request: ValidateOtpRequest, @Header("Authorization") authorizationHeader: String): Response<ValidateOtpResponse>

    @GET("payments/merchant/user/registration/status")
    suspend fun getRegistrationStatus(@Query("userPhoneNumber") userPhoneNumber: String, @Header("Authorization") authorizationHeader: String): Response<RegistrationStatusResponse>

    @POST("payments/merchant/user/registration/otp/request")
    suspend fun requestOtp(@Body request: RequestOtpRequest, @Header("Authorization") authorizationHeader: String): Response<RequestOtpResponse>

    @POST("payments/merchant/transactions/instant")
    suspend fun createTransaction(@Body request: CreateTransactionRequest, @Header("Authorization") authorizationHeader: String): Response<CreateTransactionResponse>

    @GET("payments/merchant/transactions/status")
    suspend fun checkTransactionStatus(@Query("merchantTransactionId") merchantTransactionId: String, @Header("Authorization") authorizationHeader: String): Response<CheckTransactionResponse>
}

data class CheckTransactionResponse(
    @SerializedName("status")
    var status: String,
)

data class CreateTransactionRequest(
    @SerializedName("merchantTransactionId")
    var merchantTransactionId: String,

    @SerializedName("amount")
    var amount: Int,

    @SerializedName("userPhoneNumber")
    var userPhoneNumber: String,

    @SerializedName("target")
    var target: String,

    @SerializedName("QR")
    var qr: QrData,
)

data class QrData(
    @SerializedName("data")
    var data: String,
)

data class CreateTransactionResponse(
    @SerializedName("initiationRequest")
    var initiationRequest: CreateTransactionRequest,

    @SerializedName("transactionStatus")
    var status: String,
)

data class RegistrationStatusResponse(
    @SerializedName("registrationStatus")
    var registrationStatus: String
)

data class RequestOtpRequest(
    @SerializedName("userPhoneNumber")
    var userPhoneNumber: String,
)

data class RequestOtpResponse(
    @SerializedName("otpToken")
    var otpToken: String,
)

data class ValidateOtpResponse(
    @SerializedName("message")
    var message: String,
)

data class ValidateOtpRequest(
    @SerializedName("userPhoneNumber")
    var userPhoneNumber: String,

    @SerializedName("otpToken")
    var otpToken: String,

    @SerializedName("otpValue")
    var otpValue: String,
)
