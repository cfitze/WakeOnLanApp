package com.example.wakeonlanapp.viewmodel

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
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
import kotlinx.coroutines.withContext
import java.net.Inet4Address

class WakeOnLanViewModel : ViewModel() {

    var isOnLocalNetwork by mutableStateOf(false)
        private set

    var currentNetwork: String by mutableStateOf("unknown")
        private set

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

    // Check if device is on local network for home or work
    suspend fun checkIfOnLocalNetwork(context: Context, homeSSID: String, workSSID: String, homeSubnet: String, workSubnet: String) {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return

            // Get the SSID of the active Wi-Fi network
            val ssid = getSSIDUsingNetworkCapabilities(connectivityManager, activeNetwork)

            when {
                ssid == homeSSID -> {
                    isOnLocalNetwork = true
                    currentNetwork = "home" // Mark the current network as home
                    return
                }
                ssid == workSSID -> {
                    isOnLocalNetwork = true
                    currentNetwork = "work" // Mark the current network as work
                    return
                }
                else -> {
                    // Fallback: Check IP range for home or work subnet
                    isOnLocalNetwork = checkIfInSubnet(connectivityManager, homeSubnet) ||
                            checkIfInSubnet(connectivityManager, workSubnet)
                    currentNetwork = if (isOnLocalNetwork && checkIfInSubnet(connectivityManager, homeSubnet)) {
                        "home"
                    } else if (isOnLocalNetwork && checkIfInSubnet(connectivityManager, workSubnet)) {
                        "work"
                    } else {
                        "unknown"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WakeOnLanViewModel", "Error checking local network: ${e.message}")
            isOnLocalNetwork = false
            currentNetwork = "unknown"
        }
    }


    private suspend fun getSSIDUsingNetworkCapabilities(
        connectivityManager: ConnectivityManager,
        network: Network
    ): String? {
        var ssid: String? = null // Variable to store SSID

        withContext(Dispatchers.IO) {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // Called when Wi-Fi is available
                    Log.d("WakeOnLanViewModel", "Wi-Fi is available")
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities
                ) {
                    capabilities.transportInfo?.let { transportInfo ->
                        if (transportInfo is android.net.wifi.WifiInfo) {
                            ssid = transportInfo.ssid.replace("\"", "") // Remove quotes from SSID
                            Log.d("WakeOnLanViewModel", "Retrieved SSID: $ssid")
                        } else {
                            Log.d("WakeOnLanViewModel", "Transport info is not WifiInfo")
                        }
                    }
                }

                override fun onUnavailable() {
                    Log.d("WakeOnLanViewModel", "Wi-Fi is unavailable")
                }
            }

            connectivityManager.registerNetworkCallback(request, callback)
        }

        return ssid
    }


    // Helper function to check if device IP belongs to local subnet
    private fun checkIfInSubnet(connectivityManager: ConnectivityManager, localSubnet: String): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val linkProperties = connectivityManager.getLinkProperties(network) ?: return false
        val currentIp = linkProperties.linkAddresses
            .find { it.address is Inet4Address }
            ?.address?.hostAddress
        return currentIp?.startsWith(localSubnet) == true
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
