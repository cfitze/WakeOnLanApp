package com.example.wakeonlanapp.composables

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wakeonlanapp.viewmodel.WakeOnLanViewModel
import com.example.wakeonlanapp.viewmodel.KeyManager

@Composable
fun WakeOnLanApp(viewModel: WakeOnLanViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var publicKeyVisible by remember { mutableStateOf(false) } // To control visibility of public key
    var keyAliases by remember { mutableStateOf("No key aliases available") } // To display key aliases
    var currentAlias by remember { mutableStateOf("None") } // To display the currently used key alias
    val publicKey = remember { mutableStateOf("") }

    // Generate and retrieve public key on launch
    LaunchedEffect(Unit) {
        val keyManager = KeyManager
        publicKey.value = keyManager.getOpenSshPublicKey()
        viewModel.checkWireGuardState(context)
        keyAliases = KeyManager.listKeyAliases().joinToString(", ") // Convert list to a comma-separated string
        currentAlias = KeyManager.getCurrentAlias() // Fetch the current key alias
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { viewModel.toggleWireGuardState(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(if (viewModel.isWireGuardActive) "Deactivate WireGuard" else "Activate WireGuard")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Network Information:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = viewModel.getDetailedNetworkInfo(context),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.sendWakeOnLanCommand(
                        context = context,
                        host = "10.0.0.3",
                        command = "bash WOL_Curdin_Machine.sh"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("WOL Computer Home")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.sendWakeOnLanCommand(
                        context = context,
                        host = "10.0.0.4",
                        command = "bash WOL_SA_Workstation.sh"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("WOL Workstation")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to show public key
            Button(
                onClick = { publicKeyVisible = !publicKeyVisible },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(if (publicKeyVisible) "Hide Public Key" else "Show Public Key")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display public key if visible
            if (publicKeyVisible) {
                Text(
                    text = "Public Key:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = publicKey.value,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )

                // Button to copy the public key to clipboard
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(publicKey.value))
                        Toast.makeText(context, "Public Key copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Copy Public Key")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display key aliases
            Text(
                text = "Key Aliases:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = keyAliases,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )

            // Display current alias
            Text(
                text = "Currently Used Alias: $currentAlias",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            // Button to refresh key aliases
            Button(
                onClick = {
                    keyAliases = KeyManager.listKeyAliases().joinToString(", ") // Convert list to a comma-separated string
                    currentAlias = KeyManager.getCurrentAlias() // Update the currently used alias
                    Toast.makeText(context, "Key Aliases refreshed!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Refresh Key Aliases")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWakeOnLanApp() {
    val fakeViewModel = WakeOnLanViewModel()
    WakeOnLanApp(viewModel = fakeViewModel)
}
