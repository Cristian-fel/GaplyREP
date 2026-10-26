package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Testi in italiano (Italia). */
object StringsIt : AppStrings {

    override val javaLocale: Locale = Locale.ITALY

    // =================================================================
    // COMUNE
    // =================================================================

    override val back = "Indietro"
    override val close = "Chiudi"
    override val cancel = "Annulla"
    override val accept = "Accetta"
    override val ok = "OK"
    override val done = "Fatto"
    override val delete = "Elimina"
    override val edit = "Modifica"
    override val save = "Salva"
    override val next = "Avanti"
    override val skip = "Salta"
    override val selected = "Selezionato"
    override val add = "Aggiungi"
    override val retry = "Tocca per riprovare"
    override val acceptShort = "Accetta"

    override val connectionErrorTitle = "Errore di connessione!"
    override val connectionErrorMessage = "Controlla la connessione e riprova più tardi."
    override val noActiveSessionMsg = "Nessuna sessione attiva"
    override val firestoreReadDenied =
        "Firestore ha negato la lettura di questi dati (sessione o regole di sicurezza)."

    override val successCd = "Successo"
    override val infoCd = "Informazioni"
    override val warningCd = "Avviso"
    override val errorCd = "Errore / Nessuna connessione"
    override val closeCd = "Chiudi"
    override val showPassword = "Mostra password"
    override val hidePassword = "Nascondi password"
    override val expandOptionsCd = "Espandi opzioni"

    override fun dayName(dayKey: String): String = when (dayKey) {
        "Lunes" -> "lunedì"
        "Martes" -> "martedì"
        "Miércoles" -> "mercoledì"
        "Jueves" -> "giovedì"
        "Viernes" -> "venerdì"
        "Sábado" -> "sabato"
        "Domingo" -> "domenica"
        else -> dayKey
    }

    override fun formatTime(hora24: String): String = hora24.trim()

    override fun freeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 ora libera" else "$horas ore libere"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min liberi"
        else -> "$minutos minuti liberi"
    }

    override fun formatMonthDay(date: LocalDate): String =
        DateTimeFormatter.ofPattern("d MMMM", javaLocale).format(date)

    override fun formatMonthAbbrev(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", javaLocale).format(date)

    // =================================================================
    // BENVENUTA
    // =================================================================

    override val welcomeTagline = "Trova qualcosa da fare tra le lezioni"
    override val signIn = "Accedi"
    override val createAccount = "Crea Account"
    override val accountDeletedTitle = "Account eliminato!"
    override val accountDeletedMsg = "Il tuo account e i tuoi dati sono stati eliminati correttamente."
    override val accountDeactivatedTitle = "Account disattivato!"
    override val accountDeactivatedMsg =
        "Puoi riattivarlo quando vuoi con gli stessi dati di accesso."

    // =================================================================
    // ACCESSO
    // =================================================================

    override val loginTitle = "Accedi con il tuo account di Gaply"
    override val usernameLabel = "Utente"
    override val passwordLabel = "Password"
    override val forgotPassword = "Hai dimenticato la password?"
    override val loginButton = "Entra"
    override val noAccountYet = "Non hai ancora un account? "
    override val registerLink = "Registrati"
    override val marketingOptOut =
        "Se non desideri ricevere comunicazioni di marketing sui nostri prodotti e servizi, spunta questa casella."
    override val userNotFoundTitle = "Account non trovato!"
    override val userNotFoundMsg = "Non esiste un account con quel nome utente. Controllalo o registrati."
    override val invalidEmailFormat = "Il formato dell'email non è valido"
    override val wrongPasswordMsg = "La password non è corretta"
    override val loginFailedMsg = "Impossibile accedere. Riprova."
    override val noAccountWithEmailMsg = "Non esiste un account con quella email"

    // =================================================================
    // ERRORI DI CAMPO
    // =================================================================

    override val fieldRequired = "Questo campo è obbligatorio"
    override val minChars3 = "Minimo 3 caratteri"
    override val minChars6 = "Minimo 6 caratteri"
    override val validEmailPrompt = "Inserisci un'email valida"
    override val passwordsMismatch = "Le password non coincidono"
    override val usernameTaken = "Quel nome utente è già in uso"
    override val emailTaken = "Quella email è già registrata"
    override val selectBirthdate = "Seleziona la tua data di nascita"
    override val selectUniversity = "Seleziona la tua università"

    override fun minInterestsWarning(max: Int) = "Puoi scegliere fino a $max interessi."

    // =================================================================
    // REGISTRAZIONE
    // =================================================================

    override fun stepOfTotal(step: Int, total: Int) = "Passo $step di $total"
    override val stepAccountData = "Dati dell'account"
    override val stepPersonalInfo = "Informazioni personali"
    override val stepPurpose = "Obiettivo"
    override val stepInterests = "Interessi"
    override val stepPhotoBio = "Foto e biografia"

    override val createYourAccountTitle = "Crea il tuo account"
    override val startWithLoginData = "Inizia con i tuoi dati di accesso."
    override val usernameAtHint = "Utente (@username)"
    override val emailLabel = "Email"
    override val confirmPasswordLabel = "Conferma password"

    override val personalInfoTitle = "Informazioni personali"
    override val tellUsAboutYou = "Raccontaci un po' di te."
    override val firstNameLabel = "Nome *"
    override val lastNameLabel = "Cognome"
    override val birthDateLabel = "Data di nascita *"
    override val dayLabel = "Giorno"
    override val monthLabel = "Mese"
    override val yearLabel = "Anno"
    override val whereStudyNow = "Dove studi attualmente?"
    override val universityLabel = "Università *"
    override val genderIdentityLabel = "Identità di genere"
    override val chooseYourGender = "Scegli il tuo genere"

    override val whatBringsGaply = "Cosa ti porta su Gaply?"
    override val chooseGoalHint = "Scegli l'obiettivo che ti descrive meglio. Potrai cambiarlo dopo."

    override val selectSomeInterests = "Seleziona alcuni dei tuoi interessi"
    override val addInterests = "Aggiungi interessi"
    override fun maxInterestsInTotal(max: Int, selected: Int, total: Int) =
        "Massimo $max interessi in totale ($selected/$total)."

    override val uploadPhoto = "Carica foto"
    override val takePhoto = "Scatta foto"
    override fun helloName(name: String) = "Ciao $name"
    override val giveShortBio = "Dacci una breve descrizione:"
    override val bioLabel = "Biografia"
    override val saveProfile = "Salva profilo"

    override val profileSavedTitle = "Il tuo profilo è stato salvato!"
    override val profileSavedMsg =
        "Il tuo profilo è stato salvato! Le informazioni del profilo sono state salvate correttamente."
    override val galleryInfoTitle = "Galleria foto"
    override val galleryInfoMsg = "Nella versione finale, questo pulsante aprirà la tua galleria di foto."
    override val cameraInfoTitle = "Fotocamera"
    override val cameraInfoMsg = "Nella versione finale, questo pulsante aprirà la fotocamera del tuo telefono."
    override val saveErrorTitle = "Errore durante il salvataggio!"
    override val saveErrorMsg = "Errore nel salvataggio del profilo! Scusa, riprova più tardi."

    override val months = listOf(
        "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno",
        "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre",
    )

    override val genders = listOf("Femminile", "Maschile", "Non binario", "Preferisco non dirlo")

    override val goals = listOf(
        "\uD83D\uDCDA Devo studiare",
        "\uD83C\uDFC0 Cosa si fa",
        "\uD83D\uDE09 Ho tempo libero",
        "\uD83E\uDD7A Non ho ancora le idee chiare",
    )

    override val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "Altro",
    )

    override fun interestLabel(key: String): String = when (key) {
        "Deportes" -> "Sport"
        "Música" -> "Musica"
        "Videojuegos" -> "Videogiochi"
        "Artes" -> "Arti"
        "Juegos de mesa" -> "Giochi da tavolo"
        "Películas" -> "Film"
        "Estudio" -> "Studio"
        "Eventos" -> "Eventi"
        "Baile" -> "Ballo"
        "Fotografía" -> "Fotografia"
        "Viajes" -> "Viaggi"
        "Cocina" -> "Cucina"
        "Programación" -> "Programmazione"
        "Moda" -> "Moda"
        "Ciencia" -> "Scienza"
        else -> key
    }

    // =================================================================
    // HOME
    // =================================================================

    override val tabDiscover = "Scopri"
    override val tabMatches = "Match"
    override val tabChats = "Chat"
    override val tabProfile = "Profilo"

    override val welcomeUser = "Benvenuto"
    override fun youHaveFreeSlot(descripcion: String) = "Hai $descripcion"
    override val addClassesToSeeSlots =
        "Aggiungi le tue lezioni in Il mio orario per vedere i tuoi spazi liberi"
    override val noFreeSlotsToday = "Oggi non hai spazi liberi"
    override val suggestionsToday = "Suggerimenti per oggi"
    override val createActivitySection = "Crea l'attività"
    override val addYourOwnActivity = "Aggiungi la tua attività"
    override val inviteOthers = "Invita altri a unirsi nel tuo spazio"
    override val culturalAgenda = "Agenda culturale"
    override val visitWebsite = "vai al sito"
    override val matchesEmptyMsg = "I tuoi match appariranno qui."
    override val discoverMatchesCta = "Trova qualcuno con lo stesso slot libero"
    override val chatsEmptyMsg = "Le tue conversazioni appariranno qui."
    override val eventSiteInfoMsg = "Nella versione finale, questo link aprirà il sito dell'evento."

    override val placeLibrary = "Biblioteca"
    override val placeCourt = "Campo M7A"
    override val placeMultiRoom = "Sala polifunzionale"
    override val placeGreenZone = "Zona verde"
    override val placeAuditorium = "Auditorio"

    override val profilePhotoCd = "Foto del profilo"
    override val notificationsCd = "Notifiche"
    override val addCd = "Aggiungi"

    // =================================================================
    // PROFILO
    // =================================================================

    override val profileFallbackUsername = "Utente"
    override val completeYourProfile = "Completa il tuo profilo"
    override val descriptionTitle = "Descrizione"
    override val noBioYet = "Non hai ancora aggiunto una descrizione."
    override val interestsTitle = "Interessi"
    override val chooseInterestsHint = "Scegli i tuoi interessi dal pulsante Modifica."
    override val scheduleSectionTitle = "Orario"
    override val addScheduleAction = "Aggiungi orario"
    override val addClassesHint = "Aggiungi le tue lezioni per scoprire i tuoi spazi liberi."
    override val upsTitle = "Oops!"
    override val settingsCd = "Impostazioni"
    override val changeProfilePhotoCd = "Cambia foto del profilo"
    override val accountUnknownError = "Non siamo riusciti a identificare il tuo account. Esci e accedi di nuovo."
    override val photoChangeError = "Non siamo riusciti a cambiare la tua foto. Riprova più tardi."

    // =================================================================
    // MODIFICA PROFILO
    // =================================================================

    override val editProfileTitle = "Modifica profilo"
    override val photoAndBasicsTitle = "Foto del profilo e dati base"
    override val photoBasicsSubtitle = "Questa è la foto che gli altri utenti vedranno nello swipe."
    override val profilePhotoPreviewCd = "Anteprima foto del profilo"
    override val currentPhotoCd = "Foto del profilo attuale"
    override val gallery = "Galleria"
    override val camera = "Fotocamera"
    override val namesLabel = "Nome"
    override val surnamesLabel = "Cognome"
    override val personalDetailsTitle = "Dati personali"
    override val selectAnOption = "Seleziona un'opzione"
    override val genderLabel = "Genere"
    override val interestsAndBioTitle = "Interessi e descrizione"
    override val interestsBioSubtitle = "Seleziona fino a 3 interessi (appariranno nel tuo profilo)"
    override val descriptionFieldLabel = "Descrizione"
    override val bioPlaceholder = "Raccontaci un po' di te..."
    override val saveChanges = "Salva modifiche"
    override val profilePreviewTitle = "Anteprima del profilo"
    override val noUniversity = "Senza università"
    override val noInterestsSelected = "Non hai ancora selezionato interessi."
    override val changesSavedTitle = "Modifiche salvate!"
    override val changesSavedMsg = "Il tuo profilo è stato aggiornato correttamente."
    override val editSaveErrorMsg = "Non abbiamo potuto salvare le modifiche. Riprova più tardi."

    // =================================================================
    // ORARIO
    // =================================================================

    override val mySchedule = "Il mio orario"
    override val prevDay = "Giorno precedente"
    override val nextDay = "Giorno successivo"
    override val addScheduleCd = "Aggiungi orario"
    override fun notePrefix(nota: String) = "Nota: $nota"
    override val importFromGallery = "Importa dalla galleria"
    override val importGalleryMsg =
        "Scegli una foto del tuo orario. Nella versione finale, Gaply leggerà automaticamente le lezioni dall'immagine."
    override val viewActivitiesForSlot = "Vedi le attività per questo spazio"
    override val addScheduleTitle = "Aggiungi orario"
    override val tapPlusToAdd = "Toca \"+\" per aggiungere l'orario"
    override val dayFreeUseArrows = "Questo giorno è libero. Usa ‹ › per vedere gli altri giorni."
    override val tapToRetry = "Tocca per riprovare"

    override val noConnectionSchedule = "Nessuna connessione. Controlla la tua internet e riprova."
    override val firestoreReadSchedule = "Firestore non ha potuto leggere il tuo orario (sessione o regole)."
    override val loadScheduleFailed = "Impossibile caricare l'orario. Riprova."
    override val loadGenericFailed = "Impossibile caricare. Riprova."
    override val saveGenericFailed = "Impossibile salvare. Riprova."
    override val deleteGenericFailed = "Impossibile eliminare. Riprova."
    override val firestoreWriteRules = "Firestore non ha potuto salvare (sessione o regole)."
    override val firestoreDeleteRules = "Firestore non ha potuto eliminare (sessione o regole)."

    // =================================================================
    // MODIFICA ORARIO
    // =================================================================

    override val deleteClassTitle = "Elimina lezione"
    override fun deleteClassMessage(nombre: String) =
        "Eliminare \"$nombre\" dall'orario? Questa azione non può essere annullata."
    override val thisClass = "questa lezione"
    override val scheduleFormTitle = "Orario"
    override val closeWithoutSaving = "Chiudi senza salvare"
    override val titleLabel = "Titolo"
    override val titlePlaceholder = "Titolo dell'insegnamento"
    override val startLabel = "Inizio"
    override val endLabel = "Fine"
    override val timeSectionLabel = "Ora"
    override val startsLabel = "Inizia"
    override val endsLabel = "Finisce"
    override val addNotesTitle = "Aggiungi note"
    override val notesPlaceholder = "Scrivi una nota (facoltativo)."
    override val colorLabel = "Colore"
    override val selectDay = "Seleziona un giorno"
    override val selectTime = "Seleziona un'ora"
    override val selectColor = "Seleziona un colore"
    override val classNoLongerExists = "Questa lezione non esiste più o è stata eliminata."
    override val writeTitleError = "Scrivi il titolo dell'insegnamento."
    override val endMustBeAfterStart = "L'ora finale deve essere successiva a quella iniziale."

    // =================================================================
    // SPAZI LIBERI
    // =================================================================

    override val myFreeSlots = "I miei spazi liberi"
    override val tellUsLikesFreeTime =
        "Dici cosa ti piace e quando sei libero. I suggerimenti si ordinano da soli."
    override val yourInterests = "I tuoi interessi"
    override fun selectedCount(selected: Int, max: Int) = "$selected di $max selezionati"
    override val yourFreeSlotTitle = "Il tuo spazio libero"
    override val fromLabel = "Da"
    override val toLabel = "A"
    override val startShort = "Inizio"
    override val endShort = "Fine"
    override val suggestionsTitle = "Suggerimenti"
    override val fixTimeToSeeSuggestions = "Correggi l'orario per vedere i suggerimenti."
    override val noneFitsSlot = "Nessuna attività entra in quello spazio. Prova con un altro giorno o orario."
    override fun doesNotFit(count: Int) = "Non entra ($count)"
    override val utilityExplanation =
        "Utilità = 65% interessi + 35% aderenza all'orario. Ciò che non entra nel tuo spazio viene scartato con il motivo."
    override fun yourSlotIs(rango: String) = "Il tuo spazio: $rango"

    override fun activityName(id: String, fallback: String): String = when (id) {
        "futbol" -> "Partita di calcio"
        "repaso" -> "Caffè e ripasso"
        "mesa" -> "Torneo di giochi da tavolo"
        "musica" -> "Sessione musicale"
        "baile" -> "Lezione di ballo"
        "artes" -> "Laboratorio d'arte"
        "cine" -> "Cinema in campus"
        "evento" -> "Evento culturale"
        else -> fallback
    }

    override fun activityDescription(id: String, fallback: String): String = when (id) {
        "futbol" -> "Campo del campus."
        "repaso" -> "Ripasso di gruppo al caffè."
        "mesa" -> "Sala attività."
        "musica" -> "Auditorio."
        "baile" -> "Sala polivalente."
        "artes" -> "Aula d'arte."
        "cine" -> "Auditorio."
        "evento" -> "Piazza principale."
        else -> fallback
    }

    override fun reasonInterestsMatch(compartidos: String) =
        "Corrisponde ai tuoi interessi: $compartidos"
    override val reasonNoInterestsMatch = "Non corrisponde ai tuoi interessi attuali"
    override val reasonNoInterestsRegistered = "Non hai ancora interessi registrati"
    override fun reasonFitsCompletely(hueco: String) = "Sta completo nel tuo spazio del $hueco"
    override fun reasonPartiallyUses(minutos: Int, hueco: String) =
        "Usa $minutos min del tuo spazio del $hueco"
    override fun reasonNoFreeSlot(franja: String) = "Non entra in nessuno spazio libero ($franja)"

    // =================================================================
    // IMPOSTAZIONI
    // =================================================================

    override val settingsTitle = "Impostazioni"
    override val accountSecurity = "Account e sicurezza"
    override val changePasswords = "Cambia password"
    override val updateYourAccess = "Aggiorna i tuoi dati di accesso"
    override val deactivateDeleteAccount = "Disattiva ed elimina account"
    override val deactivateDeleteAccountSubtitle = "Pausa temporanea o eliminazione permanente"
    override val twoFactor = "Autenticazione a due fattori"
    override val preferences = "Preferenze"
    override val theme = "Tema"
    override val enableDarkMode = "Attiva la modalità scura"
    override val themeSoonMessage = "Sarà nella versione finale, ci stiamo lavorando."
    override val notifications = "Notifiche"
    override val configureAlerts = "Configura le tue notifiche"
    override val language = "Lingua"
    override val saveLanguage = "Salva lingua"
    override val languageSoonMessage = "Sarà nella versione finale, ci stiamo lavorando."
    override val aboutSection = "Informazioni"
    override val rateTheApp = "Valuta l'app"
    override val aboutGaply = "Informazioni su Gaply"
    override val copyrightText = "© Gaply 2026 - Version 1.0"
    override val logout = "Esci"

    // =================================================================
    // CAMBIA PASSWORD
    // =================================================================

    override val changePasswordTitle = "Cambia password"
    override val currentPasswordLabel = "Password attuale"
    override val newPasswordLabel = "Nuova password"
    override val changePasswordButton = "Salva password"
    override val passwordUpdatedTitle = "Password aggiornata!"
    override val passwordUpdatedMsg = "La tua password è stata cambiata correttamente."
    override val changePasswordErrorTitle = "Impossibile cambiare"
    override val changePasswordReloginMsg =
        "Per sicurezza, esci e accedi di nuovo per cambiare la tua password"
    override val changePasswordErrorMsg = "Impossibile cambiare la password. Riprova."

    // =================================================================
    // ACCOUNT: DISATTIVA / ELIMINA
    // =================================================================

    override val deleteOrDeactivateTitle = "Eliminare o disattivare?"
    override val deleteOrDeactivateDesc =
        "Se vuoi lasciare Gaply temporaneamente, disattiva solo il tuo account. " +
            "Se scegli di eliminarlo, non potrai recuperarlo dopo 30 giorni."
    override val deactivateOptionTitle = "Disattiva account"
    override val deactivateOptionDesc = "Puoi riattivarlo quando vuoi con gli stessi dati di accesso."
    override val deleteOptionTitle = "Elimina account"
    override val deleteOptionDesc = "Questa azione elimina il tuo account e tutti i tuoi dati in modo permanente."
    override val confirmDeactivateTitle = "Disattivare questo account?"
    override val confirmDeactivateDesc =
        "Il tuo account resterà inattivo fino a quando non accederai di nuovo con gli stessi dati di accesso."
    override val confirmDeactivateButton = "Disattiva"
    override val confirmDeactivateListTitle = "Se disattivi l'account:"
    override val confirmDeactivateBulletPrivacy = "Nessuno potrà vedere il tuo account o il suo contenuto."
    override val confirmDeactivateBulletOutsideData =
        "I dati che non sono memorizzati nel tuo account (come i messaggi " +
            "diretti) potrebbero ancora essere visibili ad altri utenti."
    override val confirmDeactivateBulletDataKept =
        "Gaply conserverà i tuoi dati per poterli recuperare quando " +
            "riattivi il tuo account."
    override val confirmDeactivateBulletRecovery =
        "Potrai riattivare il tuo account e recuperare tutto il contenuto in " +
            "qualsiasi momento usando gli stessi dati di accesso."
    override val deactivateErrorTitle = "Impossibile disattivare"
    override val deactivateErrorMsg = "Riprova tra qualche minuto."
    override val deactivateReloginMsg =
        "Per sicurezza, esci e accedi di nuovo per disattivare il tuo account"
    override val deleteAccountTitle = "Elimina account"
    override val deleteAccountDesc =
        "Questa azione è permanente: verranno eliminati il tuo account, il tuo profilo, il tuo orario e i tuoi dati."
    override val deleteAccountButton = "Elimina definitivamente"
    override val deleteAccountErrorMsg = "Impossibile eliminare l'account. Riprova."
    override val deleteAccountConfirmTitle = "Eliminare questo account?"
    override val deleteAccountErrorTitle = "Impossibile eliminare"
    override val deleteAccountReloginMsg =
        "Per sicurezza, esci e accedi di nuovo per eliminare il tuo account"
    override val deleteWarningPermanent = "Eliminare il tuo account è permanente."
    override val deleteWarningProfileData = "Tutti i dati del tuo profilo verranno cancellati."
    override val deleteWarningActivity = "La tua cronologia attività e i contenuti salvati andranno persi."
    override val deleteWarningIrreversible = "Non puoi annullare questa azione."

    // =================================================================
    // REIMPOSTA PASSWORD
    // =================================================================

    override val resetPasswordHeader = "Recupera password"
    override val resetPasswordTitle = "Reimposta password"
    override val resetPasswordSubtitle = "Ti invieremo un link per creare una nuova password."
    override val sendResetLink = "Invia link"
    override val resetLinkSentTitle = "Link inviato!"
    override val resetLinkSentMsg = "Controlla la tua email per reimpostare la password."
    override val invalidEmailError = "Inserisci un'email valida"
    override val resetPasswordErrorTitle = "Impossibile inviare il link"
    override val resetPasswordErrorMsg = "Verifica l'email e riprova."

    // =================================================================
    // VALUTA / INFORMAZIONI
    // =================================================================

    override val rateAppTitle = "Valuta questa app"
    override val rateAppSubtitle = "Il tuo parere ci aiuta a migliorare"
    override val thanksTitle = "Grazie per il tuo parere!"
    override val thanksMsg = "La tua valutazione ci aiuta a migliorare Gaply."
    override fun thanksRatingMsg(stars: Int) =
        "Siamo felici che tu abbia valutato Gaply con $stars ${if (stars == 1) "stella" else "stelle"}."
    override val starRatingCd = "Valutazione con stelle"
    override fun starLabel(star: Int) = "Stella $star"
    override val rateCommentLabel = "Motivi (facoltativo):"
    override val rateCommentPlaceholder = "Raccontaci cosa ti è piaciuto o cosa\npossiamo migliorare..."
    override val rateSendButton = "Invia"
    override val aboutLogoCd = "Logo di Gaply"
    override val logoCd = "Logo di Gaply"
    override val welcomeIllustrationCd = "Illustrazione principale"
    override val aboutBody =
        "Gaply nasce con l'obiettivo di eliminare le barriere di comunicazione e aiutare gli " +
            "studenti a sfruttare meglio il proprio tempo tra le lezioni."

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    override val activitiesForYourSlot = "Vedi le attività per il tuo slot"
    override val activitiesSectionTitle = "Attività per il tuo slot"
    override val joinedActivitiesButton = "Vedi le mie attività attuali"
    override val joinedActivitiesTitle = "Le tue attività attuali"
    override val joinedActivitiesEmpty = "Non ti sei ancora iscritto a nessuna attività."
    override val myActivitiesCardTitle = "Le tue attività"
    override val myActivitiesCardSubtitle = "Modifica o annulla quelle che hai già pubblicato"
    override val activitiesListTitle = "Attività per il tuo slot"
    override val activitiesDetailTitle = "Attività"
    override val huecoBannerDemo = "Hai 2 ore libere 12:00 - 2:00 p.m."
    override val accordingToYourInterests = "In base ai tuoi interessi"
    override val activitiesEmptyListMsg =
        "Non ci sono ancora attività pubblicate per questo slot. Sii il primo a pubblicarne una!"
    override val likeCd = "Mi piace"
    override fun myActivitiesEmptyMsg(createWith: String) =
        "Non hai ancora pubblicato attività. Creane una con \"$createWith\" e apparirà qui."
    override val cancelActivityTitle = "Annulla attività"
    override val cancelActivityConfirmMsg = "Sei sicuro di voler annullare questa attività?"
    override val confirmCancelActivity = "Sì, annulla attività"
    override val activityUnavailableMsg = "Questa attività non è più disponibile."
    override val organizerLabel = "Organizzatore"
    override fun attendeesCount(count: Int) = "Partecipanti: $count"
    override val joinActivity = "Partecipa"
    override val alreadyJoined = "Sei già dentro"
    override val leaveActivityTitle = "Esci dall'attività"
    override val leaveActivityConfirmMsg =
        "Vuoi uscire da questa attività? Potrai rientrare quando vuoi."
    override val confirmLeaveActivity = "Sì, esci"
    override val comingSoonTitle = "In arrivo!"
    override val comingSoonMsg = "Ci stiamo lavorando e sarà aggiunto a breve"
    override val eventChat = "Chat dell'evento"
    override val locationLabel = "Posizione"
    override val howToGetThere = "Come arrivare"
    override val beFirstToJoin = "Sii il primo a partecipare"
    override val manageYourActivity = "Gestisci la tua attività"
    override val newActivityTitle = "Nuova attività"
    override val editActivityTitle = "Modifica attività"
    override val eventTitleLabel = "Titolo dell'evento"
    override val categoryLabel = "Categoria"
    override val activityPhotoLabel = "Foto del luogo dell'attività"
    override val locationPlaceLabel = "Posizione / Luogo"
    override val locationPlacePlaceholder = "es. Campo M7A, Aula 204, Biblioteca…"
    override val durationLabel = "Durata stimata"
    override val dateLabel = "Data"
    override val descriptionOptionalLabel = "Descrizione (facoltativa)"
    override val activityDescriptionPlaceholder = "Aggiungi altri dettagli sulla tua attività..."
    override val publishActivity = "Pubblica attività"
    override val selectTimeTitle = "Seleziona un orario"
    override val expandCategoriesCd = "Espandi categorie"
    override val placePhotoCd = "Foto del luogo"
    override val removePhotoCd = "Rimuovi foto"
    override val tapToAddPlacePhoto = "Tocca per aggiungere una foto del luogo"
    override val defaultColorPhotoHint = "Se non ne scegli nessuna, viene usato un colore verde predefinito"
    override val duration30 = "30 min"
    override val duration1h = "1 ora"
    override val duration2h = "2 ore"

    // =================================================================
    // MODULE 5 · MATCHES
    // =================================================================

    override val matchesNoMoreSuggestions = "Al momento non abbiamo altri suggerimenti"
    override val matchesUtilityExplanation =
        "Utilità = 60% interessi + 40% intervalli liberi in comune. " +
            "Compaino solo i profili con tempo libero condiviso."
    override val loadingData = "Caricamento…"

    override val swipeLike = "MI PIACE"
    override val swipePass = "PASSA"
    override val swipeSuper = "SUPER"
    override val superLikeCd = "Super like"

    override val matchTitle = "È un match!"
    override fun matchMessage(name: String) =
        "Tu e $name vi piacete! Potete chattare."
    override val openChat = "Apri chat"
    override val activityJoinedTitle = "Sei dentro l'attività!"
    override fun activityJoinedMsg(titulo: String) =
        "La chat di \"$titulo\" è disponibile con i partecipanti."

    override fun ageYears(age: Int) = "$age anni"
    override fun commonFreeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 ora in comune" else "$horas ore in comune"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min in comune"
        else -> "$minutos minuti in comune"
    }

    override val reasonNoCommonFreeSlot = "Non avete intervalli liberi in comune"
    override fun reasonSharedFreeSlot(rango: String) = "Intervallo in comune: $rango"

    // =================================================================
    // MODULE 5 · CHATS
    // =================================================================

    override val chatGroupTag = "Gruppo"
    override val chatNoMessages = "Ancora nessun messaggio. Invia il primo!"
    override val messageInputHint = "Scrivi un messaggio..."
    override val sendMessageCd = "Invia messaggio"
    override val chatActivityActive = "Attiva oggi"
    override fun chatActivityScheduled(fecha: String) = "Programmata: $fecha"

    override val timeJustNow = "adesso"
    override fun timeAgoMinutes(minutes: Int) = "$minutes min fa"
    override fun timeAgoHours(hours: Int) = "$hours h fa"
    override fun timeAgoDays(days: Int) = if (days <= 1) "ieri" else "$days giorni fa"

    override fun unreadMessages(count: Int) = "$count messaggi non letti"

    override val appTitle = "Gaply"
    override val searchChatsHint = "Cerca chat o persone"
    override val searchNoResults = "Nessuna chat corrisponde alla tua ricerca."

    override val chatOnlyForMembersTitle = "Prima unisciti all'attività"
    override val chatOnlyForMembersMsg =
        "Devi prima far parte dell'attività per entrare nella sua chat."
    override val deleteChatOption = "Elimina chat"
    override val pinChatOption = "Fissa chat"
    override val deleteChatConfirmTitle = "Eliminare questa chat?"
    override val deleteChatConfirmMsg =
        "Verrà rimossa dalla tua lista; i messaggi resteranno salvati in Gaply."
    override val pickChatHint = "Tocca la chat su cui agire."

    override fun systemMemberJoined(name: String) = "$name si è unito all'attività"
    override fun systemMemberLeft(name: String) = "$name ha lasciato l'attività"
}
