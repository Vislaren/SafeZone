package com.safezone.app.domain.usecases

import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.SosEvent
import com.safezone.app.domain.models.SosStatus
import com.safezone.app.domain.models.TriggerSource
import com.safezone.app.domain.repository.AuthRepository
import com.safezone.app.domain.repository.LocationRepository
import com.safezone.app.domain.repository.ProfileRepository
import com.safezone.app.domain.repository.SosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = auth.signIn(email, password)
}

class SignUpUseCase @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = auth.signUp(email, password)
}

class SignOutUseCase @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke() = auth.signOut()
}

class ObserveProfileUseCase @Inject constructor(private val profile: ProfileRepository) {
    operator fun invoke(userId: String) = profile.observeProfile(userId)
}

class SaveProfileUseCase @Inject constructor(private val profile: ProfileRepository) {
    suspend operator fun invoke(p: com.safezone.app.domain.models.UserProfile) = profile.upsertProfile(p)
}

/**
 * Main SOS trigger. Grabs location, creates event, returns handle for the service to continue.
 */
class TriggerSosUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val location: LocationRepository,
    private val sos: SosRepository
) {
    suspend operator fun invoke(source: TriggerSource = TriggerSource.MANUAL): AppResult<SosEvent> {
        val userId = auth.currentUserId.firstOrNull()
            ?: return AppResult.Error("Not signed in")
        val loc = location.currentLocation()
            ?: return AppResult.Error("Location unavailable")
        return sos.createEvent(userId, loc.lat, loc.lng, source)
    }
}

class EndSosUseCase @Inject constructor(private val sos: SosRepository) {
    suspend operator fun invoke(eventId: String, status: SosStatus = SosStatus.RESOLVED) =
        sos.endEvent(eventId, status)
}

class ObserveSosHistoryUseCase @Inject constructor(private val sos: SosRepository) {
    operator fun invoke(userId: String): Flow<List<SosEvent>> = sos.observeHistory(userId)
}

class GetNearbyUsersUseCase @Inject constructor(private val sos: SosRepository) {
    suspend operator fun invoke(lat: Double, lng: Double, radiusMeters: Int = 50) =
        sos.getNearbyUsers(lat, lng, radiusMeters)
}
