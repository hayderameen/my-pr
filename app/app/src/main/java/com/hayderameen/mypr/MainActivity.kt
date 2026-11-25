package com.hayderameen.mypr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayderameen.mypr.data.Settings
import com.hayderameen.mypr.ui.MyPrNavHost
import com.hayderameen.mypr.ui.Routes
import com.hayderameen.mypr.ui.theme.MyPRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MyPrApplication
        setContent {
            MyPRTheme {
                val settings by app.settings.settings.collectAsStateWithLifecycle(initialValue = null as Settings?)
                var seeded by remember { mutableStateOf(app.seeded.isCompleted) }
                LaunchedEffect(Unit) {
                    app.seeded.await()
                    seeded = true
                }
                val current = settings
                if (current == null || !seeded) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
                } else {
                    // Decided once; finishing onboarding navigates rather than recomposing the host.
                    val start = remember { if (current.onboarded) Routes.WORKOUTS else Routes.ONBOARDING }
                    MyPrNavHost(startDestination = start)
                }
            }
        }
    }
}
