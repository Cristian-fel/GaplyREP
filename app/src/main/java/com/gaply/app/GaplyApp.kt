package com.gaply.app

import android.app.Application
import com.gaply.app.di.AppContainer
import java.util.Locale

class GaplyApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // El locale por defecto del proceso sigue al idioma guardado:
        // formatos de fecha/hora y pickers del sistema al abrir la app.
        Locale.setDefault(container.appLanguage.value.locale)
    }
}
