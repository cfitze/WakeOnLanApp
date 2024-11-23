package com.example.wakeonlanapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.wakeonlanapp.composables.WakeOnLanApp
import com.example.wakeonlanapp.viewmodel.WakeOnLanViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WakeOnLanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WakeOnLanApp(viewModel = viewModel)
        }
    }
}
