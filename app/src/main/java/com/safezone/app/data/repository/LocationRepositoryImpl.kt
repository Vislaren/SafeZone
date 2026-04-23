package com.safezone.app.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.safezone.app.data.remote.supabase.SupabaseClientProvider
import com.safezone.app.data.remote.supabase.SupabaseSchema
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.repository.LocationPoint
import com.safezone.app.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabase: SupabaseClientProvider
) : LocationRepository {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }

    private fun hasPermission(): Boolean {
        val fine = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val coarse = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<LocationPoint> = callbackFlow {
        if (!hasPermission()) {
            close()
            return@callbackFlow
        }
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateDistanceMeters(5f)
            .setMinUpdateIntervalMillis(5_000L)
            .build()
        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(LocationPoint(it.latitude, it.longitude, it.accuracy, it.time))
                }
            }
        }
        fused.requestLocationUpdates(req, cb, context.mainLooper)
        awaitClose { fused.removeLocationUpdates(cb) }
    }

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): LocationPoint? {
        if (!hasPermission()) return null
        return suspendCancellableCoroutine { cont ->
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    cont.resume(
                        loc?.let { LocationPoint(it.latitude, it.longitude, it.accuracy, it.time) }
                    )
                }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    override suspend fun publishLocation(userId: String, lat: Double, lng: Double): AppResult<Unit> =
        runCatching {
            supabase.client.postgrest[SupabaseSchema.T_LOCATIONS].upsert(
                mapOf(
                    "user_id" to userId,
                    "lat" to lat,
                    "lng" to lng,
                    "updated_at" to java.time.Instant.now().toString()
                )
            )
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error(it.message ?: "Publish failed", it) }
}
