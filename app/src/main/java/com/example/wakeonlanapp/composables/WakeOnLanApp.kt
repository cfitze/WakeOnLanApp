package com.example.wakeonlanapp.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

    // Check WireGuard state on launch
    LaunchedEffect(Unit) {
        viewModel.checkWireGuardState(context)
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                        //url = "https://10.0.0.3:5000",
                        url = "http://10.0.0.3:5000",   // Change to HTTPS
                        macAddress = "04:7C:16:EB:F8:C9",
                        ip = "192.168.1.255",
                        port = 9
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("WOL Computer Home (HTTPS)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to send WOL to Workstation
            Button(
                onClick = {
                    viewModel.sendWakeOnLanCommandHTTPS(
                        context = context,
                        url = "http://10.0.0.4:5000",
                        //url = "https://10.0.0.4:5000",
                        macAddress = "D8:43:AE:43:51:40",
                        ip = "192.168.2.255",
                        port = 9
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("WOL Workstation (HTTPS)")
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
