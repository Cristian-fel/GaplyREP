package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Textos en español: es la implementación de referencia (textos del Figma). */
object StringsEs : AppStrings {

    override val javaLocale: Locale = Locale("es", "CO")

    // =================================================================
    // COMÚN
    // =================================================================

    override val back = "Volver"
    override val close = "Cerrar"
    override val cancel = "Cancelar"
    override val accept = "Aceptar"
    override val ok = "OK"
    override val done = "Listo"
    override val delete = "Eliminar"
    override val edit = "Editar"
    override val save = "Guardar"
    override val next = "Siguiente"
    override val skip = "Omitir"
    override val selected = "Seleccionado"
    override val add = "Agregar"
    override val retry = "Toca para reintentar"
    override val acceptShort = "Aceptar"

    override val connectionErrorTitle = "¡Error de conexión!"
    override val connectionErrorMessage = "Comprueba tu conexión, inténtalo más tarde."
    override val noActiveSessionMsg = "No hay una sesión activa"
    override val firestoreReadDenied =
        "Firestore no permitió leer estos datos (sesión o reglas de seguridad)."

    override val successCd = "Éxito"
    override val infoCd = "Información"
    override val warningCd = "Advertencia"
    override val errorCd = "Error / Sin conexión"
    override val closeCd = "Cerrar"
    override val showPassword = "Mostrar contraseña"
    override val hidePassword = "Ocultar contraseña"
    override val expandOptionsCd = "Desplegar opciones"

    override fun dayName(dayKey: String): String = dayKey

    override fun formatTime(hora24: String): String {
        val partes = hora24.trim().split(":")
        val horas = partes.getOrNull(0)?.toIntOrNull() ?: 0
        val minutos = partes.getOrNull(1)?.toIntOrNull() ?: 0
        val sufijo = if (horas < 12) "a.m." else "p.m."
        val hora12 = when {
            horas == 0 || horas == 12 -> 12
            horas > 12 -> horas - 12
            else -> horas
        }
        return "%d:%02d %s".format(hora12, minutos, sufijo)
    }

    override fun freeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 hora libre" else "$horas horas libres"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min libres"
        else -> "$minutos minutos libres"
    }

    override fun formatMonthDay(date: LocalDate): String =
        DateTimeFormatter.ofPattern("d 'de' MMMM", javaLocale).format(date)

    override fun formatMonthAbbrev(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", javaLocale).format(date)

    // =================================================================
    // BIENVENIDA
    // =================================================================

    override val welcomeTagline = "Encuentra algo que hacer entre tus clases"
    override val signIn = "Inicia Sesión"
    override val createAccount = "Crear Cuenta"
    override val accountDeletedTitle = "¡Cuenta eliminada!"
    override val accountDeletedMsg = "Tu cuenta y datos se han eliminado correctamente."
    override val accountDeactivatedTitle = "¡Cuenta desactivada!"
    override val accountDeactivatedMsg =
        "Puedes reactivarla cuando quieras con tus mismos datos de acceso."

    // =================================================================
    // INICIO DE SESIÓN
    // =================================================================

    override val loginTitle = "Inicia Sesión Con Cuenta de Gaply"
    override val usernameLabel = "Usuario"
    override val passwordLabel = "Contraseña"
    override val forgotPassword = "¿Olvidaste tu contraseña?"
    override val loginButton = "Ingresar"
    override val noAccountYet = "¿Aún no tienes cuenta? "
    override val registerLink = "Regístrate"
    override val marketingOptOut =
        "Si no deseas recibir comunicaciones de marketing sobre nuestros productos y servicios, marca esta casilla."
    override val userNotFoundTitle = "¡Cuenta no encontrada!"
    override val userNotFoundMsg = "No existe una cuenta con ese usuario. Revísalo o regístrate."
    override val invalidEmailFormat = "El formato del correo no es válido"
    override val wrongPasswordMsg = "La contraseña es incorrecta"
    override val loginFailedMsg = "No se pudo iniciar sesión. Intenta de nuevo."
    override val noAccountWithEmailMsg = "No existe una cuenta con ese correo"

    // =================================================================
    // ERRORES DE CAMPO
    // =================================================================

    override val fieldRequired = "Este campo es obligatorio"
    override val minChars3 = "Mínimo 3 caracteres"
    override val minChars6 = "Mínimo 6 caracteres"
    override val validEmailPrompt = "Ingresa un correo válido"
    override val passwordsMismatch = "Las contraseñas no coinciden"
    override val usernameTaken = "Ese nombre de usuario ya está en uso"
    override val emailTaken = "Ese correo ya está registrado"
    override val selectBirthdate = "Selecciona tu fecha de nacimiento"
    override val selectUniversity = "Selecciona tu universidad"

    override fun minInterestsWarning(max: Int) = "Puedes elegir hasta $max intereses."

    // =================================================================
    // REGISTRO
    // =================================================================

    override fun stepOfTotal(step: Int, total: Int) = "Paso $step de $total"
    override val stepAccountData = "Datos de la cuenta"
    override val stepPersonalInfo = "Información personal"
    override val stepPurpose = "Propósito"
    override val stepInterests = "Intereses"
    override val stepPhotoBio = "Foto y biografía"

    override val createYourAccountTitle = "Crea tu cuenta"
    override val startWithLoginData = "Empieza con tus datos de acceso."
    override val usernameAtHint = "Usuario (@username)"
    override val emailLabel = "Correo"
    override val confirmPasswordLabel = "Confirmar Contraseña"

    override val personalInfoTitle = "Información personal"
    override val tellUsAboutYou = "Cuéntanos un poco sobre ti."
    override val firstNameLabel = "Nombre *"
    override val lastNameLabel = "Apellidos"
    override val birthDateLabel = "Fecha de nacimiento *"
    override val dayLabel = "Día"
    override val monthLabel = "Mes"
    override val yearLabel = "Año"
    override val whereStudyNow = "¿Dónde estudias actualmente?"
    override val universityLabel = "Universidad *"
    override val genderIdentityLabel = "Identidad de género"
    override val chooseYourGender = "Elige tu género"

    override val whatBringsGaply = "¿Qué te trae a Gaply?"
    override val chooseGoalHint = "Elige el objetivo que mejor te describa. Podrás cambiarlo después."

    override val selectSomeInterests = "Selecciona algunos de tus intereses"
    override val addInterests = "Agregar intereses"
    override fun maxInterestsInTotal(max: Int, selected: Int, total: Int) =
        "Máximo $max intereses en total ($selected/$total)."

    override val uploadPhoto = "Cargar foto"
    override val takePhoto = "Tomar Foto"
    override fun helloName(name: String) = "Hola $name"
    override val giveShortBio = "Danos una breve descripción:"
    override val bioLabel = "Biografía"
    override val saveProfile = "Guardar perfil"

    override val profileSavedTitle = "¡Tu perfil se ha guardado!"
    override val profileSavedMsg =
        "¡Tu perfil se ha guardado! La información de tu perfil se ha guardado correctamente."
    override val galleryInfoTitle = "Galería de fotos"
    override val galleryInfoMsg = "En la versión final, este botón abrirá tu galería de fotos."
    override val cameraInfoTitle = "Cámara"
    override val cameraInfoMsg = "En la versión final, este botón abrirá la cámara de tu celular."
    override val saveErrorTitle = "¡Error al guardar!"
    override val saveErrorMsg = "¡Error al guardar el perfil! Disculpa, inténtalo más tarde."

    override val months = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
    )

    override val genders = listOf("Femenino", "Masculino", "No binario", "Prefiero no decirlo")

    override val goals = listOf(
        "\uD83D\uDCDA Necesito estudiar",
        "\uD83C\uDFC0 Qué hay para hacer",
        "\uD83D\uDE09 Tengo tiempo libre",
        "\uD83E\uDD7A Aún no lo tengo claro",
    )

    override val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "Otra",
    )

    override fun interestLabel(key: String): String = key

    // =================================================================
    // HOME
    // =================================================================

    override val tabDiscover = "Descubrir"
    override val tabMatches = "Matches"
    override val tabChats = "Chats"
    override val tabProfile = "Perfil"

    override val welcomeUser = "Bienvenido"
    override fun youHaveFreeSlot(descripcion: String) = "Tienes $descripcion"
    override val addClassesToSeeSlots =
        "Agrega tus clases en Mi horario para ver tus huecos libres"
    override val noFreeSlotsToday = "Hoy no tienes huecos libres"
    override val suggestionsToday = "Sugerencias para hoy"
    override val createActivitySection = "Crear la actividad"
    override val addYourOwnActivity = "Agregar tu propia actividad"
    override val inviteOthers = "Invita a otros a unirse en tu hueco"
    override val culturalAgenda = "Agenda cultural"
    override val visitWebsite = "ir al sitio"
    override val matchesEmptyMsg = "Tus matches aparecerán aquí."
    override val discoverMatchesCta = "Encuentra a alguien con tu mismo hueco"
    override val chatsEmptyMsg = "Tus conversaciones aparecerán aquí."
    override val eventSiteInfoMsg = "En la versión final, este enlace abrirá el sitio del evento."

    override val placeLibrary = "Biblioteca"
    override val placeCourt = "Cancha M7A"
    override val placeMultiRoom = "Salón Polifuncional"
    override val placeGreenZone = "Zona Verde"
    override val placeAuditorium = "Auditorio"

    override val profilePhotoCd = "Foto de perfil"
    override val notificationsCd = "Notificaciones"
    override val addCd = "Agregar"

    // =================================================================
    // PERFIL
    // =================================================================

    override val profileFallbackUsername = "Usuario"
    override val completeYourProfile = "Completa tu perfil"
    override val descriptionTitle = "Descripción"
    override val noBioYet = "Aún no has agregado una descripción."
    override val interestsTitle = "Intereses"
    override val chooseInterestsHint = "Elige tus intereses desde el botón Editar."
    override val scheduleSectionTitle = "Horario"
    override val addScheduleAction = "Agregar horario"
    override val addClassesHint = "Agrega tus clases para descubrir tus huecos libres."
    override val upsTitle = "¡Ups!"
    override val settingsCd = "Configuración"
    override val changeProfilePhotoCd = "Cambiar foto de perfil"
    override val accountUnknownError = "No pudimos identificar tu cuenta. Cierra sesión y vuelve a entrar."
    override val photoChangeError = "No pudimos cambiar tu foto. Inténtalo más tarde."

    // =================================================================
    // EDITAR PERFIL
    // =================================================================

    override val editProfileTitle = "Editar Perfil"
    override val photoAndBasicsTitle = "Foto de Perfil y Básicos"
    override val photoBasicsSubtitle = "Esta es la foto que verán otros usuarios en el swipe."
    override val profilePhotoPreviewCd = "Vista previa de foto de perfil"
    override val currentPhotoCd = "Foto de perfil actual"
    override val gallery = "Galería"
    override val camera = "Cámara"
    override val namesLabel = "Nombres"
    override val surnamesLabel = "Apellidos"
    override val personalDetailsTitle = "Detalles Personales"
    override val selectAnOption = "Seleccione una opción"
    override val genderLabel = "Género"
    override val interestsAndBioTitle = "Intereses y descripción"
    override val interestsBioSubtitle = "Selecciona hasta 3 intereses (aparecen en tu perfil)"
    override val descriptionFieldLabel = "Descripción"
    override val bioPlaceholder = "Cuéntanos un poco sobre ti..."
    override val saveChanges = "Guardar cambios"
    override val profilePreviewTitle = "Vista previa del perfil"
    override val noUniversity = "Sin universidad"
    override val noInterestsSelected = "Aún no has seleccionado intereses."
    override val changesSavedTitle = "¡Cambios guardados!"
    override val changesSavedMsg = "Tu perfil se actualizó correctamente."
    override val editSaveErrorMsg = "No pudimos guardar los cambios. Inténtalo más tarde."

    // =================================================================
    // HORARIO
    // =================================================================

    override val mySchedule = "Mi horario"
    override val prevDay = "Día anterior"
    override val nextDay = "Día siguiente"
    override val addScheduleCd = "Agregar horario"
    override fun notePrefix(nota: String) = "Nota: $nota"
    override val importFromGallery = "Importar desde galería"
    override val importGalleryMsg =
        "Elige una foto de tu horario. En la versión final, Gaply leerá las clases automáticamente desde la imagen."
    override val viewActivitiesForSlot = "Ver actividades para este hueco"
    override val addScheduleTitle = "Agregar Horario"
    override val tapPlusToAdd = "Toca \"+\" para añadir el horario"
    override val dayFreeUseArrows = "Este día está libre. Usa ‹ › para ver los demás días."
    override val tapToRetry = "Toca para reintentar"

    override val noConnectionSchedule = "Sin conexión. Revisa tu internet e intenta de nuevo."
    override val firestoreReadSchedule = "Firestore no permitió leer tu horario (sesión o reglas)."
    override val loadScheduleFailed = "No se pudo cargar el horario. Intenta de nuevo."
    override val loadGenericFailed = "No se pudo cargar. Intenta de nuevo."
    override val saveGenericFailed = "No se pudo guardar. Intenta de nuevo."
    override val deleteGenericFailed = "No se pudo eliminar. Intenta de nuevo."
    override val firestoreWriteRules = "Firestore no permitió guardar (sesión o reglas)."
    override val firestoreDeleteRules = "Firestore no permitió eliminar (sesión o reglas)."

    // =================================================================
    // EDITAR HORARIO
    // =================================================================

    override val deleteClassTitle = "Eliminar clase"
    override fun deleteClassMessage(nombre: String) =
        "¿Eliminar \"$nombre\" del horario? Esta acción no se puede deshacer."
    override val thisClass = "esta clase"
    override val scheduleFormTitle = "Horario"
    override val closeWithoutSaving = "Cerrar sin guardar"
    override val titleLabel = "Título"
    override val titlePlaceholder = "Título de la asignatura"
    override val startLabel = "Inicio"
    override val endLabel = "Fin"
    override val timeSectionLabel = "Hora"
    override val startsLabel = "Comienza"
    override val endsLabel = "Finaliza"
    override val addNotesTitle = "Añadir Notas"
    override val notesPlaceholder = "Escribe una nota (opcional)."
    override val colorLabel = "Color"
    override val selectDay = "Seleccione un día"
    override val selectTime = "Seleccione una hora"
    override val selectColor = "Seleccione un color"
    override val classNoLongerExists = "Esta clase ya no existe o fue eliminada."
    override val writeTitleError = "Escribe el título de la asignatura."
    override val endMustBeAfterStart = "La hora final debe ser posterior a la inicial."

    // =================================================================
    // HUECOS
    // =================================================================

    override val myFreeSlots = "Mis huecos"
    override val tellUsLikesFreeTime =
        "Dinos qué te gusta y en qué horario estás libre. Las sugerencias se ordenan solas."
    override val yourInterests = "Tus intereses"
    override fun selectedCount(selected: Int, max: Int) = "$selected de $max seleccionados"
    override val yourFreeSlotTitle = "Tu hueco libre"
    override val fromLabel = "De"
    override val toLabel = "A"
    override val startShort = "Inicio"
    override val endShort = "Fin"
    override val suggestionsTitle = "Sugerencias"
    override val fixTimeToSeeSuggestions = "Corrige el horario para ver las sugerencias."
    override val noneFitsSlot = "Ninguna actividad encaja en ese hueco. Prueba con otro día u horario."
    override fun doesNotFit(count: Int) = "No encaja ($count)"
    override val utilityExplanation =
        "Utilidad = 65% intereses + 35% encaje horario. Lo que no cabe en tu hueco se descarta con su motivo."
    override fun yourSlotIs(rango: String) = "Tu hueco: $rango"

    override fun activityName(id: String, fallback: String): String = fallback
    override fun activityDescription(id: String, fallback: String): String = fallback

    override fun reasonInterestsMatch(compartidos: String) =
        "Coincide con tus intereses: $compartidos"
    override val reasonNoInterestsMatch = "No coincide con tus intereses actuales"
    override val reasonNoInterestsRegistered = "Aún no tienes intereses registrados"
    override fun reasonFitsCompletely(hueco: String) = "Cabe completo en tu hueco del $hueco"
    override fun reasonPartiallyUses(minutos: Int, hueco: String) =
        "Aprovecha $minutos min de tu hueco del $hueco"
    override fun reasonNoFreeSlot(franja: String) = "No encaja en ningún hueco libre ($franja)"

    // =================================================================
    // CONFIGURACIÓN
    // =================================================================

    override val settingsTitle = "Configuración"
    override val accountSecurity = "Cuenta y Seguridad"
    override val changePasswords = "Cambiar contraseña"
    override val updateYourAccess = "Actualiza tu acceso"
    override val deactivateDeleteAccount = "Desactivar y Eliminar Cuenta"
    override val deactivateDeleteAccountSubtitle = "Pausa temporal o borrado permanente"
    override val twoFactor = "Doble factor de autenticación"
    override val preferences = "Preferencias"
    override val theme = "Tema"
    override val enableDarkMode = "Activa el modo oscuro"
    override val themeSoonMessage = "Estará en la versión final, estamos trabajando en eso."
    override val notifications = "Notificaciones"
    override val configureAlerts = "Configura tus alertas"
    override val language = "Idioma"
    override val saveLanguage = "Guardar idioma"
    override val languageSoonMessage = "Estará en la versión final, estamos trabajando en eso."
    override val aboutSection = "Acerca"
    override val rateTheApp = "Calificar la app"
    override val aboutGaply = "Acerca de Gaply"
    override val copyrightText = "© Gaply 2026 - Version 1.0"
    override val logout = "Cerrar sesión"

    // =================================================================
    // CAMBIAR CONTRASEÑA
    // =================================================================

    override val changePasswordTitle = "Cambiar contraseña"
    override val currentPasswordLabel = "Contraseña actual"
    override val newPasswordLabel = "Nueva Contraseña"
    override val changePasswordButton = "Guardar contraseña"
    override val passwordUpdatedTitle = "¡Contraseña actualizada!"
    override val passwordUpdatedMsg = "Tu contraseña se cambió correctamente."
    override val changePasswordErrorTitle = "No se pudo cambiar"
    override val changePasswordReloginMsg =
        "Por seguridad, cierra sesión y vuelve a entrar para cambiar tu contraseña"
    override val changePasswordErrorMsg = "No se pudo cambiar la contraseña. Inténtalo de nuevo."

    // =================================================================
    // CUENTA: DESACTIVAR / ELIMINAR
    // =================================================================

    override val deleteOrDeactivateTitle = "¿Eliminar o desactivar?"
    override val deleteOrDeactivateDesc =
        "Si quieres abandonar Gaply temporalmente, solo desactiva tu cuenta. " +
            "Si eliges eliminarla, no podrás recuperarla después de trascurridos 30 días."
    override val deactivateOptionTitle = "Desactivar cuenta"
    override val deactivateOptionDesc = "Puedes reactivarla cuando quieras con tus mismos datos de acceso."
    override val deleteOptionTitle = "Eliminar cuenta"
    override val deleteOptionDesc = "Esta acción borra tu cuenta y todos tus datos de forma permanente."
    override val confirmDeactivateTitle = "¿Desactivar esta cuenta?"
    override val confirmDeactivateDesc =
        "Tu cuenta quedará inactiva hasta que vuelvas a iniciar sesión con tus mismos datos de acceso."
    override val confirmDeactivateButton = "Desactivar"
    override val confirmDeactivateListTitle = "Si desactivas la cuenta:"
    override val confirmDeactivateBulletPrivacy = "Nadie podrá ver tu cuenta ni su contenido."
    override val confirmDeactivateBulletOutsideData =
        "Los datos que no se almacenan en tu cuenta (como los mensajes " +
            "directos) aún podrían ser visibles para otros usuarios."
    override val confirmDeactivateBulletDataKept =
        "Gaply conservará tus datos para que puedas recuperarlos cuando " +
            "reactives tu cuenta."
    override val confirmDeactivateBulletRecovery =
        "Podrás reactivar tu cuenta y recuperar todo el contenido en " +
            "cualquier momento usando los mismos detalles de inicio de sesión."
    override val deactivateErrorTitle = "No se pudo desactivar"
    override val deactivateErrorMsg = "Inténtalo de nuevo en unos minutos."
    override val deactivateReloginMsg =
        "Por seguridad, cierra sesión y vuelve a entrar para desactivar tu cuenta"
    override val deleteAccountTitle = "Eliminar cuenta"
    override val deleteAccountDesc =
        "Esta acción es permanente: se eliminarán tu cuenta, tu perfil, tu horario y tus datos."
    override val deleteAccountButton = "Eliminar definitivamente"
    override val deleteAccountErrorMsg = "No se pudo eliminar la cuenta. Inténtalo de nuevo."
    override val deleteAccountConfirmTitle = "¿Eliminar esta cuenta?"
    override val deleteAccountErrorTitle = "No se pudo eliminar"
    override val deleteAccountReloginMsg =
        "Por seguridad, cierra sesión y vuelve a entrar para eliminar tu cuenta"
    override val deleteWarningPermanent = "Eliminar tu cuenta es permanente ."
    override val deleteWarningProfileData = "Todos los datos de tu perfil se borrarán."
    override val deleteWarningActivity = "Tu historial de actividad y el contenido guardado se perderán."
    override val deleteWarningIrreversible = "No puedes deshacer esta acción."

    // =================================================================
    // RESTABLECER CONTRASEÑA
    // =================================================================

    override val resetPasswordHeader = "Recuperar contraseña"
    override val resetPasswordTitle = "Restablecer contraseña"
    override val resetPasswordSubtitle = "Te enviaremos un enlace para crear una nueva contraseña."
    override val sendResetLink = "Enviar enlace"
    override val resetLinkSentTitle = "¡Enlace enviado!"
    override val resetLinkSentMsg = "Revisa tu correo para restablecer tu contraseña."
    override val invalidEmailError = "Ingresa un correo válido"
    override val resetPasswordErrorTitle = "No se pudo enviar el enlace"
    override val resetPasswordErrorMsg = "Verifica el correo e inténtalo de nuevo."

    // =================================================================
    // CALIFICAR / ACERCA DE
    // =================================================================

    override val rateAppTitle = "Califica esta aplicación"
    override val rateAppSubtitle = "Tu opinión nos ayuda a mejorar"
    override val thanksTitle = "¡Gracias por tu opinión!"
    override val thanksMsg = "Tu calificación nos ayuda a mejorar Gaply."
    override fun thanksRatingMsg(stars: Int) =
        "Nos alegra que hayas calificado Gaply con $stars ${if (stars == 1) "estrella" else "estrellas"}."
    override val starRatingCd = "Calificación con estrellas"
    override fun starLabel(star: Int) = "Estrella $star"
    override val rateCommentLabel = "Motivos (opcional):"
    override val rateCommentPlaceholder = "Cuéntanos qué te gustó o qué\npodemos mejorar..."
    override val rateSendButton = "Enviar"
    override val aboutLogoCd = "Logo de Gaply"
    override val logoCd = "Logo de Gaply"
    override val welcomeIllustrationCd = "Ilustración principal"
    override val aboutBody =
        "Gaply nace con el propósito de eliminar las barreras de comunicación y ayudar a los " +
            "estudiantes a aprovechar mejor su tiempo entre clases."

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    override val activitiesForYourSlot = "Ver actividades para tu hueco"
    override val activitiesSectionTitle = "Actividades para tu hueco"
    override val joinedActivitiesButton = "Ver mis actividades actuales"
    override val joinedActivitiesTitle = "Tus actividades actuales"
    override val joinedActivitiesEmpty = "Todavía no estás inscrito en ninguna actividad."
    override val myActivitiesCardTitle = "Tus actividades"
    override val myActivitiesCardSubtitle = "Edita o cancela las que ya publicaste"
    override val activitiesListTitle = "Actividades para tu hueco"
    override val activitiesDetailTitle = "Actividades"
    override val huecoBannerDemo = "Tienes 2 horas libres 12:00 - 2:00 p.m."
    override val accordingToYourInterests = "Según tus intereses"
    override val activitiesEmptyListMsg =
        "Aún no hay actividades publicadas para este hueco. ¡Sé el primero en publicar una!"
    override val likeCd = "Me gusta"
    override fun myActivitiesEmptyMsg(createWith: String) =
        "Aún no has publicado actividades. Crea una con \"$createWith\" y aparecerá aquí."
    override val cancelActivityTitle = "Cancelar actividad"
    override val cancelActivityConfirmMsg = "¿Estás seguro de que deseas cancelar esta actividad?"
    override val confirmCancelActivity = "Sí, cancelar actividad"
    override val activityUnavailableMsg = "Esta actividad ya no está disponible."
    override val organizerLabel = "Organizador"
    override fun attendeesCount(count: Int) = "Asistentes: $count"
    override val joinActivity = "Unirme"
    override val alreadyJoined = "Ya está dentro"
    override val leaveActivityTitle = "Salir de la actividad"
    override val leaveActivityConfirmMsg =
        "¿Quieres salir de esta actividad? Podrás unirte de nuevo cuando quieras."
    override val confirmLeaveActivity = "Sí, salir"
    override val comingSoonTitle = "¡Próximamente!"
    override val comingSoonMsg = "Estamos trabajando en ello y se agregará próximamente"
    override val eventChat = "Chat del evento"
    override val locationLabel = "Ubicación"
    override val howToGetThere = "Cómo llegar"
    override val beFirstToJoin = "Sé el primero en unirte"
    override val manageYourActivity = "Gestiona tu actividad"
    override val newActivityTitle = "Nueva actividad"
    override val editActivityTitle = "Editar actividad"
    override val eventTitleLabel = "Título del evento"
    override val categoryLabel = "Categoría"
    override val activityPhotoLabel = "Foto del lugar de la actividad"
    override val locationPlaceLabel = "Ubicación / Lugar"
    override val locationPlacePlaceholder = "Ej. Cancha M7A, Salón 204, Biblioteca…"
    override val durationLabel = "Duración estimada"
    override val dateLabel = "Fecha"
    override val descriptionOptionalLabel = "Descripción (opcional)"
    override val activityDescriptionPlaceholder = "Agrega más detalles sobre tu actividad..."
    override val publishActivity = "Publicar actividad"
    override val selectTimeTitle = "Seleccione una hora"
    override val expandCategoriesCd = "Desplegar categorías"
    override val placePhotoCd = "Foto del lugar"
    override val removePhotoCd = "Quitar foto"
    override val tapToAddPlacePhoto = "Toca para agregar una foto del lugar"
    override val defaultColorPhotoHint = "Si no eliges ninguna, se usa un color verde por defecto"
    override val duration30 = "30 min"
    override val duration1h = "1 hora"
    override val duration2h = "2 horas"

    // =================================================================
    // MÓDULO 5 · MATCHES
    // =================================================================

    override val matchesNoMoreSuggestions = "No tenemos más sugerencias por el momento"
    override val matchesUtilityExplanation =
        "Utilidad = 60% intereses + 40% huecos en común. " +
            "Solo aparecen perfiles con tiempo libre compartido."
    override val loadingData = "Cargando…"

    override val swipeLike = "ME GUSTA"
    override val swipePass = "PASAR"
    override val swipeSuper = "SUPER"
    override val superLikeCd = "Super me gusta"

    override val matchTitle = "¡Es un match!"
    override fun matchMessage(name: String) =
        "¡A ti y a $name les gustan! Ya pueden chatear."
    override val openChat = "Abrir chat"
    override val activityJoinedTitle = "¡Ya estás dentro de la actividad!"
    override fun activityJoinedMsg(titulo: String) =
        "El chat de \"$titulo\" ya está disponible con sus asistentes."

    override fun ageYears(age: Int) = "$age años"
    override fun commonFreeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 hora en común" else "$horas horas en común"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min en común"
        else -> "$minutos minutos en común"
    }

    override val reasonNoCommonFreeSlot = "No tienen huecos libres en común"
    override fun reasonSharedFreeSlot(rango: String) = "Hueco en común: $rango"

    // =================================================================
    // MÓDULO 5 · CHATS
    // =================================================================

    override val chatGroupTag = "Grupo"
    override val chatNoMessages = "Todavía no hay mensajes. ¡Envía el primero!"
    override val messageInputHint = "Escribe un mensaje..."
    override val sendMessageCd = "Enviar mensaje"
    override val chatActivityActive = "Activa hoy"
    override fun chatActivityScheduled(fecha: String) = "Programada: $fecha"

    override val timeJustNow = "ahora"
    override fun timeAgoMinutes(minutes: Int) = "hace $minutes min"
    override fun timeAgoHours(hours: Int) = "hace $hours h"
    override fun timeAgoDays(days: Int) = if (days <= 1) "ayer" else "hace $days días"

    override fun unreadMessages(count: Int) = "$count mensajes sin leer"

    override val appTitle = "Gaply"
    override val searchChatsHint = "Buscar chats o personas"
    override val searchNoResults = "No hay chats que coincidan con tu búsqueda."

    override val chatOnlyForMembersTitle = "Primero únete a la actividad"
    override val chatOnlyForMembersMsg =
        "Tienes que ser parte primero de la actividad para entrar en su chat."
    override val deleteChatOption = "Eliminar chat"
    override val pinChatOption = "Destacar chat"
    override val deleteChatConfirmTitle = "¿Eliminar este chat?"
    override val deleteChatConfirmMsg =
        "Se quitará de tu lista; los mensajes seguirán guardados en Gaply."
    override val pickChatHint = "Toca el chat sobre el que quieras actuar."
}
