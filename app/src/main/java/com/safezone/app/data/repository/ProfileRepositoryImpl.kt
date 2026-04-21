package com.safezone.app.data.repository

import com.safezone.app.data.remote.supabase.SupabaseClientProvider
import com.safezone.app.data.remote.supabase.SupabaseSchema
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.UserProfile
import com.safezone.app.domain.repository.ProfileRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider
) : ProfileRepository {

    override fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        emit(runCatching { fetchProfile(userId) }.getOrNull())
        // In production you'd use Realtime channel here. Scaffold keeps a single read for simplicity.
    }

    private suspend fun fetchProfile(userId: String): UserProfile? =
        supabase.client.postgrest[SupabaseSchema.T_PROFILES]
            .select {
                filter { eq("id", userId) }
                limit(1)
            }
            .decodeSingleOrNull<UserProfile>()

    override suspend fun getProfile(userId: String): AppResult<UserProfile> = runCatching {
        val p = fetchProfile(userId) ?: error("Profile not found")
        AppResult.Success(p)
    }.getOrElse { AppResult.Error(it.message ?: "Profile fetch failed", it) }

    override suspend fun upsertProfile(profile: UserProfile): AppResult<Unit> = runCatching {
        supabase.client.postgrest[SupabaseSchema.T_PROFILES].upsert(profile)
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "Save failed", it) }

    override suspend fun uploadAvatar(userId: String, bytes: ByteArray): AppResult<String> = runCatching {
        val path = "$userId/avatar.jpg"
        supabase.client.storage.from(SupabaseSchema.BUCKET_AVATARS).upload(path, bytes) {
            upsert = true
        }
        val publicUrl = supabase.client.storage
            .from(SupabaseSchema.BUCKET_AVATARS)
            .publicUrl(path)
        AppResult.Success(publicUrl)
    }.getOrElse { AppResult.Error(it.message ?: "Upload failed", it) }

    override suspend fun updateFcmToken(userId: String, token: String): AppResult<Unit> = runCatching {
        supabase.client.postgrest[SupabaseSchema.T_PROFILES].update(
            mapOf("fcm_token" to token)
        ) { filter { eq("id", userId) } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "FCM update failed", it) }
}
