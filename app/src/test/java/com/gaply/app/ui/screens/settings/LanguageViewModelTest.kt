package com.gaply.app.ui.screens.settings

import com.gaply.app.core.i18n.AppLanguage
import com.gaply.app.di.LanguagePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Doble falso: guarda el idioma en memoria, sin Android ni Firebase. */
private class FakeLanguagePreference(initial: AppLanguage) : LanguagePreference {
    private val _appLanguage = MutableStateFlow(initial)
    override val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    override fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
    }
}

class LanguageViewModelTest {

    private lateinit var preference: FakeLanguagePreference

    @Before
    fun setUp() {
        preference = FakeLanguagePreference(AppLanguage.ES)
    }

    private fun viewModelWith(initial: AppLanguage = AppLanguage.ES): LanguageViewModel =
        LanguageViewModel(preference = preference, initialLanguage = initial)

    // --------------------------------------------------------------- lista

    @Test
    fun `la lista muestra unicamente español e inglés`() {
        val vm = viewModelWith()
        assertEquals(listOf(AppLanguage.ES, AppLanguage.EN), vm.languages)
        assertEquals(2, vm.languages.size)
    }

    @Test
    fun `la lista no incluye los demás idiomas`() {
        val vm = viewModelWith()
        val excluidos = listOf(
            AppLanguage.FR, AppLanguage.DE, AppLanguage.IT,
            AppLanguage.PT, AppLanguage.JA, AppLanguage.ZH,
        )
        for (lang in excluidos) {
            assertEquals("[$lang] no debe aparecer en la lista", false, lang in vm.languages)
        }
    }

    // ------------------------------------------------------------- select()

    @Test
    fun `seleccionar español cambia la seleccion`() {
        val vm = viewModelWith(AppLanguage.EN)
        vm.select(AppLanguage.ES)
        assertEquals(AppLanguage.ES, vm.selected)
    }

    @Test
    fun `seleccionar inglés cambia la seleccion`() {
        val vm = viewModelWith(AppLanguage.ES)
        vm.select(AppLanguage.EN)
        assertEquals(AppLanguage.EN, vm.selected)
    }

    @Test
    fun `seleccionar un idioma fuera de la lista no cambia nada`() {
        val vm = viewModelWith(AppLanguage.ES)
        vm.select(AppLanguage.FR)
        assertEquals(AppLanguage.ES, vm.selected)
        vm.select(AppLanguage.JA)
        assertEquals(AppLanguage.ES, vm.selected)
    }

    // ------------------------------------------------------------- save()

    @Test
    fun `save persiste el idioma seleccionado en la preferencia`() {
        val vm = viewModelWith(AppLanguage.ES)
        vm.select(AppLanguage.EN)
        vm.save()
        assertEquals(AppLanguage.EN, preference.appLanguage.value)
    }

    @Test
    fun `save sin cambiar seleccion persiste el inicial`() {
        val vm = viewModelWith(AppLanguage.EN)
        vm.save()
        assertEquals(AppLanguage.EN, preference.appLanguage.value)
    }

    @Test
    fun `save ignorando un no disponible no persiste el no disponible`() {
        val vm = viewModelWith(AppLanguage.ES)
        vm.select(AppLanguage.DE)
        vm.save()
        assertEquals(AppLanguage.ES, preference.appLanguage.value)
    }

    // --------------------------------------------------- idioma inicial

    @Test
    fun `idioma inicial español se respeta`() {
        val vm = viewModelWith(AppLanguage.ES)
        assertEquals(AppLanguage.ES, vm.selected)
    }

    @Test
    fun `idioma inicial inglés se respeta`() {
        val vm = viewModelWith(AppLanguage.EN)
        assertEquals(AppLanguage.EN, vm.selected)
    }

    @Test
    fun `idioma inicial fuera de la lista cae al español por defecto`() {
        val vm = viewModelWith(AppLanguage.FR)
        assertEquals(AppLanguage.ES, vm.selected)
        val vm2 = LanguageViewModel(preference, AppLanguage.JA)
        assertEquals(AppLanguage.ES, vm2.selected)
    }
}
