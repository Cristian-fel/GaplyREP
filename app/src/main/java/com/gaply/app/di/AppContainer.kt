package com.gaply.app.di

import android.content.Context
import com.gaply.app.GaplyApp
import com.gaply.app.data.local.LocalUserStore
import com.gaply.app.data.repository.InMemoryAuthRepository
import com.gaply.app.data.repository.LocalUserRepository
import com.gaply.app.domain.repository.AuthRepository
import com.gaply.app.domain.repository.UserRepository

class AppContainer(context: Context) {

    private val userStore = LocalUserStore(context)

    val authRepository: AuthRepository = InMemoryAuthRepository(userStore)

    val userRepository: UserRepository = LocalUserRepository(userStore)
}

fun Context.appContainer(): AppContainer = (applicationContext as GaplyApp).container
