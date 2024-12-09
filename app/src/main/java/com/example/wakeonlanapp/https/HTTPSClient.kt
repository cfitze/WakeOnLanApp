package com.example.wakeonlanapp.https

import android.annotation.SuppressLint
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import javax.net.ssl.*

object HTTPSClient {
    private val client: OkHttpClient

    init {
        val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun sendWOLRequest(url: String, macAddress: String, ip: String, port: Int, callback: (String?, String?) -> Unit) {
        val json = JSONObject().apply {
            put("mac", macAddress)
            put("ip", ip)
            put("port", port)
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$url/wol")
            .post(requestBody)
            .build()

        Log.d("HTTPSClient", "Preparing to send WOL request to $url")
        Log.d("HTTPSClient", "Request Payload: $json")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTPSClient", "Request failed: ${e.message}", e)
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("HTTPSClient", "Response Successful: Code: ${response.code}, Body: $responseBody")
                    callback(responseBody, null)
                } else {
                    Log.e("HTTPSClient", "Response Error: Code: ${response.code}, Body: $responseBody")
                    callback(null, "Error: ${response.code}")
                }
            }
        })
    }
}

