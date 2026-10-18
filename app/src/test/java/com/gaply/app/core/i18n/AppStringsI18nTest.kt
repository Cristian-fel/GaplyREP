package com.gaply.app.core.i18n

import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Method

/**
 * Batería i18n: valida que los 8 idiomas estén completos y sean
 * compatibles con el español (referencia).
 *
 * 1. Ningún texto String de [AppStrings] vacío en ningún idioma.
 * 2. Todas las funciones con argumentos se pueden invocar sin lanzar
 *    (String.format/interpolaciones rotas) y conservan cada argumento
 *    que la referencia ES conserva (argumento de formato olvidado).
 */
class AppStringsI18nTest {

    private val languages = AppLanguage.entries

    @Test
    fun `ningun idioma tiene textos vacios`() {
        val getters = AppStrings::class.java.declaredMethods
            .filter { !it.isSynthetic && it.parameterCount == 0 && it.returnType == String::class.java }
        assertTrue("AppStrings no expone getters", getters.size > 200)

        for (lang in languages) {
            val impl = stringsFor(lang)
            for (g in getters) {
                val value = g.invoke(impl) as String
                assertTrue(
                    "[$lang] ${g.name} está vacío",
                    value.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun `las funciones con argumentos no fallan y conservan los datos`() {
        val ref = stringsFor(AppLanguage.ES)
        val funcs = AppStrings::class.java.declaredMethods
            .filter { !it.isSynthetic && it.parameterCount > 0 && it.returnType == String::class.java }
        assertTrue("AppStrings no expone funciones con argumentos", funcs.isNotEmpty())

        for (f in funcs) {
            val args = dummyArgs(f)
            val refOut = f.invoke(ref, *args) as String

            for (lang in languages) {
                val impl = stringsFor(lang)
                val out = try {
                    f.invoke(impl, *args) as String
                } catch (t: Throwable) {
                    throw AssertionError(
                        "[$lang] ${f.name} lanza excepción: ${t.cause ?: t}",
                        t,
                    )
                }
                for (a in args) {
                    val token = a.toString()
                    val esLoUsoEs = refOut.contains(token)
                    assertTrue(
                        "[$lang] ${f.name} pierde el argumento '$token'. " +
                            "ref(ES)='$refOut' vs '$out'",
                        !esLoUsoEs || out.contains(token),
                    )
                }
            }
        }
    }

    @Test
    fun `los idiomas difieren del espanol (traducciones reales, no copias)`() {
        val ref = stringsFor(AppLanguage.ES)
        val sample = AppStrings::class.java.declaredMethods
            .filter { !it.isSynthetic && it.parameterCount == 0 && it.returnType == String::class.java }
            .first { it.name == "getSaveLanguage" }

        for (lang in languages) {
            if (lang == AppLanguage.ES) continue
            val value = sample.invoke(stringsFor(lang)) as String
            assertTrue(
                "[$lang] saveLanguage no está traducido (igual que ES)",
                value != (sample.invoke(ref) as String),
            )
        }
    }

    private fun dummyArgs(f: Method): Array<Any> = f.parameterTypes.map { t ->
        when (t) {
            String::class.java -> "zzq"
            Integer.TYPE, Integer::class.java -> 4271
            java.lang.Boolean.TYPE, java.lang.Boolean::class.java -> true
            java.time.LocalDate::class.java -> java.time.LocalDate.of(2026, 3, 15)
            else -> throw AssertionError("Tipo de argumento no soportado: $t en ${f.name}")
        }
    }.toTypedArray()
}
