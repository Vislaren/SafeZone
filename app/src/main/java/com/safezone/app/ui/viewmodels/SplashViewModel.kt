package com.safezone.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safezone.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashDestination { LOADING, LOGIN, HOME }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: AuthRepository
) : ViewModel() {

    private val _dest = MutableStateFlow(SplashDestination.LOADING)
    val dest: StateFlow<SplashDestination> = _dest.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1200) // let the branded splash sit for a moment
            val uid = auth.currentUserId.firstOrNull()
            _dest.value = if (uid != null) SplashDestination.HOME else SplashDestination.LOGIN
        }
    }
}
