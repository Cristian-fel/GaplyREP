package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** English texts. */
object StringsEn : AppStrings {

    override val javaLocale: Locale = Locale.US

    // =================================================================
    // COMMON
    // =================================================================

    override val back = "Back"
    override val close = "Close"
    override val cancel = "Cancel"
    override val accept = "OK"
    override val ok = "OK"
    override val done = "Done"
    override val delete = "Delete"
    override val edit = "Edit"
    override val save = "Save"
    override val next = "Next"
    override val skip = "Skip"
    override val selected = "Selected"
    override val add = "Add"
    override val retry = "Tap to retry"
    override val acceptShort = "Accept"

    override val connectionErrorTitle = "Connection error!"
    override val connectionErrorMessage = "Check your connection and try again later."
    override val noActiveSessionMsg = "There is no active session"
    override val firestoreReadDenied =
        "Firestore denied reading this data (session or security rules)."

    override val successCd = "Success"
    override val infoCd = "Information"
    override val warningCd = "Warning"
    override val errorCd = "Error / Offline"
    override val closeCd = "Close"
    override val showPassword = "Show password"
    override val hidePassword = "Hide password"
    override val expandOptionsCd = "Expand options"

    override fun dayName(dayKey: String): String = when (dayKey) {
        "Lunes" -> "Monday"
        "Martes" -> "Tuesday"
        "Miércoles" -> "Wednesday"
        "Jueves" -> "Thursday"
        "Viernes" -> "Friday"
        "Sábado" -> "Saturday"
        "Domingo" -> "Sunday"
        else -> dayKey
    }

    override fun formatTime(hora24: String): String {
        val partes = hora24.trim().split(":")
        val horas = partes.getOrNull(0)?.toIntOrNull() ?: 0
        val minutos = partes.getOrNull(1)?.toIntOrNull() ?: 0
        val sufijo = if (horas < 12) "AM" else "PM"
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
            if (horas == 1) "1 free hour" else "$horas free hours"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min free"
        else -> "$minutos minutes free"
    }

    override fun formatMonthDay(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMMM d", javaLocale).format(date)

    override fun formatMonthAbbrev(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", javaLocale).format(date)

    // =================================================================
    // WELCOME
    // =================================================================

    override val welcomeTagline = "Find something to do between classes"
    override val signIn = "Sign In"
    override val createAccount = "Create Account"
    override val accountDeletedTitle = "Account deleted!"
    override val accountDeletedMsg = "Your account and data have been deleted successfully."
    override val accountDeactivatedTitle = "Account deactivated!"
    override val accountDeactivatedMsg =
        "You can reactivate it anytime with the same login details."

    // =================================================================
    // LOGIN
    // =================================================================

    override val loginTitle = "Sign In With Your Gaply Account"
    override val usernameLabel = "Username"
    override val passwordLabel = "Password"
    override val forgotPassword = "Forgot your password?"
    override val loginButton = "Log In"
    override val noAccountYet = "Don't have an account yet? "
    override val registerLink = "Sign up"
    override val marketingOptOut =
        "If you don't want to receive marketing communications about our products and services, check this box."
    override val userNotFoundTitle = "Account not found!"
    override val userNotFoundMsg = "No account exists with that username. Check it or sign up."
    override val invalidEmailFormat = "The email format is not valid"
    override val wrongPasswordMsg = "The password is incorrect"
    override val loginFailedMsg = "Could not sign in. Try again."
    override val noAccountWithEmailMsg = "No account exists with that email"

    // =================================================================
    // FIELD ERRORS
    // =================================================================

    override val fieldRequired = "This field is required"
    override val minChars3 = "Minimum 3 characters"
    override val minChars6 = "Minimum 6 characters"
    override val validEmailPrompt = "Enter a valid email"
    override val passwordsMismatch = "The passwords don't match"
    override val usernameTaken = "That username is already in use"
    override val emailTaken = "That email is already registered"
    override val selectBirthdate = "Select your birth date"
    override val selectUniversity = "Select your university"

    override fun minInterestsWarning(max: Int) = "You can choose up to $max interests."

    // =================================================================
    // REGISTRATION
    // =================================================================

    override fun stepOfTotal(step: Int, total: Int) = "Step $step of $total"
    override val stepAccountData = "Account details"
    override val stepPersonalInfo = "Personal information"
    override val stepPurpose = "Purpose"
    override val stepInterests = "Interests"
    override val stepPhotoBio = "Photo and bio"

    override val createYourAccountTitle = "Create your account"
    override val startWithLoginData = "Start with your login details."
    override val usernameAtHint = "Username (@username)"
    override val emailLabel = "Email"
    override val confirmPasswordLabel = "Confirm Password"

    override val personalInfoTitle = "Personal information"
    override val tellUsAboutYou = "Tell us a little about yourself."
    override val firstNameLabel = "First name *"
    override val lastNameLabel = "Last name"
    override val birthDateLabel = "Birth date *"
    override val dayLabel = "Day"
    override val monthLabel = "Month"
    override val yearLabel = "Year"
    override val whereStudyNow = "Where do you study now?"
    override val universityLabel = "University *"
    override val genderIdentityLabel = "Gender identity"
    override val chooseYourGender = "Choose your gender"

    override val whatBringsGaply = "What brings you to Gaply?"
    override val chooseGoalHint = "Choose the goal that best describes you. You can change it later."

    override val selectSomeInterests = "Select some of your interests"
    override val addInterests = "Add interests"
    override fun maxInterestsInTotal(max: Int, selected: Int, total: Int) =
        "Maximum $max interests in total ($selected/$total)."

    override val uploadPhoto = "Upload photo"
    override val takePhoto = "Take Photo"
    override fun helloName(name: String) = "Hello $name"
    override val giveShortBio = "Give us a short description:"
    override val bioLabel = "Bio"
    override val saveProfile = "Save profile"

    override val profileSavedTitle = "Your profile has been saved!"
    override val profileSavedMsg =
        "Your profile has been saved! Your profile information was saved successfully."
    override val galleryInfoTitle = "Photo gallery"
    override val galleryInfoMsg = "In the final version, this button will open your photo gallery."
    override val cameraInfoTitle = "Camera"
    override val cameraInfoMsg = "In the final version, this button will open your phone's camera."
    override val saveErrorTitle = "Error saving!"
    override val saveErrorMsg = "Error saving your profile! Sorry, try again later."

    override val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    override val genders = listOf("Female", "Male", "Non-binary", "Prefer not to say")

    override val goals = listOf(
        "\uD83D\uDCDA I need to study",
        "\uD83C\uDFC0 What's going on",
        "\uD83D\uDE09 I have free time",
        "\uD83E\uDD7A I'm still not sure",
    )

    override val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "Other",
    )

    override fun interestLabel(key: String): String = when (key) {
        "Deportes" -> "Sports"
        "Música" -> "Music"
        "Videojuegos" -> "Video games"
        "Artes" -> "Arts"
        "Juegos de mesa" -> "Board games"
        "Películas" -> "Movies"
        "Estudio" -> "Study"
        "Eventos" -> "Events"
        "Baile" -> "Dance"
        "Fotografía" -> "Photography"
        "Viajes" -> "Travel"
        "Cocina" -> "Cooking"
        "Programación" -> "Programming"
        "Moda" -> "Fashion"
        "Ciencia" -> "Science"
        else -> key
    }

    // =================================================================
    // HOME
    // =================================================================

    override val tabDiscover = "Discover"
    override val tabMatches = "Matches"
    override val tabChats = "Chats"
    override val tabProfile = "Profile"

    override val welcomeUser = "Welcome"
    override fun youHaveFreeSlot(descripcion: String) = "You have $descripcion"
    override val addClassesToSeeSlots =
        "Add your classes in My schedule to see your free slots"
    override val noFreeSlotsToday = "You have no free slots today"
    override val suggestionsToday = "Suggestions for today"
    override val createActivitySection = "Create the activity"
    override val addYourOwnActivity = "Add your own activity"
    override val inviteOthers = "Invite others to join in your slot"
    override val culturalAgenda = "Cultural agenda"
    override val visitWebsite = "go to site"
    override val matchesEmptyMsg = "Your matches will appear here."
    override val discoverMatchesCta = "Find someone with your same free time"
    override val chatsEmptyMsg = "Your conversations will appear here."
    override val eventSiteInfoMsg = "In the final version, this link will open the event's website."

    override val placeLibrary = "Library"
    override val placeCourt = "Court M7A"
    override val placeMultiRoom = "Multi-purpose Room"
    override val placeGreenZone = "Green Zone"
    override val placeAuditorium = "Auditorium"

    override val profilePhotoCd = "Profile photo"
    override val notificationsCd = "Notifications"
    override val addCd = "Add"

    // =================================================================
    // PROFILE
    // =================================================================

    override val profileFallbackUsername = "Username"
    override val completeYourProfile = "Complete your profile"
    override val descriptionTitle = "Description"
    override val noBioYet = "You haven't added a description yet."
    override val interestsTitle = "Interests"
    override val chooseInterestsHint = "Choose your interests from the Edit button."
    override val scheduleSectionTitle = "Schedule"
    override val addScheduleAction = "Add schedule"
    override val addClassesHint = "Add your classes to discover your free slots."
    override val upsTitle = "Oops!"
    override val settingsCd = "Settings"
    override val changeProfilePhotoCd = "Change profile photo"
    override val accountUnknownError = "We couldn't identify your account. Log out and log back in."
    override val photoChangeError = "We couldn't change your photo. Try again later."

    // =================================================================
    // EDIT PROFILE
    // =================================================================

    override val editProfileTitle = "Edit Profile"
    override val photoAndBasicsTitle = "Profile Photo and Basics"
    override val photoBasicsSubtitle = "This is the photo other users will see on the swipe."
    override val profilePhotoPreviewCd = "Profile photo preview"
    override val currentPhotoCd = "Current profile photo"
    override val gallery = "Gallery"
    override val camera = "Camera"
    override val namesLabel = "First names"
    override val surnamesLabel = "Last names"
    override val personalDetailsTitle = "Personal Details"
    override val selectAnOption = "Select an option"
    override val genderLabel = "Gender"
    override val interestsAndBioTitle = "Interests and bio"
    override val interestsBioSubtitle = "Select up to 3 interests (they appear on your profile)"
    override val descriptionFieldLabel = "Description"
    override val bioPlaceholder = "Tell us a little about yourself..."
    override val saveChanges = "Save changes"
    override val profilePreviewTitle = "Profile preview"
    override val noUniversity = "No university"
    override val noInterestsSelected = "You haven't selected any interests yet."
    override val changesSavedTitle = "Changes saved!"
    override val changesSavedMsg = "Your profile was updated successfully."
    override val editSaveErrorMsg = "We couldn't save the changes. Try again later."

    // =================================================================
    // SCHEDULE
    // =================================================================

    override val mySchedule = "My schedule"
    override val prevDay = "Previous day"
    override val nextDay = "Next day"
    override val addScheduleCd = "Add schedule"
    override fun notePrefix(nota: String) = "Note: $nota"
    override val importFromGallery = "Import from gallery"
    override val importGalleryMsg =
        "Choose a photo of your schedule. In the final version, Gaply will read your classes automatically from the image."
    override val viewActivitiesForSlot = "View activities for this slot"
    override val addScheduleTitle = "Add Schedule"
    override val tapPlusToAdd = "Tap \"+\" to add the schedule"
    override val dayFreeUseArrows = "This day is free. Use ‹ › to see the other days."
    override val tapToRetry = "Tap to retry"

    override val noConnectionSchedule = "No connection. Check your internet and try again."
    override val firestoreReadSchedule = "Firestore didn't allow reading your schedule (session or rules)."
    override val loadScheduleFailed = "Could not load the schedule. Try again."
    override val loadGenericFailed = "Could not load. Try again."
    override val saveGenericFailed = "Could not save. Try again."
    override val deleteGenericFailed = "Could not delete. Try again."
    override val firestoreWriteRules = "Firestore didn't allow saving (session or rules)."
    override val firestoreDeleteRules = "Firestore didn't allow deleting (session or rules)."

    // =================================================================
    // EDIT SCHEDULE
    // =================================================================

    override val deleteClassTitle = "Delete class"
    override fun deleteClassMessage(nombre: String) =
        "Remove \"$nombre\" from your schedule? This action can't be undone."
    override val thisClass = "this class"
    override val scheduleFormTitle = "Schedule"
    override val closeWithoutSaving = "Close without saving"
    override val titleLabel = "Title"
    override val titlePlaceholder = "Course title"
    override val startLabel = "Start"
    override val endLabel = "End"
    override val timeSectionLabel = "Time"
    override val startsLabel = "Starts"
    override val endsLabel = "Ends"
    override val addNotesTitle = "Add Notes"
    override val notesPlaceholder = "Write a note (optional)."
    override val colorLabel = "Color"
    override val selectDay = "Select a day"
    override val selectTime = "Select a time"
    override val selectColor = "Select a color"
    override val classNoLongerExists = "This class no longer exists or was deleted."
    override val writeTitleError = "Write the course title."
    override val endMustBeAfterStart = "The end time must be after the start time."

    // =================================================================
    // FREE SLOTS
    // =================================================================

    override val myFreeSlots = "My free slots"
    override val tellUsLikesFreeTime =
        "Tell us what you like and when you're free. The suggestions sort themselves."
    override val yourInterests = "Your interests"
    override fun selectedCount(selected: Int, max: Int) = "$selected of $max selected"
    override val yourFreeSlotTitle = "Your free slot"
    override val fromLabel = "From"
    override val toLabel = "To"
    override val startShort = "Start"
    override val endShort = "End"
    override val suggestionsTitle = "Suggestions"
    override val fixTimeToSeeSuggestions = "Fix the time to see the suggestions."
    override val noneFitsSlot = "No activity fits in that slot. Try another day or time."
    override fun doesNotFit(count: Int) = "Doesn't fit ($count)"
    override val utilityExplanation =
        "Utility = 65% interests + 35% schedule fit. What doesn't fit in your slot is discarded with its reason."
    override fun yourSlotIs(rango: String) = "Your slot: $rango"

    override fun activityName(id: String, fallback: String): String = when (id) {
        "futbol" -> "Football match"
        "repaso" -> "Coffee and study"
        "mesa" -> "Board game tournament"
        "musica" -> "Music session"
        "baile" -> "Dance class"
        "artes" -> "Arts workshop"
        "cine" -> "Campus cinema"
        "evento" -> "Cultural event"
        else -> fallback
    }

    override fun activityDescription(id: String, fallback: String): String = when (id) {
        "futbol" -> "Campus court."
        "repaso" -> "Group study in the cafeteria."
        "mesa" -> "Activity room."
        "musica" -> "Auditorium."
        "baile" -> "Multi-purpose floor."
        "artes" -> "Art room."
        "cine" -> "Auditorium."
        "evento" -> "Main square."
        else -> fallback
    }

    override fun reasonInterestsMatch(compartidos: String) =
        "Matches your interests: $compartidos"
    override val reasonNoInterestsMatch = "Doesn't match your current interests"
    override val reasonNoInterestsRegistered = "You don't have any registered interests yet"
    override fun reasonFitsCompletely(hueco: String) = "Fits completely in your slot on $hueco"
    override fun reasonPartiallyUses(minutos: Int, hueco: String) =
        "Uses $minutos min of your slot on $hueco"
    override fun reasonNoFreeSlot(franja: String) = "Doesn't fit in any free slot ($franja)"

    // =================================================================
    // SETTINGS
    // =================================================================

    override val settingsTitle = "Settings"
    override val accountSecurity = "Account and Security"
    override val changePasswords = "Change password"
    override val updateYourAccess = "Update your access"
    override val deactivateDeleteAccount = "Deactivate and Delete Account"
    override val deactivateDeleteAccountSubtitle = "Temporary pause or permanent deletion"
    override val twoFactor = "Two-factor authentication"
    override val preferences = "Preferences"
    override val theme = "Theme"
    override val enableDarkMode = "Turn on dark mode"
    override val themeSoonMessage = "It will be in the final version, we're working on it."
    override val notifications = "Notifications"
    override val configureAlerts = "Configure your alerts"
    override val language = "Language"
    override val saveLanguage = "Save language"
    override val languageSoonMessage = "It will be in the final version, we're working on it."
    override val aboutSection = "About"
    override val rateTheApp = "Rate the app"
    override val aboutGaply = "About Gaply"
    override val copyrightText = "© Gaply 2026 - Version 1.0"
    override val logout = "Log out"

    // =================================================================
    // CHANGE PASSWORD
    // =================================================================

    override val changePasswordTitle = "Change password"
    override val currentPasswordLabel = "Current password"
    override val newPasswordLabel = "New Password"
    override val changePasswordButton = "Save password"
    override val passwordUpdatedTitle = "Password updated!"
    override val passwordUpdatedMsg = "Your password was changed successfully."
    override val changePasswordErrorTitle = "Could not change"
    override val changePasswordReloginMsg =
        "For security, log out and log back in to change your password"
    override val changePasswordErrorMsg = "Could not change the password. Try again."

    // =================================================================
    // ACCOUNT: DEACTIVATE / DELETE
    // =================================================================

    override val deleteOrDeactivateTitle = "Delete or deactivate?"
    override val deleteOrDeactivateDesc =
        "If you want to leave Gaply temporarily, just deactivate your account. " +
            "If you choose to delete it, you won't be able to recover it after 30 days."
    override val deactivateOptionTitle = "Deactivate account"
    override val deactivateOptionDesc = "You can reactivate it anytime with the same login details."
    override val deleteOptionTitle = "Delete account"
    override val deleteOptionDesc = "This action permanently erases your account and all your data."
    override val confirmDeactivateTitle = "Deactivate this account?"
    override val confirmDeactivateDesc =
        "Your account will stay inactive until you log back in with the same login details."
    override val confirmDeactivateButton = "Deactivate"
    override val confirmDeactivateListTitle = "If you deactivate your account:"
    override val confirmDeactivateBulletPrivacy = "No one will be able to see your account or its content."
    override val confirmDeactivateBulletOutsideData =
        "Data that isn't stored on your account (like direct messages) " +
            "might still be visible to other users."
    override val confirmDeactivateBulletDataKept =
        "Gaply will keep your data so you can recover it when you " +
            "reactivate your account."
    override val confirmDeactivateBulletRecovery =
        "You'll be able to reactivate your account and recover all the content at " +
            "any time using the same login details."
    override val deactivateErrorTitle = "Could not deactivate"
    override val deactivateErrorMsg = "Try again in a few minutes."
    override val deactivateReloginMsg =
        "For security, log out and log back in to deactivate your account"
    override val deleteAccountTitle = "Delete account"
    override val deleteAccountDesc =
        "This action is permanent: your account, profile, schedule and data will be deleted."
    override val deleteAccountButton = "Delete permanently"
    override val deleteAccountErrorMsg = "Could not delete the account. Try again."
    override val deleteAccountConfirmTitle = "Delete this account?"
    override val deleteAccountErrorTitle = "Could not delete"
    override val deleteAccountReloginMsg =
        "For security, log out and log back in to delete your account"
    override val deleteWarningPermanent = "Deleting your account is permanent."
    override val deleteWarningProfileData = "All your profile data will be erased."
    override val deleteWarningActivity = "Your activity history and saved content will be lost."
    override val deleteWarningIrreversible = "You can't undo this action."

    // =================================================================
    // RESET PASSWORD
    // =================================================================

    override val resetPasswordHeader = "Recover password"
    override val resetPasswordTitle = "Reset password"
    override val resetPasswordSubtitle = "We'll send you a link to create a new password."
    override val sendResetLink = "Send link"
    override val resetLinkSentTitle = "Link sent!"
    override val resetLinkSentMsg = "Check your email to reset your password."
    override val invalidEmailError = "Enter a valid email"
    override val resetPasswordErrorTitle = "Could not send the link"
    override val resetPasswordErrorMsg = "Check the email and try again."

    // =================================================================
    // RATE / ABOUT
    // =================================================================

    override val rateAppTitle = "Rate this app"
    override val rateAppSubtitle = "Your opinion helps us improve"
    override val thanksTitle = "Thanks for your feedback!"
    override val thanksMsg = "Your rating helps us improve Gaply."
    override fun thanksRatingMsg(stars: Int) =
        "We're glad you rated Gaply $stars ${if (stars == 1) "star" else "stars"}."
    override val starRatingCd = "Star rating"
    override fun starLabel(star: Int) = "Star $star"
    override val rateCommentLabel = "Reasons (optional):"
    override val rateCommentPlaceholder = "Tell us what you liked or what\nwe can improve..."
    override val rateSendButton = "Send"
    override val aboutLogoCd = "Gaply logo"
    override val logoCd = "Gaply logo"
    override val welcomeIllustrationCd = "Main illustration"
    override val aboutBody =
        "Gaply was created to remove communication barriers and help " +
            "students make the most of their time between classes."

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    override val activitiesForYourSlot = "See activities for your slot"
    override val activitiesSectionTitle = "Activities for your slot"
    override val joinedActivitiesButton = "See my current activities"
    override val joinedActivitiesTitle = "Your current activities"
    override val joinedActivitiesEmpty = "You haven't joined any activity yet."
    override val myActivitiesCardTitle = "Your activities"
    override val myActivitiesCardSubtitle = "Edit or cancel the ones you've already published"
    override val activitiesListTitle = "Activities for your slot"
    override val activitiesDetailTitle = "Activities"
    override val huecoBannerDemo = "You have 2 free hours 12:00 - 2:00 p.m."
    override val accordingToYourInterests = "Based on your interests"
    override val activitiesEmptyListMsg =
        "There are no published activities for this slot yet. Be the first to publish one!"
    override val likeCd = "Like"
    override fun myActivitiesEmptyMsg(createWith: String) =
        "You haven't published activities yet. Create one with \"$createWith\" and it will appear here."
    override val cancelActivityTitle = "Cancel activity"
    override val cancelActivityConfirmMsg = "Are you sure you want to cancel this activity?"
    override val confirmCancelActivity = "Yes, cancel activity"
    override val activityUnavailableMsg = "This activity is no longer available."
    override val organizerLabel = "Organizer"
    override fun attendeesCount(count: Int) = "Attendees: $count"
    override val joinActivity = "Join"
    override val alreadyJoined = "You're already in"
    override val leaveActivityTitle = "Leave activity"
    override val leaveActivityConfirmMsg =
        "Do you want to leave this activity? You can join again whenever you want."
    override val confirmLeaveActivity = "Yes, leave"
    override val comingSoonTitle = "Coming soon!"
    override val comingSoonMsg = "We're working on it and it will be added soon"
    override val eventChat = "Event chat"
    override val locationLabel = "Location"
    override val howToGetThere = "How to get there"
    override val beFirstToJoin = "Be the first to join"
    override val manageYourActivity = "Manage your activity"
    override val newActivityTitle = "New activity"
    override val editActivityTitle = "Edit activity"
    override val eventTitleLabel = "Event title"
    override val categoryLabel = "Category"
    override val activityPhotoLabel = "Photo of the activity place"
    override val locationPlaceLabel = "Location / Place"
    override val locationPlacePlaceholder = "e.g. Court M7A, Room 204, Library…"
    override val durationLabel = "Estimated duration"
    override val dateLabel = "Date"
    override val descriptionOptionalLabel = "Description (optional)"
    override val activityDescriptionPlaceholder = "Add more details about your activity..."
    override val publishActivity = "Publish activity"
    override val selectTimeTitle = "Select a time"
    override val expandCategoriesCd = "Expand categories"
    override val placePhotoCd = "Place photo"
    override val removePhotoCd = "Remove photo"
    override val tapToAddPlacePhoto = "Tap to add a photo of the place"
    override val defaultColorPhotoHint = "If you don't choose one, a default green color is used"
    override val duration30 = "30 min"
    override val duration1h = "1 hour"
    override val duration2h = "2 hours"

    // =================================================================
    // MODULE 5 · MATCHES
    // =================================================================

    override val matchesNoMoreSuggestions = "We have no more suggestions for now"
    override val matchesUtilityExplanation =
        "Utility = 60% interests + 40% shared free slots. " +
            "Only profiles with shared free time appear."
    override val loadingData = "Loading…"

    override val swipeLike = "LIKE"
    override val swipePass = "PASS"
    override val swipeSuper = "SUPER"
    override val superLikeCd = "Super like"

    override val matchTitle = "It's a match!"
    override fun matchMessage(name: String) =
        "You and $name like each other! You can chat now."
    override val openChat = "Open chat"
    override val activityJoinedTitle = "You're in the activity!"
    override fun activityJoinedMsg(titulo: String) =
        "The chat for \"$titulo\" is ready with its attendees."

    override fun ageYears(age: Int) = "$age years old"
    override fun commonFreeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 hour in common" else "$horas hours in common"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min in common"
        else -> "$minutos minutes in common"
    }

    override val reasonNoCommonFreeSlot = "You have no free slots in common"
    override fun reasonSharedFreeSlot(rango: String) = "Shared free slot: $rango"

    // =================================================================
    // MODULE 5 · CHATS
    // =================================================================

    override val chatGroupTag = "Group"
    override val chatNoMessages = "No messages yet. Send the first one!"
    override val messageInputHint = "Type a message..."
    override val sendMessageCd = "Send message"
    override val chatActivityActive = "Active today"
    override fun chatActivityScheduled(fecha: String) = "Scheduled: $fecha"

    override val timeJustNow = "now"
    override fun timeAgoMinutes(minutes: Int) = "${minutes}m ago"
    override fun timeAgoHours(hours: Int) = "${hours}h ago"
    override fun timeAgoDays(days: Int) = if (days <= 1) "yesterday" else "${days}d ago"

    override fun unreadMessages(count: Int) = "$count unread messages"

    override val appTitle = "Gaply"
    override val searchChatsHint = "Search chats or people"
    override val searchNoResults = "No chats match your search."

    override val chatOnlyForMembersTitle = "Join the activity first"
    override val chatOnlyForMembersMsg =
        "You have to be part of the activity first to enter its chat."
    override val deleteChatOption = "Delete chat"
    override val pinChatOption = "Pin chat"
    override val deleteChatConfirmTitle = "Delete this chat?"
    override val deleteChatConfirmMsg =
        "It will be removed from your list; the messages will stay saved in Gaply."
    override val pickChatHint = "Tap the chat you want to act on."

    override fun systemMemberJoined(name: String) = "$name joined the activity"
    override fun systemMemberLeft(name: String) = "$name left the activity"
}
