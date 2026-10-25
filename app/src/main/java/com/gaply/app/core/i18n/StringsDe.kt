package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Deutsche Texte. */
object StringsDe : AppStrings {

    override val javaLocale: Locale = Locale.GERMANY

    // =================================================================
    // ALLGEMEIN
    // =================================================================

    override val back = "Zurück"
    override val close = "Schließen"
    override val cancel = "Abbrechen"
    override val accept = "OK"
    override val ok = "OK"
    override val done = "Fertig"
    override val delete = "Löschen"
    override val edit = "Bearbeiten"
    override val save = "Speichern"
    override val next = "Weiter"
    override val skip = "Überspringen"
    override val selected = "Ausgewählt"
    override val add = "Hinzufügen"
    override val retry = "Zum Wiederholen tippen"
    override val acceptShort = "Akzeptieren"

    override val connectionErrorTitle = "Verbindungsfehler!"
    override val connectionErrorMessage = "Prüfe deine Verbindung und versuche es später erneut."
    override val noActiveSessionMsg = "Keine aktive Sitzung"
    override val firestoreReadDenied =
        "Firestore hat das Lesen dieser Daten abgelehnt (Sitzung oder Sicherheitsregeln)."

    override val successCd = "Erfolg"
    override val infoCd = "Information"
    override val warningCd = "Warnung"
    override val errorCd = "Fehler / Offline"
    override val closeCd = "Schließen"
    override val showPassword = "Passwort anzeigen"
    override val hidePassword = "Passwort verbergen"
    override val expandOptionsCd = "Optionen ausklappen"

    override fun dayName(dayKey: String): String = when (dayKey) {
        "Lunes" -> "Montag"
        "Martes" -> "Dienstag"
        "Miércoles" -> "Mittwoch"
        "Jueves" -> "Donnerstag"
        "Viernes" -> "Freitag"
        "Sábado" -> "Samstag"
        "Domingo" -> "Sonntag"
        else -> dayKey
    }

    override fun formatTime(hora24: String): String = hora24.trim()

    override fun freeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 freie Stunde" else "$horas freie Stunden"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} Min. frei"
        else -> "$minutos Minuten frei"
    }

    override fun formatMonthDay(date: LocalDate): String =
        DateTimeFormatter.ofPattern("d. MMMM", javaLocale).format(date)

    override fun formatMonthAbbrev(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", javaLocale).format(date)

    // =================================================================
    // WILLKOMMEN
    // =================================================================

    override val welcomeTagline = "Finde etwas zwischen den Vorlesungen"
    override val signIn = "Anmelden"
    override val createAccount = "Konto erstellen"
    override val accountDeletedTitle = "Konto gelöscht!"
    override val accountDeletedMsg = "Dein Konto und deine Daten wurden erfolgreich gelöscht."
    override val accountDeactivatedTitle = "Konto deaktiviert!"
    override val accountDeactivatedMsg =
        "Du kannst es jederzeit mit denselben Zugangsdaten wieder aktivieren."

    // =================================================================
    // ANMELDUNG
    // =================================================================

    override val loginTitle = "Mit deinem Gaply-Konto anmelden"
    override val usernameLabel = "Benutzername"
    override val passwordLabel = "Passwort"
    override val forgotPassword = "Passwort vergessen?"
    override val loginButton = "Anmelden"
    override val noAccountYet = "Noch kein Konto? "
    override val registerLink = "Registrieren"
    override val marketingOptOut =
        "Wenn du keine Marketing-Kommunikation über unsere Produkte und Dienstleistungen erhalten möchtest, setze dieses Häkchen."
    override val userNotFoundTitle = "Konto nicht gefunden!"
    override val userNotFoundMsg = "Es existiert kein Konto mit diesem Benutzernamen. Prüfe ihn oder registriere dich."
    override val invalidEmailFormat = "Das E-Mail-Format ist ungültig"
    override val wrongPasswordMsg = "Das Passwort ist falsch"
    override val loginFailedMsg = "Anmeldung fehlgeschlagen. Versuche es erneut."
    override val noAccountWithEmailMsg = "Es existiert kein Konto mit dieser E-Mail"

    // =================================================================
    // FELDFEHLER
    // =================================================================

    override val fieldRequired = "Dieses Feld ist erforderlich"
    override val minChars3 = "Mindestens 3 Zeichen"
    override val minChars6 = "Mindestens 6 Zeichen"
    override val validEmailPrompt = "Gib eine gültige E-Mail ein"
    override val passwordsMismatch = "Die Passwörter stimmen nicht überein"
    override val usernameTaken = "Dieser Benutzername wird bereits verwendet"
    override val emailTaken = "Diese E-Mail ist bereits registriert"
    override val selectBirthdate = "Wähle dein Geburtsdatum"
    override val selectUniversity = "Wähle deine Universität"

    override fun minInterestsWarning(max: Int) = "Du kannst bis zu $max Interessen auswählen."

    // =================================================================
    // REGISTRIERUNG
    // =================================================================

    override fun stepOfTotal(step: Int, total: Int) = "Schritt $step von $total"
    override val stepAccountData = "Kontodaten"
    override val stepPersonalInfo = "Persönliche Informationen"
    override val stepPurpose = "Zweck"
    override val stepInterests = "Interessen"
    override val stepPhotoBio = "Foto und Bio"

    override val createYourAccountTitle = "Erstelle dein Konto"
    override val startWithLoginData = "Beginne mit deinen Zugangsdaten."
    override val usernameAtHint = "Benutzername (@username)"
    override val emailLabel = "E-Mail"
    override val confirmPasswordLabel = "Passwort bestätigen"

    override val personalInfoTitle = "Persönliche Informationen"
    override val tellUsAboutYou = "Erzähl uns ein wenig über dich."
    override val firstNameLabel = "Vorname *"
    override val lastNameLabel = "Nachname"
    override val birthDateLabel = "Geburtsdatum *"
    override val dayLabel = "Tag"
    override val monthLabel = "Monat"
    override val yearLabel = "Jahr"
    override val whereStudyNow = "Wo studierst du gerade?"
    override val universityLabel = "Universität *"
    override val genderIdentityLabel = "Geschlechtsidentität"
    override val chooseYourGender = "Wähle dein Geschlecht"

    override val whatBringsGaply = "Was bringt dich zu Gaply?"
    override val chooseGoalHint = "Wähle das Ziel, das dich am besten beschreibt. Du kannst es später ändern."

    override val selectSomeInterests = "Wähle einige deiner Interessen aus"
    override val addInterests = "Interessen hinzufügen"
    override fun maxInterestsInTotal(max: Int, selected: Int, total: Int) =
        "Maximal $max Interessen insgesamt ($selected/$total)."

    override val uploadPhoto = "Foto hochladen"
    override val takePhoto = "Foto aufnehmen"
    override fun helloName(name: String) = "Hallo $name"
    override val giveShortBio = "Gib uns eine kurze Beschreibung:"
    override val bioLabel = "Bio"
    override val saveProfile = "Profil speichern"

    override val profileSavedTitle = "Dein Profil wurde gespeichert!"
    override val profileSavedMsg =
        "Dein Profil wurde gespeichert! Deine Profilinformationen wurden erfolgreich gespeichert."
    override val galleryInfoTitle = "Fotogalerie"
    override val galleryInfoMsg = "In der finalen Version öffnet dieser Button deine Fotogalerie."
    override val cameraInfoTitle = "Kamera"
    override val cameraInfoMsg = "In der finalen Version öffnet dieser Button die Kamera deines Handys."
    override val saveErrorTitle = "Fehler beim Speichern!"
    override val saveErrorMsg = "Fehler beim Speichern deines Profils! Entschuldige, versuche es später erneut."

    override val months = listOf(
        "Januar", "Februar", "März", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember",
    )

    override val genders = listOf("Weiblich", "Männlich", "Nichtbinär", "Ich möchte nicht angeben")

    override val goals = listOf(
        "\uD83D\uDCDA Ich muss lernen",
        "\uD83C\uDFC0 Was gibt's zu tun",
        "\uD83D\uDE09 Ich habe freie Zeit",
        "\uD83E\uDD7A Mir ist noch nicht klar",
    )

    override val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "Andere",
    )

    override fun interestLabel(key: String): String = when (key) {
        "Deportes" -> "Sport"
        "Música" -> "Musik"
        "Videojuegos" -> "Videospiele"
        "Artes" -> "Kunst"
        "Juegos de mesa" -> "Brettspiele"
        "Películas" -> "Filme"
        "Estudio" -> "Lernen"
        "Eventos" -> "Events"
        "Baile" -> "Tanzen"
        "Fotografía" -> "Fotografie"
        "Viajes" -> "Reisen"
        "Cocina" -> "Kochen"
        "Programación" -> "Programmieren"
        "Moda" -> "Mode"
        "Ciencia" -> "Wissenschaft"
        else -> key
    }

    // =================================================================
    // STARTSEITE
    // =================================================================

    override val tabDiscover = "Entdecken"
    override val tabMatches = "Matches"
    override val tabChats = "Chats"
    override val tabProfile = "Profil"

    override val welcomeUser = "Willkommen"
    override fun youHaveFreeSlot(descripcion: String) = "Du hast $descripcion"
    override val addClassesToSeeSlots =
        "Füge deine Fächer in Mein Stundenplan hinzu, um deine freien Zeiten zu sehen"
    override val noFreeSlotsToday = "Heute hast du keine freien Zeiten"
    override val suggestionsToday = "Vorschläge für heute"
    override val createActivitySection = "Aktivität erstellen"
    override val addYourOwnActivity = "Eigene Aktivität hinzufügen"
    override val inviteOthers = "Lade andere ein, sich in deiner freien Zeit zu treffen"
    override val culturalAgenda = "Kulturprogramm"
    override val visitWebsite = "zur Website"
    override val matchesEmptyMsg = "Deine Matches erscheinen hier."
    override val chatsEmptyMsg = "Deine Unterhaltungen erscheinen hier."
    override val eventSiteInfoMsg = "In der finalen Version öffnet dieser Link die Website der Veranstaltung."

    override val placeLibrary = "Bibliothek"
    override val placeCourt = "Sportplatz M7A"
    override val placeMultiRoom = "Mehrzweckraum"
    override val placeGreenZone = "Grüne Zone"
    override val placeAuditorium = "Auditorium"

    override val profilePhotoCd = "Profilfoto"
    override val notificationsCd = "Benachrichtigungen"
    override val addCd = "Hinzufügen"

    // =================================================================
    // PROFIL
    // =================================================================

    override val profileFallbackUsername = "Benutzername"
    override val completeYourProfile = "Vervollständige dein Profil"
    override val descriptionTitle = "Beschreibung"
    override val noBioYet = "Du hast noch keine Beschreibung hinzugefügt."
    override val interestsTitle = "Interessen"
    override val chooseInterestsHint = "Wähle deine Interessen über die Schaltfläche Bearbeiten."
    override val scheduleSectionTitle = "Stundenplan"
    override val addScheduleAction = "Stundenplan hinzufügen"
    override val addClassesHint = "Füge deine Fächer hinzu, um deine freien Zeiten zu entdecken."
    override val upsTitle = "Ups!"
    override val settingsCd = "Einstellungen"
    override val changeProfilePhotoCd = "Profilfoto ändern"
    override val accountUnknownError = "Wir konnten dein Konto nicht erkennen. Melde dich ab und wieder an."
    override val photoChangeError = "Wir konnten dein Foto nicht ändern. Versuche es später erneut."

    // =================================================================
    // PROFIL BEARBEITEN
    // =================================================================

    override val editProfileTitle = "Profil bearbeiten"
    override val photoAndBasicsTitle = "Profilfoto und Basics"
    override val photoBasicsSubtitle = "Das ist das Foto, das andere beim Swipe sehen."
    override val profilePhotoPreviewCd = "Vorschau des Profilfotos"
    override val currentPhotoCd = "Aktuelles Profilfoto"
    override val gallery = "Galerie"
    override val camera = "Kamera"
    override val namesLabel = "Vornamen"
    override val surnamesLabel = "Nachnamen"
    override val personalDetailsTitle = "Persönliche Details"
    override val selectAnOption = "Wähle eine Option"
    override val genderLabel = "Geschlecht"
    override val interestsAndBioTitle = "Interessen und Bio"
    override val interestsBioSubtitle = "Wähle bis zu 3 Interessen aus (sie erscheinen auf deinem Profil)"
    override val descriptionFieldLabel = "Beschreibung"
    override val bioPlaceholder = "Erzähl uns ein wenig über dich..."
    override val saveChanges = "Änderungen speichern"
    override val profilePreviewTitle = "Profilvorschau"
    override val noUniversity = "Keine Universität"
    override val noInterestsSelected = "Du hast noch keine Interessen ausgewählt."
    override val changesSavedTitle = "Änderungen gespeichert!"
    override val changesSavedMsg = "Dein Profil wurde erfolgreich aktualisiert."
    override val editSaveErrorMsg = "Wir konnten die Änderungen nicht speichern. Versuche es später erneut."

    // =================================================================
    // STUNDENPLAN
    // =================================================================

    override val mySchedule = "Mein Stundenplan"
    override val prevDay = "Vorheriger Tag"
    override val nextDay = "Nächster Tag"
    override val addScheduleCd = "Stundenplan hinzufügen"
    override fun notePrefix(nota: String) = "Notiz: $nota"
    override val importFromGallery = "Aus Galerie importieren"
    override val importGalleryMsg =
        "Wähle ein Foto deines Stundenplans. In der finalen Version liest Gaply deine Fächer automatisch aus dem Bild aus."
    override val viewActivitiesForSlot = "Aktivitäten für diese freie Zeit ansehen"
    override val addScheduleTitle = "Stundenplan hinzufügen"
    override val tapPlusToAdd = "Tippe auf „+\", um den Stundenplan hinzuzufügen"
    override val dayFreeUseArrows = "Dieser Tag ist frei. Nutze ‹ ›, um die anderen Tage zu sehen."
    override val tapToRetry = "Zum Wiederholen tippen"

    override val noConnectionSchedule = "Keine Verbindung. Prüfe dein Internet und versuche es erneut."
    override val firestoreReadSchedule = "Firestore hat das Lesen deines Stundenplans nicht erlaubt (Sitzung oder Regeln)."
    override val loadScheduleFailed = "Stundenplan konnte nicht geladen werden. Versuche es erneut."
    override val loadGenericFailed = "Konnte nicht geladen werden. Versuche es erneut."
    override val saveGenericFailed = "Konnte nicht gespeichert werden. Versuche es erneut."
    override val deleteGenericFailed = "Konnte nicht gelöscht werden. Versuche es erneut."
    override val firestoreWriteRules = "Firestore hat das Speichern nicht erlaubt (Sitzung oder Regeln)."
    override val firestoreDeleteRules = "Firestore hat das Löschen nicht erlaubt (Sitzung oder Regeln)."

    // =================================================================
    // STUNDENPLAN BEARBEITEN
    // =================================================================

    override val deleteClassTitle = "Fach löschen"
    override fun deleteClassMessage(nombre: String) =
        "„$nombre“ aus dem Stundenplan entfernen? Diese Aktion kann nicht rückgängig gemacht werden."
    override val thisClass = "dieses Fach"
    override val scheduleFormTitle = "Stundenplan"
    override val closeWithoutSaving = "Ohne Speichern schließen"
    override val titleLabel = "Titel"
    override val titlePlaceholder = "Titel des Fachs"
    override val startLabel = "Beginn"
    override val endLabel = "Ende"
    override val timeSectionLabel = "Uhrzeit"
    override val startsLabel = "Beginnt"
    override val endsLabel = "Endet"
    override val addNotesTitle = "Notizen hinzufügen"
    override val notesPlaceholder = "Schreibe eine Notiz (optional)."
    override val colorLabel = "Farbe"
    override val selectDay = "Wähle einen Tag"
    override val selectTime = "Wähle eine Uhrzeit"
    override val selectColor = "Wähle eine Farbe"
    override val classNoLongerExists = "Dieses Fach existiert nicht mehr oder wurde gelöscht."
    override val writeTitleError = "Schreibe den Titel des Fachs."
    override val endMustBeAfterStart = "Die Endzeit muss nach der Startzeit liegen."

    // =================================================================
    // FREIE ZEITEN
    // =================================================================

    override val myFreeSlots = "Meine freien Zeiten"
    override val tellUsLikesFreeTime =
        "Sag uns, was du magst und wann du frei bist. Die Vorschläge sortieren sich von selbst."
    override val yourInterests = "Deine Interessen"
    override fun selectedCount(selected: Int, max: Int) = "$selected von $max ausgewählt"
    override val yourFreeSlotTitle = "Deine freie Zeit"
    override val fromLabel = "Von"
    override val toLabel = "Bis"
    override val startShort = "Beginn"
    override val endShort = "Ende"
    override val suggestionsTitle = "Vorschläge"
    override val fixTimeToSeeSuggestions = "Passe die Uhrzeit an, um die Vorschläge zu sehen."
    override val noneFitsSlot = "Keine Aktivität passt in diese freie Zeit. Probiere einen anderen Tag oder eine andere Uhrzeit."
    override fun doesNotFit(count: Int) = "Passt nicht ($count)"
    override val utilityExplanation =
        "Nützlichkeit = 65 % Interessen + 35 % Zeitkompatibilität. Was nicht in deine freie Zeit passt, wird mit Begründung verworfen."
    override fun yourSlotIs(rango: String) = "Deine freie Zeit: $rango"

    override fun activityName(id: String, fallback: String): String = when (id) {
        "futbol" -> "Fußballspiel"
        "repaso" -> "Kaffee und Lernen"
        "mesa" -> "Brettspielturnier"
        "musica" -> "Musiksession"
        "baile" -> "Tanzkurs"
        "artes" -> "Kunstwerkstatt"
        "cine" -> "Kino auf dem Campus"
        "evento" -> "Kulturevent"
        else -> fallback
    }

    override fun activityDescription(id: String, fallback: String): String = when (id) {
        "futbol" -> "Campus-Sportplatz."
        "repaso" -> "Gruppenlernen in der Cafeteria."
        "mesa" -> "Aktivitätsraum."
        "musica" -> "Auditorium."
        "baile" -> "Mehrzweckraum."
        "artes" -> "Kunstraum."
        "cine" -> "Auditorium."
        "evento" -> "Hauptplatz."
        else -> fallback
    }

    override fun reasonInterestsMatch(compartidos: String) =
        "Passt zu deinen Interessen: $compartidos"
    override val reasonNoInterestsMatch = "Passt nicht zu deinen aktuellen Interessen"
    override val reasonNoInterestsRegistered = "Du hast noch keine registrierten Interessen"
    override fun reasonFitsCompletely(hueco: String) = "Passt vollständig in deine freie Zeit am $hueco"
    override fun reasonPartiallyUses(minutos: Int, hueco: String) =
        "Nutzt $minutos Min. deiner freien Zeit am $hueco"
    override fun reasonNoFreeSlot(franja: String) = "Passt in keine freie Zeit ($franja)"

    // =================================================================
    // EINSTELLUNGEN
    // =================================================================

    override val settingsTitle = "Einstellungen"
    override val accountSecurity = "Konto und Sicherheit"
    override val changePasswords = "Passwort ändern"
    override val updateYourAccess = "Aktualisiere deinen Zugang"
    override val deactivateDeleteAccount = "Konto deaktivieren und löschen"
    override val deactivateDeleteAccountSubtitle = "Temporäre Pause oder dauerhaftes Löschen"
    override val twoFactor = "Zwei-Faktor-Authentifizierung"
    override val preferences = "Voreinstellungen"
    override val theme = "Design"
    override val enableDarkMode = "Aktiviere den dunklen Modus"
    override val themeSoonMessage = "Wird in der finalen Version sein, wir arbeiten daran."
    override val notifications = "Benachrichtigungen"
    override val configureAlerts = "Konfiguriere deine Warnmeldungen"
    override val language = "Sprache"
    override val saveLanguage = "Sprache speichern"
    override val languageSoonMessage = "Wird in der finalen Version sein, wir arbeiten daran."
    override val aboutSection = "Über"
    override val rateTheApp = "App bewerten"
    override val aboutGaply = "Über Gaply"
    override val copyrightText = "© Gaply 2026 - Version 1.0"
    override val logout = "Abmelden"

    // =================================================================
    // PASSWORT ÄNDERN
    // =================================================================

    override val changePasswordTitle = "Passwort ändern"
    override val currentPasswordLabel = "Aktuelles Passwort"
    override val newPasswordLabel = "Neues Passwort"
    override val changePasswordButton = "Passwort speichern"
    override val passwordUpdatedTitle = "Passwort aktualisiert!"
    override val passwordUpdatedMsg = "Dein Passwort wurde erfolgreich geändert."
    override val changePasswordErrorTitle = "Konnte nicht geändert werden"
    override val changePasswordReloginMsg =
        "Aus Sicherheitsgründen melde dich ab und wieder an, um dein Passwort zu ändern"
    override val changePasswordErrorMsg = "Passwort konnte nicht geändert werden. Versuche es erneut."

    // =================================================================
    // KONTO: DEAKTIVIEREN / LÖSCHEN
    // =================================================================

    override val deleteOrDeactivateTitle = "Löschen oder deaktivieren?"
    override val deleteOrDeactivateDesc =
        "Wenn du Gaply nur vorübergehend verlassen möchtest, deaktiviere einfach dein Konto. " +
            "Wenn du es löschst, kannst du es nach 30 Tagen nicht wiederherstellen."
    override val deactivateOptionTitle = "Konto deaktivieren"
    override val deactivateOptionDesc = "Du kannst es jederzeit mit denselben Zugangsdaten wieder aktivieren."
    override val deleteOptionTitle = "Konto löschen"
    override val deleteOptionDesc = "Diese Aktion löscht dein Konto und alle deine Daten dauerhaft."
    override val confirmDeactivateTitle = "Dieses Konto deaktivieren?"
    override val confirmDeactivateDesc =
        "Dein Konto bleibt inaktiv, bis du dich mit denselben Zugangsdaten wieder anmeldest."
    override val confirmDeactivateButton = "Deaktivieren"
    override val confirmDeactivateListTitle = "Wenn du das Konto deaktivierst:"
    override val confirmDeactivateBulletPrivacy = "Niemand kann dein Konto oder dessen Inhalte sehen."
    override val confirmDeactivateBulletOutsideData =
        "Daten, die nicht auf deinem Konto gespeichert sind (wie Direktnachrichten), " +
            "sind für andere Benutzer möglicherweise weiterhin sichtbar."
    override val confirmDeactivateBulletDataKept =
        "Gaply behält deine Daten, damit du sie wiederherstellen kannst, wenn du " +
            "dein Konto reaktivierst."
    override val confirmDeactivateBulletRecovery =
        "Du kannst dein Konto jederzeit reaktivieren und alle Inhalte mit " +
            "denselben Zugangsdaten wiederherstellen."
    override val deactivateErrorTitle = "Konnte nicht deaktiviert werden"
    override val deactivateErrorMsg = "Versuche es in einigen Minuten erneut."
    override val deactivateReloginMsg =
        "Aus Sicherheitsgründen melde dich ab und wieder an, um dein Konto zu deaktivieren"
    override val deleteAccountTitle = "Konto löschen"
    override val deleteAccountDesc =
        "Diese Aktion ist endgültig: Dein Konto, dein Profil, dein Stundenplan und deine Daten werden gelöscht."
    override val deleteAccountButton = "Endgültig löschen"
    override val deleteAccountErrorMsg = "Konto konnte nicht gelöscht werden. Versuche es erneut."
    override val deleteAccountConfirmTitle = "Dieses Konto löschen?"
    override val deleteAccountErrorTitle = "Konnte nicht gelöscht werden"
    override val deleteAccountReloginMsg =
        "Aus Sicherheitsgründen melde dich ab und wieder an, um dein Konto zu löschen"
    override val deleteWarningPermanent = "Das Löschen deines Kontos ist endgültig."
    override val deleteWarningProfileData = "Alle Daten deines Profils werden gelöscht."
    override val deleteWarningActivity = "Dein Aktivitätsverlauf und gespeicherte Inhalte gehen verloren."
    override val deleteWarningIrreversible = "Du kannst diese Aktion nicht rückgängig machen."

    // =================================================================
    // PASSWORT ZURÜCKSETZEN
    // =================================================================

    override val resetPasswordHeader = "Passwort wiederherstellen"
    override val resetPasswordTitle = "Passwort zurücksetzen"
    override val resetPasswordSubtitle = "Wir senden dir einen Link, um ein neues Passwort zu erstellen."
    override val sendResetLink = "Link senden"
    override val resetLinkSentTitle = "Link gesendet!"
    override val resetLinkSentMsg = "Prüfe deine E-Mail, um dein Passwort zurückzusetzen."
    override val invalidEmailError = "Gib eine gültige E-Mail ein"
    override val resetPasswordErrorTitle = "Link konnte nicht gesendet werden"
    override val resetPasswordErrorMsg = "Prüfe die E-Mail und versuche es erneut."

    // =================================================================
    // BEWERTEN / ÜBER
    // =================================================================

    override val rateAppTitle = "Bewerte diese App"
    override val rateAppSubtitle = "Deine Meinung hilft uns, besser zu werden"
    override val thanksTitle = "Danke für dein Feedback!"
    override val thanksMsg = "Deine Bewertung hilft uns, Gaply zu verbessern."
    override fun thanksRatingMsg(stars: Int) =
        "Wir freuen uns, dass du Gaply mit $stars ${if (stars == 1) "Stern" else "Sternen"} bewertet hast."
    override val starRatingCd = "Sternbewertung"
    override fun starLabel(star: Int) = "Stern $star"
    override val rateCommentLabel = "Gründe (optional):"
    override val rateCommentPlaceholder = "Erzähl uns, was dir gefallen hat oder was\nwir verbessern können..."
    override val rateSendButton = "Senden"
    override val aboutLogoCd = "Gaply-Logo"
    override val logoCd = "Gaply-Logo"
    override val welcomeIllustrationCd = "Hauptillustration"
    override val aboutBody =
        "Gaply wurde geschaffen, um Kommunikationsbarrieren abzubauen und " +
            "Studierenden zu helfen, ihre Zeit zwischen den Vorlesungen besser zu nutzen."

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    override val activitiesForYourSlot = "Aktivitäten für deinen Slot ansehen"
    override val myActivitiesCardTitle = "Deine Aktivitäten"
    override val myActivitiesCardSubtitle = "Bearbeite oder storniere die bereits veröffentlichten"
    override val activitiesListTitle = "Aktivitäten für deinen Slot"
    override val activitiesDetailTitle = "Aktivitäten"
    override val huecoBannerDemo = "Du hast 2 freie Stunden 12:00 - 2:00 p.m."
    override val accordingToYourInterests = "Nach deinen Interessen"
    override val activitiesEmptyListMsg =
        "Für diesen Slot gibt es noch keine veröffentlichten Aktivitäten. Sei der Erste, der eine veröffentlicht!"
    override val likeCd = "Gefällt mir"
    override fun myActivitiesEmptyMsg(createWith: String) =
        "Du hast noch keine Aktivitäten veröffentlicht. Erstelle eine mit \"$createWith\" und sie erscheint hier."
    override val cancelActivityTitle = "Aktivität stornieren"
    override val cancelActivityConfirmMsg = "Möchtest du diese Aktivität wirklich stornieren?"
    override val confirmCancelActivity = "Ja, Aktivität stornieren"
    override val activityUnavailableMsg = "Diese Aktivität ist nicht mehr verfügbar."
    override val organizerLabel = "Organisator"
    override fun attendeesCount(count: Int) = "Teilnehmer: $count"
    override val joinActivity = "Beitreten"
    override val alreadyJoined = "Du bist bereits dabei"
    override val leaveActivityTitle = "Aktivität verlassen"
    override val leaveActivityConfirmMsg =
        "Möchtest du diese Aktivität verlassen? Du kannst jederzeit wieder beitreten."
    override val confirmLeaveActivity = "Ja, verlassen"
    override val comingSoonTitle = "Bald verfügbar!"
    override val comingSoonMsg = "Wir arbeiten daran und es wird bald hinzugefügt"
    override val eventChat = "Event-Chat"
    override val locationLabel = "Standort"
    override val howToGetThere = "So findest du hin"
    override val beFirstToJoin = "Sei der Erste, der beitritt"
    override val manageYourActivity = "Verwalte deine Aktivität"
    override val newActivityTitle = "Neue Aktivität"
    override val editActivityTitle = "Aktivität bearbeiten"
    override val eventTitleLabel = "Titel der Veranstaltung"
    override val categoryLabel = "Kategorie"
    override val activityPhotoLabel = "Foto des Aktivitätsorts"
    override val locationPlaceLabel = "Ort / Standort"
    override val locationPlacePlaceholder = "z. B. Court M7A, Raum 204, Bibliothek…"
    override val durationLabel = "Geschätzte Dauer"
    override val dateLabel = "Datum"
    override val descriptionOptionalLabel = "Beschreibung (optional)"
    override val activityDescriptionPlaceholder = "Füge mehr Details zu deiner Aktivität hinzu..."
    override val publishActivity = "Aktivität veröffentlichen"
    override val selectTimeTitle = "Uhrzeit auswählen"
    override val expandCategoriesCd = "Kategorien ausklappen"
    override val placePhotoCd = "Foto des Ortes"
    override val removePhotoCd = "Foto entfernen"
    override val tapToAddPlacePhoto = "Tippe, um ein Foto des Ortes hinzuzufügen"
    override val defaultColorPhotoHint = "Wenn du keine auswählst, wird standardmäßig eine grüne Farbe verwendet"
    override val duration30 = "30 Min."
    override val duration1h = "1 Stunde"
    override val duration2h = "2 Stunden"

    // =================================================================
    // MODULE 5 · MATCHES
    // =================================================================

    override val matchesNoMoreSuggestions = "Wir haben vorerst keine weiteren Vorschläge"
    override val matchesUtilityExplanation =
        "Nützlichkeit = 60 % Interessen + 40 % gemeinsame freie Zeiten. " +
            "Es erscheinen nur Profile mit gemeinsamer Freizeit."
    override val loadingData = "Wird geladen…"

    override val swipeLike = "GEFÄLLT"
    override val swipePass = "PASSEN"
    override val swipeSuper = "SUPER"
    override val superLikeCd = "Super Like"

    override val matchTitle = "Es ist ein Match!"
    override fun matchMessage(name: String) =
        "Du und $name mögt euch! Ihr könnt jetzt chatten."
    override val openChat = "Chat öffnen"

    override fun ageYears(age: Int) = "$age Jahre"
    override fun commonFreeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 Stunde gemeinsam" else "$horas Stunden gemeinsam"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min gemeinsam"
        else -> "$minutos Minuten gemeinsam"
    }

    override val reasonNoCommonFreeSlot = "Ihr habt keine gemeinsamen freien Zeiten"
    override fun reasonSharedFreeSlot(rango: String) = "Gemeinsame freie Zeit: $rango"

    // =================================================================
    // MODULE 5 · CHATS
    // =================================================================

    override val chatGroupTag = "Gruppe"
    override val chatNoMessages = "Noch keine Nachrichten. Schreib die erste!"
    override val messageInputHint = "Nachricht schreiben..."
    override val sendMessageCd = "Nachricht senden"
    override val chatActivityActive = "Heute aktiv"
    override fun chatActivityScheduled(fecha: String) = "Geplant für $fecha"

    override val timeJustNow = "gerade eben"
    override fun timeAgoMinutes(minutes: Int) = "vor $minutes Min."
    override fun timeAgoHours(hours: Int) = "vor $hours Std."
    override fun timeAgoDays(days: Int) = if (days <= 1) "gestern" else "vor $days Tagen"

    override fun unreadMessages(count: Int) = "$count ungelesene Nachrichten"
}
