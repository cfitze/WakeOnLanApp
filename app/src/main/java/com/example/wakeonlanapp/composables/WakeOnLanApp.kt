package com.example.wakeonlanapp.composables

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wakeonlanapp.viewmodel.WakeOnLanViewModel

@Composable
fun WakeOnLanApp(viewModel: WakeOnLanViewModel) {
    val context = LocalContext.current

    // Check WireGuard state and local network status on app start
    LaunchedEffect(Unit) {
        viewModel.checkWireGuardState(context)
        viewModel.checkIfOnLocalNetwork(context) // Check local network
        if (!viewModel.isOnLocalNetwork && !viewModel.isWireGuardActive) {
            viewModel.promptWireGuardConnection()
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the WireGuard connection prompt dialog
            WireGuardPromptDialog(viewModel, context)

            // Display local network status
            Text(
                text = "On Local Network: ${viewModel.isOnLocalNetwork}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Current Network: ${viewModel.currentNetwork}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button to toggle WireGuard state
            Button(
                onClick = { viewModel.toggleWireGuardState(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(if (viewModel.isWireGuardActive) "Deactivate WireGuard" else "Activate WireGuard")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Network Information Display
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

            // Button to send WOL to Computer Home
            Button(
                onClick = {
                    viewModel.sendWakeOnLanCommandHTTPS(
                        context = context,
                        localUrl = "http://192.168.1.115:5000",  // Local Home IP
                        wireGuardUrl = "http://10.0.0.3:5000",   // WireGuard Home IP
                        macAddress = "04:7C:16:EB:F8:C9",
                        ip = "192.168.1.255",
                        port = 9
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("WOL Computer Home")
            }

            // Spacer
            Spacer(modifier = Modifier.height(16.dp))

            // Button to send WOL to Workstation
            Button(
                onClick = {
                    viewModel.sendWakeOnLanCommandHTTPS(
                        context = context,
                        localUrl = "http://192.168.2.110:5000",  // Local Work IP
                        wireGuardUrl = "http://10.0.0.4:5000",   // WireGuard Work IP
                        macAddress = "D8:43:AE:43:51:40",
                        ip = "192.168.2.255",
                        port = 9
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("WOL Workstation")
            }

        }
    }
}

@Composable
fun WireGuardPromptDialog(viewModel: WakeOnLanViewModel, context: Context) {
    if (viewModel.showWireGuardDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissWireGuardDialog() },
            title = { Text("Connect to WireGuard?") },
            text = { Text("This app requires a secure connection via WireGuard. Please connect to continue.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissWireGuardDialog()
                        viewModel.toggleWireGuardState(context) // Attempt to connect to WireGuard
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.dismissWireGuardDialog() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWakeOnLanApp() {
    val fakeViewModel = WakeOnLanViewModel()
    WakeOnLanApp(viewModel = fakeViewModel)
}
