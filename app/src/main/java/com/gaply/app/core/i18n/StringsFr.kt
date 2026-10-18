package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Textes en français. */
object StringsFr : AppStrings {

    override val javaLocale: Locale = Locale.FRANCE

    // =================================================================
    // COMMUN
    // =================================================================

    override val back = "Retour"
    override val close = "Fermer"
    override val cancel = "Annuler"
    override val accept = "OK"
    override val ok = "OK"
    override val done = "Terminé"
    override val delete = "Supprimer"
    override val edit = "Modifier"
    override val save = "Enregistrer"
    override val next = "Suivant"
    override val skip = "Passer"
    override val selected = "Sélectionné"
    override val add = "Ajouter"
    override val retry = "Touchez pour réessayer"
    override val acceptShort = "Accepter"

    override val connectionErrorTitle = "Erreur de connexion !"
    override val connectionErrorMessage = "Vérifiez votre connexion et réessayez plus tard."
    override val noActiveSessionMsg = "Aucune session active"

    override val successCd = "Succès"
    override val infoCd = "Information"
    override val warningCd = "Avertissement"
    override val errorCd = "Erreur / Hors ligne"
    override val closeCd = "Fermer"
    override val showPassword = "Afficher le mot de passe"
    override val hidePassword = "Masquer le mot de passe"
    override val expandOptionsCd = "Déployer les options"

    override fun dayName(dayKey: String): String = when (dayKey) {
        "Lunes" -> "lundi"
        "Martes" -> "mardi"
        "Miércoles" -> "mercredi"
        "Jueves" -> "jeudi"
        "Viernes" -> "vendredi"
        "Sábado" -> "samedi"
        "Domingo" -> "dimanche"
        else -> dayKey
    }

    override fun formatTime(hora24: String): String = hora24.trim()

    override fun freeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 heure libre" else "$horas heures libres"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min libres"
        else -> "$minutos minutes libres"
    }

    override fun formatMonthDay(date: LocalDate): String =
        DateTimeFormatter.ofPattern("d MMMM", javaLocale).format(date)

    override fun formatMonthAbbrev(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", javaLocale).format(date)

    // =================================================================
    // BIENVENUE
    // =================================================================

    override val welcomeTagline = "Trouvez quelque chose à faire entre les cours"
    override val signIn = "Se connecter"
    override val createAccount = "Créer un compte"
    override val accountDeletedTitle = "Compte supprimé !"
    override val accountDeletedMsg = "Votre compte et vos données ont été supprimés avec succès."
    override val accountDeactivatedTitle = "Compte désactivé !"
    override val accountDeactivatedMsg =
        "Vous pouvez le réactiver à tout moment avec les mêmes identifiants."

    // =================================================================
    // CONNEXION
    // =================================================================

    override val loginTitle = "Connectez-vous avec votre compte Gaply"
    override val usernameLabel = "Utilisateur"
    override val passwordLabel = "Mot de passe"
    override val forgotPassword = "Mot de passe oublié ?"
    override val loginButton = "Connexion"
    override val noAccountYet = "Pas encore de compte ? "
    override val registerLink = "Inscrivez-vous"
    override val marketingOptOut =
        "Si vous ne souhaitez pas recevoir de communications marketing sur nos produits et services, cochez cette case."
    override val userNotFoundTitle = "Compte introuvable !"
    override val userNotFoundMsg = "Aucun compte n'existe avec cet identifiant. Vérifiez ou inscrivez-vous."
    override val invalidEmailFormat = "Le format de l'e-mail n'est pas valide"
    override val wrongPasswordMsg = "Le mot de passe est incorrect"
    override val loginFailedMsg = "Impossible de se connecter. Réessayez."
    override val noAccountWithEmailMsg = "Aucun compte n'existe avec cet e-mail"

    // =================================================================
    // ERREURS DE CHAMP
    // =================================================================

    override val fieldRequired = "Ce champ est obligatoire"
    override val minChars3 = "Minimum 3 caractères"
    override val minChars6 = "Minimum 6 caractères"
    override val validEmailPrompt = "Saisissez un e-mail valide"
    override val passwordsMismatch = "Les mots de passe ne correspondent pas"
    override val usernameTaken = "Ce nom d'utilisateur est déjà pris"
    override val emailTaken = "Cet e-mail est déjà enregistré"
    override val selectBirthdate = "Sélectionnez votre date de naissance"
    override val selectUniversity = "Sélectionnez votre université"

    override fun minInterestsWarning(max: Int) = "Vous pouvez choisir jusqu'à $max intérêts."

    // =================================================================
    // INSCRIPTION
    // =================================================================

    override fun stepOfTotal(step: Int, total: Int) = "Étape $step sur $total"
    override val stepAccountData = "Données du compte"
    override val stepPersonalInfo = "Informations personnelles"
    override val stepPurpose = "Objectif"
    override val stepInterests = "Intérêts"
    override val stepPhotoBio = "Photo et bio"

    override val createYourAccountTitle = "Créez votre compte"
    override val startWithLoginData = "Commencez par vos identifiants."
    override val usernameAtHint = "Utilisateur (@username)"
    override val emailLabel = "E-mail"
    override val confirmPasswordLabel = "Confirmer le mot de passe"

    override val personalInfoTitle = "Informations personnelles"
    override val tellUsAboutYou = "Parlez-nous un peu de vous."
    override val firstNameLabel = "Prénom *"
    override val lastNameLabel = "Nom"
    override val birthDateLabel = "Date de naissance *"
    override val dayLabel = "Jour"
    override val monthLabel = "Mois"
    override val yearLabel = "Année"
    override val whereStudyNow = "Où étudiez-vous actuellement ?"
    override val universityLabel = "Université *"
    override val genderIdentityLabel = "Identité de genre"
    override val chooseYourGender = "Choisissez votre genre"

    override val whatBringsGaply = "Qu'est-ce qui vous amène sur Gaply ?"
    override val chooseGoalHint = "Choisissez l'objectif qui vous décrit le mieux. Vous pourrez le changer plus tard."

    override val selectSomeInterests = "Sélectionnez quelques-uns de vos intérêts"
    override val addInterests = "Ajouter des intérêts"
    override fun maxInterestsInTotal(max: Int, selected: Int, total: Int) =
        "Maximum $max intérêts au total ($selected/$total)."

    override val uploadPhoto = "Charger une photo"
    override val takePhoto = "Prendre une photo"
    override fun helloName(name: String) = "Bonjour $name"
    override val giveShortBio = "Donnez-nous une brève description :"
    override val bioLabel = "Bio"
    override val saveProfile = "Enregistrer le profil"

    override val profileSavedTitle = "Votre profil a été enregistré !"
    override val profileSavedMsg =
        "Votre profil a été enregistré ! Les informations de votre profil ont été enregistrées avec succès."
    override val galleryInfoTitle = "Galerie de photos"
    override val galleryInfoMsg = "Dans la version finale, ce bouton ouvrira votre galerie de photos."
    override val cameraInfoTitle = "Appareil photo"
    override val cameraInfoMsg = "Dans la version finale, ce bouton ouvrira l'appareil photo de votre téléphone."
    override val saveErrorTitle = "Erreur lors de l'enregistrement !"
    override val saveErrorMsg = "Erreur lors de l'enregistrement du profil ! Désolé, réessayez plus tard."

    override val months = listOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre",
    )

    override val genders = listOf("Féminin", "Masculin", "Non-binaire", "Je préfère ne pas le dire")

    override val goals = listOf(
        "\uD83D\uDCDA J'ai besoin d'étudier",
        "\uD83C\uDFC0 Quoi de neuf",
        "\uD83D\uDE09 J'ai du temps libre",
        "\uD83E\uDD7A Je ne suis pas encore sûr(e)",
    )

    override val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "Autre",
    )

    override fun interestLabel(key: String): String = when (key) {
        "Deportes" -> "Sports"
        "Música" -> "Musique"
        "Videojuegos" -> "Jeux vidéo"
        "Artes" -> "Arts"
        "Juegos de mesa" -> "Jeux de société"
        "Películas" -> "Films"
        "Estudio" -> "Études"
        "Eventos" -> "Événements"
        "Baile" -> "Danse"
        "Fotografía" -> "Photographie"
        "Viajes" -> "Voyages"
        "Cocina" -> "Cuisine"
        "Programación" -> "Programmation"
        "Moda" -> "Mode"
        "Ciencia" -> "Science"
        else -> key
    }

    // =================================================================
    // ACCUEIL
    // =================================================================

    override val tabDiscover = "Découvrir"
    override val tabMatches = "Matches"
    override val tabChats = "Discussions"
    override val tabProfile = "Profil"

    override val welcomeUser = "Bienvenue"
    override fun youHaveFreeSlot(descripcion: String) = "Vous avez $descripcion"
    override val addClassesToSeeSlots =
        "Ajoutez vos cours dans Mon horaire pour voir vos créneaux libres"
    override val noFreeSlotsToday = "Vous n'avez aucun créneau libre aujourd'hui"
    override val suggestionsToday = "Suggestions pour aujourd'hui"
    override val createActivitySection = "Créer l'activité"
    override val addYourOwnActivity = "Ajouter votre propre activité"
    override val inviteOthers = "Invitez d'autres personnes à rejoindre votre créneau"
    override val culturalAgenda = "Agenda culturel"
    override val visitWebsite = "aller au site"
    override val matchesEmptyMsg = "Vos matches apparaîtront ici."
    override val chatsEmptyMsg = "Vos conversations apparaîtront ici."
    override val eventSiteInfoMsg = "Dans la version finale, ce lien ouvrira le site de l'événement."

    override val placeLibrary = "Bibliothèque"
    override val placeCourt = "Terrain M7A"
    override val placeMultiRoom = "Salle polyvalente"
    override val placeGreenZone = "Zone verte"
    override val placeAuditorium = "Auditorium"

    override val profilePhotoCd = "Photo de profil"
    override val notificationsCd = "Notifications"
    override val addCd = "Ajouter"

    // =================================================================
    // PROFIL
    // =================================================================

    override val profileFallbackUsername = "Utilisateur"
    override val completeYourProfile = "Complétez votre profil"
    override val descriptionTitle = "Description"
    override val noBioYet = "Vous n'avez pas encore ajouté de description."
    override val interestsTitle = "Intérêts"
    override val chooseInterestsHint = "Choisissez vos intérêts depuis le bouton Modifier."
    override val scheduleSectionTitle = "Horaire"
    override val addScheduleAction = "Ajouter un horaire"
    override val addClassesHint = "Ajoutez vos cours pour découvrir vos créneaux libres."
    override val upsTitle = "Oups !"
    override val settingsCd = "Paramètres"
    override val changeProfilePhotoCd = "Changer la photo de profil"
    override val accountUnknownError = "Nous n'avons pas pu identifier votre compte. Déconnectez-vous puis reconnectez-vous."
    override val photoChangeError = "Nous n'avons pas pu changer votre photo. Réessayez plus tard."

    // =================================================================
    // MODIFIER LE PROFIL
    // =================================================================

    override val editProfileTitle = "Modifier le profil"
    override val photoAndBasicsTitle = "Photo de profil et bases"
    override val photoBasicsSubtitle = "C'est la photo que les autres verront lors du swipe."
    override val profilePhotoPreviewCd = "Aperçu de la photo de profil"
    override val currentPhotoCd = "Photo de profil actuelle"
    override val gallery = "Galerie"
    override val camera = "Appareil photo"
    override val namesLabel = "Prénoms"
    override val surnamesLabel = "Noms"
    override val personalDetailsTitle = "Détails personnels"
    override val selectAnOption = "Sélectionnez une option"
    override val genderLabel = "Genre"
    override val interestsAndBioTitle = "Intérêts et bio"
    override val interestsBioSubtitle = "Sélectionnez jusqu'à 3 intérêts (ils apparaissent sur votre profil)"
    override val descriptionFieldLabel = "Description"
    override val bioPlaceholder = "Parlez-nous un peu de vous..."
    override val saveChanges = "Enregistrer les modifications"
    override val profilePreviewTitle = "Aperçu du profil"
    override val noUniversity = "Sans université"
    override val noInterestsSelected = "Vous n'avez pas encore sélectionné d'intérêts."
    override val changesSavedTitle = "Modifications enregistrées !"
    override val changesSavedMsg = "Votre profil a été mis à jour avec succès."
    override val editSaveErrorMsg = "Nous n'avons pas pu enregistrer les modifications. Réessayez plus tard."

    // =================================================================
    // HORAIRE
    // =================================================================

    override val mySchedule = "Mon horaire"
    override val prevDay = "Jour précédent"
    override val nextDay = "Jour suivant"
    override val addScheduleCd = "Ajouter un horaire"
    override fun notePrefix(nota: String) = "Note : $nota"
    override val importFromGallery = "Importer depuis la galerie"
    override val importGalleryMsg =
        "Choisissez une photo de votre horaire. Dans la version finale, Gaply lira vos cours automatiquement depuis l'image."
    override val viewActivitiesForSlot = "Voir les activités pour ce créneau"
    override val addScheduleTitle = "Ajouter un horaire"
    override val tapPlusToAdd = "Touchez « + » pour ajouter l'horaire"
    override val dayFreeUseArrows = "Cette journée est libre. Utilisez ‹ › pour voir les autres jours."
    override val tapToRetry = "Touchez pour réessayer"

    override val noConnectionSchedule = "Pas de connexion. Vérifiez votre internet et réessayez."
    override val firestoreReadSchedule = "Firestore n'a pas autorisé la lecture de votre horaire (session ou règles)."
    override val loadScheduleFailed = "Impossible de charger l'horaire. Réessayez."
    override val loadGenericFailed = "Impossible de charger. Réessayez."
    override val saveGenericFailed = "Impossible d'enregistrer. Réessayez."
    override val deleteGenericFailed = "Impossible de supprimer. Réessayez."
    override val firestoreWriteRules = "Firestore n'a pas autorisé l'enregistrement (session ou règles)."
    override val firestoreDeleteRules = "Firestore n'a pas autorisé la suppression (session ou règles)."

    // =================================================================
    // MODIFIER L'HORAIRE
    // =================================================================

    override val deleteClassTitle = "Supprimer le cours"
    override fun deleteClassMessage(nombre: String) =
        "Supprimer « $nombre » de l'horaire ? Cette action est irréversible."
    override val thisClass = "ce cours"
    override val scheduleFormTitle = "Horaire"
    override val closeWithoutSaving = "Fermer sans enregistrer"
    override val titleLabel = "Titre"
    override val titlePlaceholder = "Titre du cours"
    override val startLabel = "Début"
    override val endLabel = "Fin"
    override val timeSectionLabel = "Heure"
    override val startsLabel = "Commence"
    override val endsLabel = "Se termine"
    override val addNotesTitle = "Ajouter des notes"
    override val notesPlaceholder = "Écrivez une note (facultatif)."
    override val colorLabel = "Couleur"
    override val selectDay = "Sélectionnez un jour"
    override val selectTime = "Sélectionnez une heure"
    override val selectColor = "Sélectionnez une couleur"
    override val classNoLongerExists = "Ce cours n'existe plus ou a été supprimé."
    override val writeTitleError = "Écrivez le titre du cours."
    override val endMustBeAfterStart = "L'heure de fin doit être après l'heure de début."

    // =================================================================
    // CRÉNEAUX LIBRES
    // =================================================================

    override val myFreeSlots = "Mes créneaux"
    override val tellUsLikesFreeTime =
        "Dites-nous ce que vous aimez et quand vous êtes libre. Les suggestions se trient toutes seules."
    override val yourInterests = "Vos intérêts"
    override fun selectedCount(selected: Int, max: Int) = "$selected sur $max sélectionnés"
    override val yourFreeSlotTitle = "Votre créneau libre"
    override val fromLabel = "De"
    override val toLabel = "À"
    override val startShort = "Début"
    override val endShort = "Fin"
    override val suggestionsTitle = "Suggestions"
    override val fixTimeToSeeSuggestions = "Corrigez l'horaire pour voir les suggestions."
    override val noneFitsSlot = "Aucune activité ne rentre dans ce créneau. Essayez un autre jour ou une autre heure."
    override fun doesNotFit(count: Int) = "Ne rentre pas ($count)"
    override val utilityExplanation =
        "Utilité = 65 % intérêts + 35 % compatibilité d'horaire. Ce qui ne rentre pas dans votre créneau est écarté avec son motif."
    override fun yourSlotIs(rango: String) = "Votre créneau : $rango"

    override fun activityName(id: String, fallback: String): String = when (id) {
        "futbol" -> "Match de foot"
        "repaso" -> "Café et révision"
        "mesa" -> "Tournoi de jeux de société"
        "musica" -> "Session musique"
        "baile" -> "Cours de danse"
        "artes" -> "Atelier d'arts"
        "cine" -> "Cinéma sur le campus"
        "evento" -> "Événement culturel"
        else -> fallback
    }

    override fun activityDescription(id: String, fallback: String): String = when (id) {
        "futbol" -> "Terrain du campus."
        "repaso" -> "Révision de groupe à la cafétéria."
        "mesa" -> "Salle d'activités."
        "musica" -> "Auditorium."
        "baile" -> "Piste multi-usages."
        "artes" -> "Salle d'arts."
        "cine" -> "Auditorium."
        "evento" -> "Place principale."
        else -> fallback
    }

    override fun reasonInterestsMatch(compartidos: String) =
        "Correspond à vos intérêts : $compartidos"
    override val reasonNoInterestsMatch = "Ne correspond pas à vos intérêts actuels"
    override val reasonNoInterestsRegistered = "Vous n'avez pas encore d'intérêts enregistrés"
    override fun reasonFitsCompletely(hueco: String) = "Tient complètement dans votre créneau du $hueco"
    override fun reasonPartiallyUses(minutos: Int, hueco: String) =
        "Utilise $minutos min de votre créneau du $hueco"
    override fun reasonNoFreeSlot(franja: String) = "Ne rentre dans aucun créneau libre ($franja)"

    // =================================================================
    // PARAMÈTRES
    // =================================================================

    override val settingsTitle = "Paramètres"
    override val accountSecurity = "Compte et sécurité"
    override val changePasswords = "Changer le mot de passe"
    override val updateYourAccess = "Mettez à jour votre accès"
    override val deactivateDeleteAccount = "Désactiver et supprimer le compte"
    override val deactivateDeleteAccountSubtitle = "Pause temporaire ou suppression permanente"
    override val twoFactor = "Authentification à deux facteurs"
    override val preferences = "Préférences"
    override val theme = "Thème"
    override val enableDarkMode = "Activez le mode sombre"
    override val themeSoonMessage = "Il sera dans la version finale, nous y travaillons."
    override val notifications = "Notifications"
    override val configureAlerts = "Configurez vos alertes"
    override val language = "Langue"
    override val saveLanguage = "Enregistrer la langue"
    override val aboutSection = "À propos"
    override val rateTheApp = "Noter l'application"
    override val aboutGaply = "À propos de Gaply"
    override val copyrightText = "© Gaply 2026 - Version 1.0"
    override val logout = "Se déconnecter"

    // =================================================================
    // CHANGER LE MOT DE PASSE
    // =================================================================

    override val changePasswordTitle = "Changer le mot de passe"
    override val currentPasswordLabel = "Mot de passe actuel"
    override val newPasswordLabel = "Nouveau mot de passe"
    override val changePasswordButton = "Enregistrer le mot de passe"
    override val passwordUpdatedTitle = "Mot de passe mis à jour !"
    override val passwordUpdatedMsg = "Votre mot de passe a été changé avec succès."
    override val changePasswordErrorTitle = "Impossible de changer"
    override val changePasswordReloginMsg =
        "Par sécurité, déconnectez-vous puis reconnectez-vous pour changer votre mot de passe"
    override val changePasswordErrorMsg = "Impossible de changer le mot de passe. Réessayez."

    // =================================================================
    // COMPTE : DÉSACTIVER / SUPPRIMER
    // =================================================================

    override val deleteOrDeactivateTitle = "Supprimer ou désactiver ?"
    override val deleteOrDeactivateDesc =
        "Si vous souhaitez temporairement quitter Gaply, désactivez simplement votre compte. " +
            "Si vous choisissez de le supprimer, vous ne pourrez pas le récupérer après 30 jours."
    override val deactivateOptionTitle = "Désactiver le compte"
    override val deactivateOptionDesc = "Vous pouvez le réactiver à tout moment avec les mêmes identifiants."
    override val deleteOptionTitle = "Supprimer le compte"
    override val deleteOptionDesc = "Cette action efface définitivement votre compte et toutes vos données."
    override val confirmDeactivateTitle = "Désactiver ce compte ?"
    override val confirmDeactivateDesc =
        "Votre compte restera inactif jusqu'à ce que vous vous reconnectiez avec les mêmes identifiants."
    override val confirmDeactivateButton = "Désactiver"
    override val confirmDeactivateListTitle = "Si vous désactivez le compte :"
    override val confirmDeactivateBulletPrivacy = "Personne ne pourra voir votre compte ni son contenu."
    override val confirmDeactivateBulletOutsideData =
        "Les données qui ne sont pas stockées sur votre compte (comme les messages " +
            "privés) pourraient encore être visibles par d'autres utilisateurs."
    override val confirmDeactivateBulletDataKept =
        "Gaply conservera vos données pour que vous puissiez les récupérer lorsque " +
            "vous réactiverez votre compte."
    override val confirmDeactivateBulletRecovery =
        "Vous pourrez réactiver votre compte et récupérer tout le contenu à " +
            "tout moment avec les mêmes identifiants."
    override val deactivateErrorTitle = "Impossible de désactiver"
    override val deactivateErrorMsg = "Réessayez dans quelques minutes."
    override val deactivateReloginMsg =
        "Par sécurité, déconnectez-vous puis reconnectez-vous pour désactiver votre compte"
    override val deleteAccountTitle = "Supprimer le compte"
    override val deleteAccountDesc =
        "Cette action est définitive : votre compte, votre profil, votre horaire et vos données seront supprimés."
    override val deleteAccountButton = "Supprimer définitivement"
    override val deleteAccountErrorMsg = "Impossible de supprimer le compte. Réessayez."
    override val deleteAccountConfirmTitle = "Supprimer ce compte ?"
    override val deleteAccountErrorTitle = "Impossible de supprimer"
    override val deleteAccountReloginMsg =
        "Par sécurité, déconnectez-vous puis reconnectez-vous pour supprimer votre compte"
    override val deleteWarningPermanent = "La suppression de votre compte est définitive."
    override val deleteWarningProfileData = "Toutes les données de votre profil seront effacées."
    override val deleteWarningActivity = "Votre historique d'activité et votre contenu enregistré seront perdus."
    override val deleteWarningIrreversible = "Vous ne pouvez pas annuler cette action."

    // =================================================================
    // RÉINITIALISER LE MOT DE PASSE
    // =================================================================

    override val resetPasswordHeader = "Récupérer le mot de passe"
    override val resetPasswordTitle = "Réinitialiser le mot de passe"
    override val resetPasswordSubtitle = "Nous vous enverrons un lien pour créer un nouveau mot de passe."
    override val sendResetLink = "Envoyer le lien"
    override val resetLinkSentTitle = "Lien envoyé !"
    override val resetLinkSentMsg = "Vérifiez votre e-mail pour réinitialiser votre mot de passe."
    override val invalidEmailError = "Saisissez un e-mail valide"
    override val resetPasswordErrorTitle = "Impossible d'envoyer le lien"
    override val resetPasswordErrorMsg = "Vérifiez l'e-mail et réessayez."

    // =================================================================
    // ÉVALUER / À PROPOS
    // =================================================================

    override val rateAppTitle = "Évaluez cette application"
    override val rateAppSubtitle = "Votre avis nous aide à améliorer"
    override val thanksTitle = "Merci pour votre avis !"
    override val thanksMsg = "Votre note nous aide à améliorer Gaply."
    override fun thanksRatingMsg(stars: Int) =
        "Nous sommes ravis que vous ayez noté Gaply $stars ${if (stars == 1) "étoile" else "étoiles"}."
    override val starRatingCd = "Note par étoiles"
    override fun starLabel(star: Int) = "Étoile $star"
    override val rateCommentLabel = "Motifs (facultatif) :"
    override val rateCommentPlaceholder = "Dites-nous ce que vous avez aimé ou ce\nque nous pouvons améliorer..."
    override val rateSendButton = "Envoyer"
    override val aboutLogoCd = "Logo de Gaply"
    override val logoCd = "Logo de Gaply"
    override val welcomeIllustrationCd = "Illustration principale"
    override val aboutBody =
        "Gaply est née pour éliminer les barrières de communication et aider les " +
            "étudiants à mieux profiter de leur temps entre les cours."

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    override val activitiesForYourSlot = "Voir les activités pour votre créneau"
    override val myActivitiesCardTitle = "Vos activités"
    override val myActivitiesCardSubtitle = "Modifiez ou annulez celles que vous avez déjà publiées"
    override val activitiesListTitle = "Activités pour votre créneau"
    override val activitiesDetailTitle = "Activités"
    override val huecoBannerDemo = "Vous avez 2 heures libres 12:00 - 2:00 p.m."
    override val accordingToYourInterests = "Selon vos centres d'intérêt"
    override val activitiesEmptyListMsg =
        "Aucune activité publiée pour ce créneau pour le moment. Soyez le premier à en publier une !"
    override val likeCd = "J'aime"
    override fun myActivitiesEmptyMsg(createWith: String) =
        "Vous n'avez pas encore publié d'activités. Créez-en une avec « $createWith » et elle apparaîtra ici."
    override val cancelActivityTitle = "Annuler l'activité"
    override val cancelActivityConfirmMsg = "Voulez-vous vraiment annuler cette activité ?"
    override val confirmCancelActivity = "Oui, annuler l'activité"
    override val activityUnavailableMsg = "Cette activité n'est plus disponible."
    override val organizerLabel = "Organisateur"
    override fun attendeesCount(count: Int) = "Participants : $count"
    override val joinActivity = "Rejoindre"
    override val alreadyJoined = "Vous y êtes déjà"
    override val comingSoonTitle = "Bientôt disponible !"
    override val comingSoonMsg = "Nous y travaillons et cela sera ajouté prochainement"
    override val eventChat = "Chat de l'événement"
    override val locationLabel = "Lieu"
    override val howToGetThere = "Comment s'y rendre"
    override val beFirstToJoin = "Soyez le premier à rejoindre"
    override val manageYourActivity = "Gérez votre activité"
    override val newActivityTitle = "Nouvelle activité"
    override val editActivityTitle = "Modifier l'activité"
    override val eventTitleLabel = "Titre de l'événement"
    override val categoryLabel = "Catégorie"
    override val activityPhotoLabel = "Photo du lieu de l'activité"
    override val locationPlaceLabel = "Lieu / Emplacement"
    override val locationPlacePlaceholder = "ex. Terrain M7A, Salle 204, Bibliothèque…"
    override val durationLabel = "Durée estimée"
    override val dateLabel = "Date"
    override val descriptionOptionalLabel = "Description (facultatif)"
    override val activityDescriptionPlaceholder = "Ajoutez plus de détails sur votre activité..."
    override val publishActivity = "Publier l'activité"
    override val selectTimeTitle = "Sélectionnez une heure"
    override val expandCategoriesCd = "Déployer les catégories"
    override val placePhotoCd = "Photo du lieu"
    override val removePhotoCd = "Retirer la photo"
    override val tapToAddPlacePhoto = "Touchez pour ajouter une photo du lieu"
    override val defaultColorPhotoHint = "Si vous n'en choisissez aucune, une couleur verte par défaut est utilisée"
    override val duration30 = "30 min"
    override val duration1h = "1 heure"
    override val duration2h = "2 heures"
}
