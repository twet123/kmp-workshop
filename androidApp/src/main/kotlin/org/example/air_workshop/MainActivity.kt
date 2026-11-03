package org.example.air_workshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.air_workshop.database.setAndroidContext
import org.example.air_workshop.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Room needs a context before Koin builds the database
        setAndroidContext(applicationContext)
        // Idempotent, so activity recreation doesn't restart Koin
        initKoin {
            androidContext(applicationContext)
        }

        setContent {
            App(isDesktop = false)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(isDesktop = false)
}
