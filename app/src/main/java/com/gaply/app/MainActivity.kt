package com.gaply.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.gaply.app.core.i18n.LocalStrings
import com.gaply.app.core.i18n.stringsFor
import com.gaply.app.core.i18n.withLocale
import com.gaply.app.di.appContainer
import com.gaply.app.navigation.GaplyNavHost
import com.gaply.app.ui.theme.GaplyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = applicationContext.appContainer()

        setContent {
            val language by container.appLanguage.collectAsState()
            val strings = remember(language) { stringsFor(language) }
            val baseContext = LocalContext.current
            val localizedContext = remember(language) { baseContext.withLocale(language.locale) }

            CompositionLocalProvider(LocalStrings provides strings) {
                CompositionLocalProvider(LocalContext provides localizedContext) {
                    GaplyTheme {
                        GaplyNavHost()
                    }
                }
            }
        }
    }
}
