package com.example.wakeonlanapp.https

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object HTTPSClient {
    private val client = OkHttpClient()

    fun sendWOLRequest(url: String, macAddress: String, callback: (String?, String?) -> Unit) {
        val json = JSONObject()
        json.put("mac_address", macAddress)

        val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())
        val request = Request.Builder()
            .url("$url/wol")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(response.body?.string(), null)
                } else {
                    callback(null, "Error: ${response.code}")
                }
            }
        })
    }

    fun executeCommand(url: String, command: String, callback: (String?, String?) -> Unit) {
        val json = JSONObject()
        json.put("command", command)

        val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())
        val request = Request.Builder()
            .url("$url/execute")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(response.body?.string(), null)
                } else {
                    callback(null, "Error: ${response.code}")
                }
            }
        })
    }
}
