package com.gaply.app.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `fromStored resuelve los ids estables de los ocho idiomas`() {
        for (lang in AppLanguage.entries) {
            assertEquals("[$lang] id estable", lang, AppLanguage.fromStored(lang.id))
        }
    }

    @Test
    fun `fromStored resuelve los labels legados`() {
        for (lang in AppLanguage.entries) {
            assertEquals("[$lang] label legado", lang, AppLanguage.fromStored(lang.label))
        }
    }

    @Test
    fun `fromStored con valor desconocido cae al DEFAULT (ES)`() {
        assertEquals(AppLanguage.DEFAULT, AppLanguage.fromStored("xx"))
        assertEquals(AppLanguage.DEFAULT, AppLanguage.fromStored(""))
        assertEquals(AppLanguage.DEFAULT, AppLanguage.fromStored(null))
    }

    @Test
    fun `el DEFAULT es español`() {
        assertEquals(AppLanguage.ES, AppLanguage.DEFAULT)
    }

    @Test
    fun `languageSoonMessage existe y no esta vacio en los ocho idiomas`() {
        for (lang in AppLanguage.entries) {
            val s = stringsFor(lang)
            assertNotNull("[$lang] languageSoonMessage", s.languageSoonMessage)
            assertTrue(
                "[$lang] languageSoonMessage está vacío",
                s.languageSoonMessage.isNotBlank(),
            )
        }
    }

    @Test
    fun `languageSoonMessage difiere entre ES y EN (no es el mismo texto)`() {
        val es = stringsFor(AppLanguage.ES).languageSoonMessage
        val en = stringsFor(AppLanguage.EN).languageSoonMessage
        assertTrue("ES y EN deben traducirse distinto", es != en)
    }

    private fun stringsFor(lang: AppLanguage): AppStrings = when (lang) {
        AppLanguage.ES -> StringsEs
        AppLanguage.EN -> StringsEn
        AppLanguage.FR -> StringsFr
        AppLanguage.DE -> StringsDe
        AppLanguage.IT -> StringsIt
        AppLanguage.PT -> StringsPt
        AppLanguage.JA -> StringsJa
        AppLanguage.ZH -> StringsZh
    }
}
