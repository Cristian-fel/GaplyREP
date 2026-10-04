package com.gaply.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gaply.app.navigation.GaplyNavHost
import com.gaply.app.ui.theme.GaplyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GaplyTheme {
                GaplyNavHost()
            }
        }
    }
}
