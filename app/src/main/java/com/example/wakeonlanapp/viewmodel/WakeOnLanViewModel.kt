package com.example.wakeonlanapp.viewmodel

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.wakeonlanapp.https.HTTPSClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Inet4Address

class WakeOnLanViewModel : ViewModel() {
    var isWireGuardActive by mutableStateOf(false)
        private set

    var showWireGuardDialog by mutableStateOf(false)
        private set

    // Function to trigger the WireGuard dialog
    fun promptWireGuardConnection() {
        showWireGuardDialog = true
    }

    // Function to handle dialog dismissal
    fun dismissWireGuardDialog() {
        showWireGuardDialog = false
    }

    // Check if WireGuard is active
    fun checkWireGuardState(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        isWireGuardActive = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
    }

    // Toggle WireGuard state
    fun toggleWireGuardState(context: Context) {
        if (isWireGuardActive) {
            isWireGuardActive = false
            Toast.makeText(context, "WireGuard Deactivated", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    setClassName(
                        "com.wireguard.android",
                        "com.wireguard.android.activity.MainActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WireGuard app not found!", Toast.LENGTH_LONG).show()
            }
            isWireGuardActive = true
            Toast.makeText(context, "WireGuard Activated", Toast.LENGTH_SHORT).show()
        }
    }

    // Get detailed network information
    fun getDetailedNetworkInfo(context: Context): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return "No active network"
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown network type"
            val linkProperties: LinkProperties? = connectivityManager.getLinkProperties(network)

            val connectionType = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Other"
            }

            val ipAddress = linkProperties?.linkAddresses
                ?.find { it.address is Inet4Address }
                ?.address?.hostAddress ?: "Unknown IP"

            val subnetMask = linkProperties?.linkAddresses
                ?.find { it.address is Inet4Address }
                ?.prefixLength?.let { "/$it" } ?: "Unknown"

            val linkSpeed = if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                "N/A"
            } else {
                "N/A"
            }

            """
                Connection Type: $connectionType
                IP Address: $ipAddress
                Subnet Mask: $subnetMask
                Link Speed: $linkSpeed
            """.trimIndent()
        } catch (e: Exception) {
            "Error retrieving network info: ${e.message}"
        }
    }

    // Function to send Wake-On-LAN command using HTTPS
    fun sendWakeOnLanCommandHTTPS(context: Context, url: String, macAddress: String, ip: String, port: Int) {
        if (!isWireGuardActive) {
            Toast.makeText(context, "WireGuard is not connected. Please activate it.", Toast.LENGTH_SHORT).show()
            return
        }

        HTTPSClient.sendWOLRequest(url, macAddress, ip, port) { response, error ->
            CoroutineScope(Dispatchers.Main).launch {
                if (error != null) {
                    val errorMessage = "Failed to send WOL request: $error"
                    Log.e("WakeOnLanViewModel", errorMessage)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                } else {
                    val successMessage = "WOL request successful: $response"
                    Log.d("WakeOnLanViewModel", successMessage)
                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
