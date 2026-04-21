package com.safezone.app.data.remote.supabase

/**
 * Table and column references. Keep in sync with the SQL in /supabase/schema.sql
 */
object SupabaseSchema {
    const val T_PROFILES = "profiles"
    const val T_PHRASES = "security_phrases"
    const val T_SOS_EVENTS = "sos_events"
    const val T_ALERT_DELIVERIES = "alert_deliveries"
    const val T_LOCATIONS = "user_locations"

    const val BUCKET_AVATARS = "avatars"
    const val BUCKET_SOS_AUDIO = "sos-audio"
    const val BUCKET_SOS_IMAGES = "sos-images"
    const val BUCKET_VOICE_PHRASES = "voice-phrases"

    /** Postgres RPC for nearby users; see schema.sql */
    const val RPC_NEARBY_USERS = "nearby_users"
    /** RPC to broadcast an SOS to nearby users (creates alert_deliveries rows) */
    const val RPC_BROADCAST_SOS = "broadcast_sos_event"
}
