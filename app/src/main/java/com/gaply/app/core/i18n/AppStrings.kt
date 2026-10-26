package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Catálogo completo de textos de la app (Módulos 1 y 2).
 *
 * Cada idioma implementa este interface; las pantallas NUNCA escriben
 * cadenas literales: leen `LocalStrings.current`. Los valores canónicos
 * que se guardan en perfil/horario (intereses, días de la semana) siguen
 * siendo los del español; aquí solo se traducen para mostrar.
 */
interface AppStrings {

    val javaLocale: java.util.Locale

    // =================================================================
    // COMÚN
    // =================================================================

    val back: String
    val close: String
    val cancel: String
    val accept: String
    val ok: String
    val done: String
    val delete: String
    val edit: String
    val save: String
    val next: String
    val skip: String
    val selected: String
    val add: String
    val retry: String
    val acceptShort: String

    val connectionErrorTitle: String
    val connectionErrorMessage: String
    val noActiveSessionMsg: String
    /** Firestore rechazó la lectura: sesión caducada o reglas sin publicar. */
    val firestoreReadDenied: String

    val successCd: String
    val infoCd: String
    val warningCd: String
    val errorCd: String
    val closeCd: String
    val showPassword: String
    val hidePassword: String
    val expandOptionsCd: String

    /** "Lunes" (clave canónica en español) -> "Monday" / "Montag" / "月曜日". */
    fun dayName(dayKey: String): String

    /** "10:00" -> "10:00 a.m." (12 h). */
    fun formatTime(hora24: String): String

    /** "10:00" - "12:00" -> "10:00 a.m. - 12:00 p.m." */
    fun formatTimeRange(inicio24: String, fin24: String): String =
        "${formatTime(inicio24)} - ${formatTime(fin24)}"

    /** 120 -> "2 horas libres" · 90 -> "90 minutos libres". */
    fun freeDuration(minutos: Int): String

    /** "5 de octubre" para la cabecera del horario. */
    fun formatMonthDay(date: LocalDate): String

    /** "oct." para la etiqueta pequeña del horario. */
    fun formatMonthAbbrev(date: LocalDate): String

    // =================================================================
    // BIENVENIDA
    // =================================================================

    val welcomeTagline: String
    val signIn: String
    val createAccount: String
    val accountDeletedTitle: String
    val accountDeletedMsg: String
    val accountDeactivatedTitle: String
    val accountDeactivatedMsg: String

    // =================================================================
    // INICIO DE SESIÓN
    // =================================================================

    val loginTitle: String
    val usernameLabel: String
    val passwordLabel: String
    val forgotPassword: String
    val loginButton: String
    val noAccountYet: String
    val registerLink: String
    val marketingOptOut: String
    val userNotFoundTitle: String
    val userNotFoundMsg: String
    val invalidEmailFormat: String
    val wrongPasswordMsg: String
    val loginFailedMsg: String
    val noAccountWithEmailMsg: String

    // =================================================================
    // ERRORES DE CAMPO (compartidos)
    // =================================================================

    val fieldRequired: String
    val minChars3: String
    val minChars6: String
    val validEmailPrompt: String
    val passwordsMismatch: String
    val usernameTaken: String
    val emailTaken: String
    val selectBirthdate: String
    val selectUniversity: String

    fun minInterestsWarning(max: Int): String

    // =================================================================
    // REGISTRO
    // =================================================================

    fun stepOfTotal(step: Int, total: Int): String
    val stepAccountData: String
    val stepPersonalInfo: String
    val stepPurpose: String
    val stepInterests: String
    val stepPhotoBio: String

    val createYourAccountTitle: String
    val startWithLoginData: String
    val usernameAtHint: String
    val emailLabel: String
    val confirmPasswordLabel: String

    val personalInfoTitle: String
    val tellUsAboutYou: String
    val firstNameLabel: String
    val lastNameLabel: String
    val birthDateLabel: String
    val dayLabel: String
    val monthLabel: String
    val yearLabel: String
    val whereStudyNow: String
    val universityLabel: String
    val genderIdentityLabel: String
    val chooseYourGender: String

    val whatBringsGaply: String
    val chooseGoalHint: String

    val selectSomeInterests: String
    val addInterests: String
    fun maxInterestsInTotal(max: Int, selected: Int, total: Int): String

    val uploadPhoto: String
    val takePhoto: String
    fun helloName(name: String): String
    val giveShortBio: String
    val bioLabel: String
    val saveProfile: String

    val profileSavedTitle: String
    val profileSavedMsg: String
    val galleryInfoTitle: String
    val galleryInfoMsg: String
    val cameraInfoTitle: String
    val cameraInfoMsg: String
    val saveErrorTitle: String
    val saveErrorMsg: String

    /** Meses en orden (enero..diciembre). */
    val months: List<String>
    val genders: List<String>
    /** "📚 Necesito estudiar" (emoji + etiqueta), en orden. */
    val goals: List<String>
    val universities: List<String>

    /** Interés canónico en español ("Deportes") -> etiqueta localizada. */
    fun interestLabel(key: String): String

    // =================================================================
    // HOME
    // =================================================================

    val tabDiscover: String
    val tabMatches: String
    val tabChats: String
    val tabProfile: String

    val welcomeUser: String
    fun youHaveFreeSlot(descripcion: String): String
    val addClassesToSeeSlots: String
    val noFreeSlotsToday: String
    val suggestionsToday: String
    val createActivitySection: String
    val addYourOwnActivity: String
    val inviteOthers: String
    val culturalAgenda: String
    val visitWebsite: String
    val matchesEmptyMsg: String
    val discoverMatchesCta: String
    val chatsEmptyMsg: String
    val eventSiteInfoMsg: String

    val placeLibrary: String
    val placeCourt: String
    val placeMultiRoom: String
    val placeGreenZone: String
    val placeAuditorium: String

    val profilePhotoCd: String
    val notificationsCd: String
    val addCd: String

    // =================================================================
    // PERFIL
    // =================================================================

    val profileFallbackUsername: String
    val completeYourProfile: String
    val descriptionTitle: String
    val noBioYet: String
    val interestsTitle: String
    val chooseInterestsHint: String
    val scheduleSectionTitle: String
    val addScheduleAction: String
    val addClassesHint: String
    val upsTitle: String
    val settingsCd: String
    val changeProfilePhotoCd: String
    val accountUnknownError: String
    val photoChangeError: String

    // =================================================================
    // EDITAR PERFIL
    // =================================================================

    val editProfileTitle: String
    val photoAndBasicsTitle: String
    val photoBasicsSubtitle: String
    val profilePhotoPreviewCd: String
    val currentPhotoCd: String
    val gallery: String
    val camera: String
    val namesLabel: String
    val surnamesLabel: String
    val personalDetailsTitle: String
    val selectAnOption: String
    val genderLabel: String
    val interestsAndBioTitle: String
    val interestsBioSubtitle: String
    val descriptionFieldLabel: String
    val bioPlaceholder: String
    val saveChanges: String
    val profilePreviewTitle: String
    val noUniversity: String
    val noInterestsSelected: String
    val changesSavedTitle: String
    val changesSavedMsg: String
    val editSaveErrorMsg: String

    // =================================================================
    // HORARIO
    // =================================================================

    val mySchedule: String
    val prevDay: String
    val nextDay: String
    val addScheduleCd: String
    fun notePrefix(nota: String): String
    val importFromGallery: String
    val importGalleryMsg: String
    val viewActivitiesForSlot: String
    val addScheduleTitle: String
    val tapPlusToAdd: String
    val dayFreeUseArrows: String
    val tapToRetry: String

    val noConnectionSchedule: String
    val firestoreReadSchedule: String
    val loadScheduleFailed: String
    val loadGenericFailed: String
    val saveGenericFailed: String
    val deleteGenericFailed: String
    val firestoreWriteRules: String
    val firestoreDeleteRules: String

    // =================================================================
    // EDITAR HORARIO
    // =================================================================

    val deleteClassTitle: String
    fun deleteClassMessage(nombre: String): String
    val thisClass: String
    val scheduleFormTitle: String
    val closeWithoutSaving: String
    val titleLabel: String
    val titlePlaceholder: String
    val startLabel: String
    val endLabel: String
    val timeSectionLabel: String
    val startsLabel: String
    val endsLabel: String
    val addNotesTitle: String
    val notesPlaceholder: String
    val colorLabel: String
    val selectDay: String
    val selectTime: String
    val selectColor: String
    val classNoLongerExists: String
    val writeTitleError: String
    val endMustBeAfterStart: String

    // =================================================================
    // HUECOS (agente)
    // =================================================================

    val myFreeSlots: String
    val tellUsLikesFreeTime: String
    val yourInterests: String
    fun selectedCount(selected: Int, max: Int): String
    val yourFreeSlotTitle: String
    val fromLabel: String
    val toLabel: String
    val startShort: String
    val endShort: String
    val suggestionsTitle: String
    val fixTimeToSeeSuggestions: String
    val noneFitsSlot: String
    fun doesNotFit(count: Int): String
    val utilityExplanation: String
    fun yourSlotIs(rango: String): String

    fun activityName(id: String, fallback: String): String
    fun activityDescription(id: String, fallback: String): String

    fun reasonInterestsMatch(compartidos: String): String
    val reasonNoInterestsMatch: String
    val reasonNoInterestsRegistered: String
    fun reasonFitsCompletely(hueco: String): String
    fun reasonPartiallyUses(minutos: Int, hueco: String): String
    fun reasonNoFreeSlot(franja: String): String

    // =================================================================
    // CONFIGURACIÓN
    // =================================================================

    val settingsTitle: String
    val accountSecurity: String
    val changePasswords: String
    val updateYourAccess: String
    val deactivateDeleteAccount: String
    /** Subtítulo de la fila: "Pausa temporal o borrado permanente". */
    val deactivateDeleteAccountSubtitle: String
    val twoFactor: String
    val preferences: String
    val theme: String
    val enableDarkMode: String
    val themeSoonMessage: String
    val notifications: String
    val configureAlerts: String
    val language: String
    val saveLanguage: String
    val languageSoonMessage: String
    val aboutSection: String
    val rateTheApp: String
    val aboutGaply: String
    val copyrightText: String
    val logout: String

    // =================================================================
    // CAMBIAR CONTRASEÑA
    // =================================================================

    val changePasswordTitle: String
    val currentPasswordLabel: String
    val newPasswordLabel: String
    val changePasswordButton: String
    val passwordUpdatedTitle: String
    val passwordUpdatedMsg: String
    val changePasswordErrorTitle: String
    val changePasswordReloginMsg: String
    val changePasswordErrorMsg: String

    // =================================================================
    // CUENTA: DESACTIVAR / ELIMINAR
    // =================================================================

    val deleteOrDeactivateTitle: String
    val deleteOrDeactivateDesc: String
    val deactivateOptionTitle: String
    val deactivateOptionDesc: String
    val deleteOptionTitle: String
    val deleteOptionDesc: String
    val confirmDeactivateTitle: String
    val confirmDeactivateDesc: String
    val confirmDeactivateButton: String
    val confirmDeactivateListTitle: String
    val confirmDeactivateBulletPrivacy: String
    val confirmDeactivateBulletOutsideData: String
    val confirmDeactivateBulletDataKept: String
    val confirmDeactivateBulletRecovery: String
    val deactivateErrorTitle: String
    val deactivateErrorMsg: String
    val deactivateReloginMsg: String
    val deleteAccountTitle: String
    val deleteAccountDesc: String
    val deleteAccountButton: String
    val deleteAccountErrorMsg: String
    val deleteAccountConfirmTitle: String
    val deleteAccountErrorTitle: String
    val deleteAccountReloginMsg: String
    val deleteWarningPermanent: String
    val deleteWarningProfileData: String
    val deleteWarningActivity: String
    val deleteWarningIrreversible: String

    // =================================================================
    // RESTABLECER CONTRASEÑA
    // =================================================================

    val resetPasswordHeader: String
    val resetPasswordTitle: String
    val resetPasswordSubtitle: String
    val sendResetLink: String
    val resetLinkSentTitle: String
    val resetLinkSentMsg: String
    val invalidEmailError: String
    val resetPasswordErrorTitle: String
    val resetPasswordErrorMsg: String

    // =================================================================
    // CALIFICAR / ACERCA DE
    // =================================================================

    val rateAppTitle: String
    val rateAppSubtitle: String
    val thanksTitle: String
    val thanksMsg: String
    fun thanksRatingMsg(stars: Int): String
    val starRatingCd: String
    fun starLabel(star: Int): String
    val rateCommentLabel: String
    val rateCommentPlaceholder: String
    val rateSendButton: String
    val aboutLogoCd: String
    val aboutBody: String
    val logoCd: String
    val welcomeIllustrationCd: String

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    /** Botón del Descubrir: "Ver actividades para tu hueco". */
    val activitiesForYourSlot: String
    val activitiesSectionTitle: String
    /** Botón del Descubrir: actividades a las que ya me uní, la más próxima primero. */
    val joinedActivitiesButton: String
    /** Título del listado anterior. */
    val joinedActivitiesTitle: String
    /** Estado vacío del listado anterior. */
    val joinedActivitiesEmpty: String
    /** Tarjeta del Descubrir: "Tus actividades". */
    val myActivitiesCardTitle: String
    /** Subtítulo de la tarjeta de "Tus actividades". */
    val myActivitiesCardSubtitle: String

    /** Título del listado: "Actividades para tu hueco". */
    val activitiesListTitle: String
    /** Título del detalle: "Actividades". */
    val activitiesDetailTitle: String
    /** Banner de ejemplo del hueco en las pantallas del módulo. */
    val huecoBannerDemo: String
    /** Subtítulo del listado: "Según tus intereses". */
    val accordingToYourInterests: String
    val activitiesEmptyListMsg: String
    val likeCd: String
    fun myActivitiesEmptyMsg(createWith: String): String
    val cancelActivityTitle: String
    val cancelActivityConfirmMsg: String
    val confirmCancelActivity: String

    val activityUnavailableMsg: String
    val organizerLabel: String
    fun attendeesCount(count: Int): String
    val joinActivity: String
    val alreadyJoined: String
    /** Confirmación de salir de una actividad a la que te uniste (deshacer "Unirme"). */
    val leaveActivityTitle: String
    val leaveActivityConfirmMsg: String
    val confirmLeaveActivity: String
    val comingSoonTitle: String
    val comingSoonMsg: String
    val eventChat: String
    val locationLabel: String
    val howToGetThere: String
    val beFirstToJoin: String
    val manageYourActivity: String

    val newActivityTitle: String
    val editActivityTitle: String
    val eventTitleLabel: String
    val categoryLabel: String
    val activityPhotoLabel: String
    val locationPlaceLabel: String
    val locationPlacePlaceholder: String
    val durationLabel: String
    val dateLabel: String
    val descriptionOptionalLabel: String
    val activityDescriptionPlaceholder: String
    val publishActivity: String
    val selectTimeTitle: String
    val expandCategoriesCd: String
    val placePhotoCd: String
    val removePhotoCd: String
    val tapToAddPlacePhoto: String
    val defaultColorPhotoHint: String
    /** Etiquetas del selector segmentado (el valor guardado es el canónico en ES). */
    val duration30: String
    val duration1h: String
    val duration2h: String

    // =================================================================
    // MÓDULO 5 · MATCHES
    // =================================================================

    /** Mazo agotado: "No tenemos más sugerencias por el momento". */
    val matchesNoMoreSuggestions: String
    /** Pie del mazo con la función de utilidad del agente (evidencia de IA). */
    val matchesUtilityExplanation: String
    /** Estado de carga compartido de Matches y Chats. */
    val loadingData: String

    /** Etiquetas de los indicadores que aparecen al arrastrar la tarjeta. */
    val swipeLike: String
    val swipePass: String
    val swipeSuper: String
    /** Descripción accesible del botón de super me gusta. */
    val superLikeCd: String

    /** Diálogo de match mutuo: "¡Es un match!". */
    val matchTitle: String
    fun matchMessage(name: String): String
    val openChat: String

    /** Diálogo al unirse a una actividad desde el mazo (CTA al chat). */
    val activityJoinedTitle: String
    fun activityJoinedMsg(titulo: String): String

    /** "20 años" (la edad solo se muestra si hay fecha de nacimiento válida). */
    fun ageYears(age: Int): String
    /** "2 horas en común" a partir de los minutos de solape de huecos. */
    fun commonFreeDuration(minutos: Int): String

    /** Motivo de descarte del agente de matches (sin huecos en común). */
    val reasonNoCommonFreeSlot: String
    fun reasonSharedFreeSlot(rango: String): String

    // =================================================================
    // MÓDULO 5 · CHATS
    // =================================================================

    /** Distintivo de los chats de grupo (actividad) en la lista. */
    val chatGroupTag: String
    val chatNoMessages: String
    val messageInputHint: String
    val sendMessageCd: String
    val chatActivityActive: String
    fun chatActivityScheduled(fecha: String): String

    /** Tiempo relativo del último mensaje de la lista de chats. */
    val timeJustNow: String
    fun timeAgoMinutes(minutes: Int): String
    fun timeAgoHours(hours: Int): String
    fun timeAgoDays(days: Int): String

    /** Descripción accesible del badge de no leídos. */
    fun unreadMessages(count: Int): String

    /** Cabecera fija de la pestaña Chats (nombre de la app). */
    val appTitle: String
    /** Campo de búsqueda de chats de la pestaña Chats. */
    val searchChatsHint: String
    /** Estado vacío cuando la búsqueda no encuentra nada. */
    val searchNoResults: String

    /** Aviso al tocar "Chat del evento" sin ser parte de la actividad. */
    val chatOnlyForMembersTitle: String
    val chatOnlyForMembersMsg: String

    /** Menú de los tres puntos de la pestaña Chats. */
    val deleteChatOption: String
    val pinChatOption: String
    /** Confirmación antes de quitar un chat de la lista. */
    val deleteChatConfirmTitle: String
    val deleteChatConfirmMsg: String
    /** Indicación mientras el listado está en modo eliminar/destacar. */
    val pickChatHint: String
}
