package com.gaply.app.navigation

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val RESET = "reset"
    const val REGISTER = "register"
    const val HOME = "home"
    const val EDIT_PROFILE = "edit_profile"
    const val SCHEDULE = "schedule"
    const val SCHEDULE_EDIT = "schedule_edit"
    const val HUECOS = "huecos"

    const val ARG_IDENTIFIER = "identifier"
    const val ARG_CLASS_ID = "classId"
    const val ARG_DIA = "dia"
    const val ARG_INICIO = "inicio"
    const val ARG_FIN = "fin"

    fun reset(identifier: String): String =
        "$RESET?$ARG_IDENTIFIER=${android.net.Uri.encode(identifier)}"

    /** Formulario del horario: vacío crea una clase nueva; con id la edita. */
    fun scheduleEdit(classId: String = "", dia: String = ""): String =
        "$SCHEDULE_EDIT?$ARG_CLASS_ID=${android.net.Uri.encode(classId)}" +
            "&$ARG_DIA=${android.net.Uri.encode(dia)}"

    /** Sugerencias del agente abiertas desde un hueco del horario. */
    fun huecos(dia: String, inicio: String, fin: String): String =
        "$HUECOS?$ARG_DIA=${android.net.Uri.encode(dia)}&$ARG_INICIO=$inicio&$ARG_FIN=$fin"
}
