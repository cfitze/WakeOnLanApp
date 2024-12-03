package com.example.wakeonlanapp.ssh

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import com.example.wakeonlanapp.viewmodel.KeyManager
import java.io.File

object SSHConnectionManager {
    suspend fun executeCommandWithPublicKey(
        context: Context,
        host: String,
        username: String,
        command: String
    ): String {
        val sshClient = SSHClient()
        sshClient.addHostKeyVerifier(PromiscuousVerifier()) // Use PromiscuousVerifier for testing (not secure for production)

        try {
            Log.d("SSH", "Connecting to $host...")
            sshClient.connect(host)

            // Retrieve the public and private keys from Android Keystore
            val publicKey = KeyManager.getPublicKey()
            val privateKey = KeyManager.getPrivateKey()

            // Log keys for debugging purposes (be careful not to log sensitive keys in production)
            Log.d("SSH", "Public Key: ${KeyManager.getOpenSshPublicKey()}")
            Log.d("SSH", "Private Key: ${privateKey.encoded.contentToString()}")

            // Save the public key for debugging if needed
            File(context.filesDir, "app-public-key.pem").writeBytes(publicKey.encoded)

            // Use the KeyStoreIdentityProvider to wrap the keys for SSHJ
            val identityProvider = KeyStoreIdentityProvider(
                publicKey = publicKey,
                privateKey = privateKey
            )

            Log.d("SSH", "Authenticating with public key...")
            sshClient.authPublickey(username, identityProvider) // Use the identity provider for authentication

            Log.d("SSH", "Starting session and executing command...")
            val session = sshClient.startSession()
            val cmd = session.exec(command)

            // Read the command output
            val output = cmd.inputStream.bufferedReader().readText()

            cmd.join() // Wait for the command to finish
            session.close() // Close the session after execution

            Log.d("SSH", "Command executed successfully. Output: $output")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Command executed successfully", Toast.LENGTH_SHORT).show()
            }

            return output
        } catch (e: Exception) {
            Log.e("SSH", "Error during SSH operation", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            throw e
        } finally {
            Log.d("SSH", "Disconnecting SSH client...")
            sshClient.disconnect()
        }
    }
}
