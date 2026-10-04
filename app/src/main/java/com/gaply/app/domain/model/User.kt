package com.gaply.app.domain.model

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val university: String = "",
    val gender: String = "",
    val goal: String = "",
    val interests: List<String> = emptyList(),
    val bio: String = "",
    val profilePictureUrl: String = "",
    val receiveMarketing: Boolean = false,
)
