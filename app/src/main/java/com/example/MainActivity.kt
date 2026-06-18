package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.data.VaccineDatabase
import com.example.data.VaccineRepository
import com.example.notification.NotificationHelper
import com.example.ui.VaccineViewModel
import com.example.ui.VaccineViewModelFactory
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize components
    val database = VaccineDatabase.getDatabase(this)
    val notificationHelper = NotificationHelper(this)
    val repository = VaccineRepository(database.vaccineDao(), notificationHelper)
    
    val viewModel: VaccineViewModel by viewModels {
      VaccineViewModelFactory(repository)
    }

    enableEdgeToEdge()

    // Request dynamic POST_NOTIFICATIONS permission on Android 13+
    checkAndRequestPermissions()

    setContent {
      MyApplicationTheme {
        DashboardScreen(
          viewModel = viewModel,
          notificationHelper = notificationHelper
        )
      }
    }
  }

  private fun checkAndRequestPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
      ) {
        ActivityCompat.requestPermissions(
          this,
          arrayOf(Manifest.permission.POST_NOTIFICATIONS),
          101
        )
      }
    }
  }
}
