package com.gaply.app

import android.app.Application
import com.gaply.app.data.seed.MockDataSeeder
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
        // Datos de prueba (QA): inserta usr_01..usr_10 y act_21..act_40 una sola
        // vez. Omite todo si act_21 ya existe; nunca rompe el arranque.
        MockDataSeeder.seedIfEmpty()
    }
}
