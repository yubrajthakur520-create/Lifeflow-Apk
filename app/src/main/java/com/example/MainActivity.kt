package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notifications.HabitReminderScheduler
import com.example.ui.LifeflowApp
import com.example.ui.LifeflowViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        HabitReminderScheduler.ensureChannel(this)
        setContent {
            val vm: LifeflowViewModel = viewModel(factory = LifeflowViewModel.Factory(applicationContext))
            LifeflowApp(viewModel = vm)
        }
    }
}
