package com.safezone.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.EmergencyContact
import com.safezone.app.domain.models.UserProfile
import com.safezone.app.domain.repository.AuthRepository
import com.safezone.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val photoUrl: String? = null,
    val contactName1: String = "",
    val contactPhone1: String = "",
    val contactName2: String = "",
    val contactPhone2: String = "",
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val savedOnce: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val profile: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        val uid = auth.currentUserId.firstOrNull()
        if (uid == null) { _state.value = _state.value.copy(loading = false); return@launch }
        val r = profile.getProfile(uid)
        if (r is AppResult.Success) {
            val p = r.data
            _state.value = ProfileUiState(
                fullName = p.fullName,
                phone = p.phone,
                address = p.address,
                photoUrl = p.photoUrl,
                contactName1 = p.emergencyContacts.getOrNull(0)?.name.orEmpty(),
                contactPhone1 = p.emergencyContacts.getOrNull(0)?.phone.orEmpty(),
                contactName2 = p.emergencyContacts.getOrNull(1)?.name.orEmpty(),
                contactPhone2 = p.emergencyContacts.getOrNull(1)?.phone.orEmpty(),
                loading = false
            )
        } else {
            _state.value = _state.value.copy(loading = false)
        }
    }

    fun onName(v: String) { _state.value = _state.value.copy(fullName = v) }
    fun onPhone(v: String) { _state.value = _state.value.copy(phone = v) }
    fun onAddress(v: String) { _state.value = _state.value.copy(address = v) }
    fun onContact1Name(v: String) { _state.value = _state.value.copy(contactName1 = v) }
    fun onContact1Phone(v: String) { _state.value = _state.value.copy(contactPhone1 = v) }
    fun onContact2Name(v: String) { _state.value = _state.value.copy(contactName2 = v) }
    fun onContact2Phone(v: String) { _state.value = _state.value.copy(contactPhone2 = v) }

    fun uploadAvatar(bytes: ByteArray) = viewModelScope.launch {
        val uid = auth.currentUserId.firstOrNull() ?: return@launch
        val r = profile.uploadAvatar(uid, bytes)
        if (r is AppResult.Success) _state.value = _state.value.copy(photoUrl = r.data)
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val uid = auth.currentUserId.firstOrNull() ?: run {
            _state.value = _state.value.copy(error = "Not signed in"); return@launch
        }
        val s = _state.value
        if (s.contactName1.isBlank() || s.contactPhone1.isBlank() ||
            s.contactName2.isBlank() || s.contactPhone2.isBlank()
        ) {
            _state.value = s.copy(error = "Both emergency contacts required"); return@launch
        }
        _state.value = s.copy(saving = true, error = null)
        val p = UserProfile(
            id = uid,
            email = "",
            fullName = s.fullName,
            phone = s.phone,
            address = s.address,
            photoUrl = s.photoUrl,
            emergencyContacts = listOf(
                EmergencyContact(s.contactName1, s.contactPhone1),
                EmergencyContact(s.contactName2, s.contactPhone2)
            )
        )
        when (val r = profile.upsertProfile(p)) {
            is AppResult.Success -> {
                _state.value = _state.value.copy(saving = false, savedOnce = true)
                onDone()
            }
            is AppResult.Error -> _state.value = _state.value.copy(saving = false, error = r.message)
            else -> Unit
        }
    }
}
