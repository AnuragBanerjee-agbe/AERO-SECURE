package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.VpnDatabase
import com.example.data.VpnRepository
import com.example.vpn.VpnViewModel
import com.example.vpn.VpnViewModelFactory
import com.example.ui.VpnScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize secure VPN local Room database
        val database = VpnDatabase.getDatabase(this)
        val repository = VpnRepository(database.vpnRecordDao())

        // Acquire VpnViewModel with physical application and database repository reference
        val viewModel: VpnViewModel by viewModels {
            VpnViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        VpnScreen(viewModel)
                    }
                }
            }
        }
    }
}
