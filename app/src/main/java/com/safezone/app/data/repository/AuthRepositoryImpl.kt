package com.safezone.app.data.repository

import com.safezone.app.data.remote.supabase.SupabaseClientProvider
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider
) : AuthRepository {

    override val currentUserId: Flow<String?> =
        supabase.client.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.id
                else -> null
            }
        }

    override suspend fun signUp(email: String, password: String): AppResult<String> = runCatching {
        supabase.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val uid = supabase.client.auth.currentUserOrNull()?.id
            ?: error("Sign-up succeeded but no user returned")
        AppResult.Success(uid)
    }.getOrElse { AppResult.Error(it.message ?: "Sign-up failed", it) }

    override suspend fun signIn(email: String, password: String): AppResult<String> = runCatching {
        supabase.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val uid = supabase.client.auth.currentUserOrNull()?.id
            ?: error("Sign-in succeeded but no user returned")
        AppResult.Success(uid)
    }.getOrElse { AppResult.Error(it.message ?: "Sign-in failed", it) }

    override suspend fun signOut(): AppResult<Unit> = runCatching {
        supabase.client.auth.signOut()
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "Sign-out failed", it) }

    override suspend fun refreshSession(): AppResult<Unit> = runCatching {
        supabase.client.auth.currentSessionOrNull()?.let {
            supabase.client.auth.refreshCurrentSession()
        }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "Refresh failed", it) }
}
