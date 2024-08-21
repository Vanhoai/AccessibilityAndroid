package com.dyland.accessibility

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.dyland.accessibility.ui.theme.AccessibilityTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.i("Voice Permission", "Permission granted")
                } else {
                    Log.i("Voice Permission", "Permission not granted")
                }
            }

        setContent {
            val hasRecordAudioPermission = remember {
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }

            AccessibilityTheme {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column {
                            Button(onClick = {
                                requestPermission()
                            }) {
                                Text(text = "Request Accessibility")
                            }

                            Button(onClick = {
                                if (!hasRecordAudioPermission) {
                                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }) {
                                Text(text = "Request Voice")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestPermission() {
        val settingsIntent = Intent(
            Settings.ACTION_ACCESSIBILITY_SETTINGS
        )
        startActivity(settingsIntent)
    }
}

