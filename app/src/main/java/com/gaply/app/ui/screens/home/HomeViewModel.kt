package com.gaply.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.User
import com.gaply.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: User? = null,
    val selectedTab: Int = 0,
    val dialog: HomeDialog? = null,
)

enum class HomeDialog { CREATE_ACTIVITY, EVENT_SITE }

class HomeViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(0)
    private val dialog = MutableStateFlow<HomeDialog?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        selectedTab,
        dialog,
    ) { user, tab, currentDialog ->
        HomeUiState(user = user, selectedTab = tab, dialog = currentDialog)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onSelectTab(index: Int) {
        selectedTab.value = index
    }

    fun onShowCreateActivityInfo() {
        dialog.value = HomeDialog.CREATE_ACTIVITY
    }

    fun onShowEventSiteInfo() {
        dialog.value = HomeDialog.EVENT_SITE
    }

    fun consumeDialog() {
        dialog.value = null
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(container.authRepository) as T
                }
            }
    }
}
