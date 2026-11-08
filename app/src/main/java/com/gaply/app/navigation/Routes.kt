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
    const val SETTINGS = "settings"
    const val ACCOUNT_OPTIONS = "account_options"
    const val CONFIRM_DEACTIVATE = "confirm_deactivate"
    const val CHANGE_PASSWORD = "change_password"
    const val DELETE_ACCOUNT = "delete_account"
    const val RATE_APP = "rate_app"
    const val LANGUAGE = "language"
    const val ABOUT = "about"

    // ---- Módulo Actividades (solo rutas nuevas) ----
    const val NEW_ACTIVITY = "new_activity"
    const val ACTIVITIES_LIST = "activities_list"
    const val ACTIVITY_DETAIL = "activity_detail"
    const val PROFILE = "profile"
    const val MY_ACTIVITIES = "my_activities"
    const val EDIT_ACTIVITY = "edit_activity"

    // ---- Módulo 5 · Chats ----
    /** Detalle de una conversación (tiene back propio: no es pestaña). */
    const val CHAT_DETAIL = "chat_detail"
    const val ARG_CHAT_ID = "chatId"

    const val ARG_IDENTIFIER = "identifier"
    const val ARG_CLASS_ID = "classId"
    const val ARG_DIA = "dia"
    const val ARG_INICIO = "inicio"
    const val ARG_FIN = "fin"
    const val ARG_ACCOUNT_MESSAGE = "accountMessage"
    const val ARG_ACTIVITY_ID = "activityId"

    fun reset(identifier: String): String =
        "$RESET?$ARG_IDENTIFIER=${android.net.Uri.encode(identifier)}"

    /** Welcome con el modal de cuenta desactivada/eliminada tras salir. */
    fun welcome(accountMessage: String): String =
        "$WELCOME?$ARG_ACCOUNT_MESSAGE=${android.net.Uri.encode(accountMessage)}"

    /** Formulario del horario: vacío crea una clase nueva; con id la edita. */
    fun scheduleEdit(classId: String = "", dia: String = ""): String =
        "$SCHEDULE_EDIT?$ARG_CLASS_ID=${android.net.Uri.encode(classId)}" +
            "&$ARG_DIA=${android.net.Uri.encode(dia)}"

    /** Sugerencias del agente abiertas desde un hueco del horario. */
    fun huecos(dia: String, inicio: String, fin: String): String =
        "$HUECOS?$ARG_DIA=${android.net.Uri.encode(dia)}&$ARG_INICIO=$inicio&$ARG_FIN=$fin"

    /** Detalle de una actividad publicada, identificada por su id. */
    fun activityDetail(activityId: String): String =
        "$ACTIVITY_DETAIL?$ARG_ACTIVITY_ID=${android.net.Uri.encode(activityId)}"

    /** Formulario en modo edición de una actividad propia (precarga datos). */
    fun editActivity(activityId: String): String =
        "$EDIT_ACTIVITY?$ARG_ACTIVITY_ID=${android.net.Uri.encode(activityId)}"

    /** Conversación abierta desde la lista de chats o desde el diálogo de match. */
    fun chatDetail(chatId: String): String =
        "$CHAT_DETAIL?$ARG_CHAT_ID=${android.net.Uri.encode(chatId)}"

    // ---- Módulo Radar AR (brújula espacial) ----
    /** Pantalla del Radar AR hacia la ubicación de una actividad. */
    const val RADAR_AR = "radar_ar"
    const val ARG_RADAR_TITLE = "radarTitle"
    const val ARG_RADAR_LOCATION = "radarLocation"
    const val ARG_RADAR_LAT = "radarLat"
    const val ARG_RADAR_LNG = "radarLng"

    /**
     * Radar AR abierto desde "Cómo llegar" del detalle de la actividad.
     *
     * [lat]/[lng] son las coordenadas exactas guardadas en la actividad
     * (0.0 = sin GPS → el Radar resuelve con el texto de [location]).
     */
    fun radarAr(title: String, location: String, lat: Double, lng: Double): String =
        "$RADAR_AR?$ARG_RADAR_TITLE=${android.net.Uri.encode(title)}" +
            "&$ARG_RADAR_LOCATION=${android.net.Uri.encode(location)}" +
            "&$ARG_RADAR_LAT=$lat&$ARG_RADAR_LNG=$lng"
}
