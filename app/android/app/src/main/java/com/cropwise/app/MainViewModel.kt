package com.cropwise.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cropwise.app.data.model.UserDto
import com.cropwise.app.data.repository.CropWiseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Holds global auth/profile/language state shared across all screens. */
class MainViewModel(val repo: CropWiseRepository) : ViewModel() {

    private val _loggedIn = MutableStateFlow(false)
    val loggedIn: StateFlow<Boolean> = _loggedIn

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    private val _profile = MutableStateFlow<UserDto?>(null)
    val profile: StateFlow<UserDto?> = _profile

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    init {
        viewModelScope.launch {
            repo.bootstrap()
            _loggedIn.value = repo.isLoggedIn
            if (repo.isLoggedIn) loadProfile()
            _ready.value = true
        }
    }

    fun loadProfile() = viewModelScope.launch {
        runCatching { repo.profile() }.onSuccess {
            _profile.value = it
            it.preferredLanguage?.let { l -> _language.value = l }
        }
    }

    fun onAuthSuccess(user: UserDto) {
        _profile.value = user
        user.preferredLanguage?.let { _language.value = it }
        _loggedIn.value = true
    }

    fun setLanguage(lang: String) = viewModelScope.launch {
        _language.value = lang
        repo.setLanguage(lang)
    }

    fun logout() = viewModelScope.launch {
        repo.logout()
        _profile.value = null
        _loggedIn.value = false
    }

    class Factory(private val repo: CropWiseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repo) as T
    }
}
