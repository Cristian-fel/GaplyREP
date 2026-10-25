package com.gaply.app.ui

import com.gaply.app.core.i18n.AppStrings
import com.gaply.app.domain.model.RepoError

/**
 * Mensaje de error legible de los repositorios, siempre vía i18n (nunca cadenas
 * hardcodeadas en las pantallas). Lo usan Matches, Chats y el detalle de la
 * actividad cuando Firestore responde con permisos o sin conexión.
 *
 * [RepoError.PERMISSION] no es "sin sesión" en el mazo: la mayoría de las veces
 * Firestore rechazó la lectura porque las reglas de seguridad no están
 * publicadas (ver `firestore.rules` en la raíz del repo).
 */
fun mensajeDe(error: RepoError, s: AppStrings): String = when (error) {
    RepoError.CONNECTION -> s.connectionErrorMessage
    RepoError.PERMISSION -> s.firestoreReadDenied
    RepoError.EMPTY_FIELDS -> s.fieldRequired
    RepoError.WRONG_CREDENTIALS -> s.wrongPasswordMsg
    RepoError.USER_NOT_FOUND -> s.userNotFoundMsg
    RepoError.EMAIL_NOT_FOUND -> s.noAccountWithEmailMsg
    RepoError.USERNAME_TAKEN -> s.usernameTaken
    RepoError.EMAIL_TAKEN -> s.emailTaken
    RepoError.WEAK_PASSWORD -> s.minChars6
    else -> s.loadGenericFailed
}
