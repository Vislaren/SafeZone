package com.safezone.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.usecases.SignInUseCase
import com.safezone.app.domain.usecases.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val agreed: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signIn: SignInUseCase,
    private val signUp: SignUpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEmail(v: String) { _state.value = _state.value.copy(email = v, error = null) }
    fun onPassword(v: String) { _state.value = _state.value.copy(password = v, error = null) }
    fun onConfirm(v: String) { _state.value = _state.value.copy(confirm = v, error = null) }
    fun onAgreedToggle() { _state.value = _state.value.copy(agreed = !_state.value.agreed) }

    fun login() = viewModelScope.launch {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Enter credentials"); return@launch
        }
        _state.value = s.copy(loading = true, error = null)
        when (val r = signIn(s.email.trim(), s.password)) {
            is AppResult.Success -> _state.value = _state.value.copy(loading = false, success = true)
            is AppResult.Error -> _state.value = _state.value.copy(loading = false, error = r.message)
            else -> Unit
        }
    }

    fun register() = viewModelScope.launch {
        val s = _state.value
        when {
            s.email.isBlank() -> { _state.value = s.copy(error = "Email required"); return@launch }
            s.password.length < 8 -> { _state.value = s.copy(error = "Password must be 8+ chars"); return@launch }
            s.password != s.confirm -> { _state.value = s.copy(error = "Passwords don't match"); return@launch }
            !s.agreed -> { _state.value = s.copy(error = "You must agree to the Terms"); return@launch }
        }
        _state.value = s.copy(loading = true, error = null)
        when (val r = signUp(s.email.trim(), s.password)) {
            is AppResult.Success -> _state.value = _state.value.copy(loading = false, success = true)
            is AppResult.Error -> _state.value = _state.value.copy(loading = false, error = r.message)
            else -> Unit
        }
    }

    fun clearSuccess() { _state.value = _state.value.copy(success = false) }
}
