package com.example.wakeonlanapp.ssh

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import com.example.wakeonlanapp.viewmodel.KeyManager

object SSHConnectionManager {
    suspend fun executeCommandWithPublicKey(
        context: Context,
        host: String,
        username: String,
        command: String
    ): String {
        val sshClient = SSHClient()
        sshClient.addHostKeyVerifier(PromiscuousVerifier()) // For testing only

        try {
            sshClient.connect(host)
            val identityProvider = KeyStoreIdentityProvider(
                publicKey = KeyManager.getPublicKey(),
                privateKey = KeyManager.getPrivateKey()
            )

            // Authenticate using the identity provider
            sshClient.authPublickey(username, identityProvider)

            // Start a session and execute the command
            val session = sshClient.startSession()
            val cmd = session.exec(command)

            // Read the command output
            val output = cmd.inputStream.bufferedReader().readText()

            cmd.join() // Wait for the command to finish

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Command executed successfully", Toast.LENGTH_SHORT).show()
            }

            // Return the output
            return output
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            throw e
        } finally {
            // Disconnect the client in the end
            sshClient.disconnect()
        }
    }
}
