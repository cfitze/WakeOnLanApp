package com.example.wakeonlanapp.viewmodel
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.util.Properties
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch

class WakeOnLanViewModel : ViewModel() {
    var isWireGuardActive by mutableStateOf(false)
        private set

    var publicKey by mutableStateOf("") // Holds the public key

    init {
        // Generate a key pair on initialization if not already present
        KeyManager.generateKeyPairIfNeeded()
        // Retrieve the public key in OpenSSH format
        publicKey = KeyManager.getOpenSshPublicKey()
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

    // Function to send Wake-On-LAN command
    fun sendWakeOnLanCommand(context: Context, host: String, password: String, command: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = "admin"
                val jsch = JSch()
                val session = jsch.getSession(user, host, 22)
                session.setPassword(password)

                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                session.setConfig(config)
                session.connect()

                val channel = session.openChannel("exec") as ChannelExec
                channel.setCommand(command)
                channel.connect()

                channel.disconnect()
                session.disconnect()

                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Magic Packet Sent Successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
