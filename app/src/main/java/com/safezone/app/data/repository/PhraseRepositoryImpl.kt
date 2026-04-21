package com.safezone.app.data.repository

import com.safezone.app.data.remote.supabase.SupabaseClientProvider
import com.safezone.app.data.remote.supabase.SupabaseSchema
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.SecurityPhrase
import com.safezone.app.domain.repository.PhraseRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhraseRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider
) : PhraseRepository {

    override fun observePhrases(userId: String): Flow<List<SecurityPhrase>> = flow {
        val rows = runCatching {
            supabase.client.postgrest[SupabaseSchema.T_PHRASES]
                .select { filter { eq("user_id", userId) } }
                .decodeList<SecurityPhrase>()
        }.getOrDefault(emptyList())
        emit(rows)
    }

    override suspend fun savePhrase(phrase: SecurityPhrase): AppResult<Unit> = runCatching {
        supabase.client.postgrest[SupabaseSchema.T_PHRASES].upsert(phrase)
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "Save failed", it) }

    override suspend fun deletePhrase(id: String): AppResult<Unit> = runCatching {
        supabase.client.postgrest[SupabaseSchema.T_PHRASES].delete {
            filter { eq("id", id) }
        }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "Delete failed", it) }

    override suspend fun uploadVoiceSample(phraseId: String, bytes: ByteArray): AppResult<String> =
        runCatching {
            val path = "$phraseId.wav"
            supabase.client.storage.from(SupabaseSchema.BUCKET_VOICE_PHRASES)
                .upload(path, bytes) { upsert = true }
            AppResult.Success(
                supabase.client.storage.from(SupabaseSchema.BUCKET_VOICE_PHRASES).publicUrl(path)
            )
        }.getOrElse { AppResult.Error(it.message ?: "Upload failed", it) }
}
