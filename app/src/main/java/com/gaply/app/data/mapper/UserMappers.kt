package com.gaply.app.data.mapper

import com.gaply.app.domain.model.User

fun User.toFirestoreMap(): HashMap<String, Any> = hashMapOf(
    "userId" to userId,
    "username" to username,
    "email" to email,
    "passwordHash" to passwordHash,
    "firstName" to firstName,
    "lastName" to lastName,
    "birthDate" to birthDate,
    "university" to university,
    "gender" to gender,
    "goal" to goal,
    "interests" to interests,
    "bio" to bio,
    "profilePictureUrl" to profilePictureUrl,
    "receiveMarketing" to receiveMarketing,
)

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toUser(defaultUserId: String = ""): User = User(
    userId = this["userId"] as? String ?: defaultUserId,
    username = this["username"] as? String ?: "",
    email = this["email"] as? String ?: "",
    passwordHash = this["passwordHash"] as? String ?: "",
    firstName = this["firstName"] as? String ?: "",
    lastName = this["lastName"] as? String ?: "",
    birthDate = this["birthDate"] as? String ?: "",
    university = this["university"] as? String ?: "",
    gender = this["gender"] as? String ?: "",
    goal = this["goal"] as? String ?: "",
    interests = (this["interests"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    bio = this["bio"] as? String ?: "",
    profilePictureUrl = this["profilePictureUrl"] as? String ?: "",
    receiveMarketing = this["receiveMarketing"] as? Boolean ?: false,
)
