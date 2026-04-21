-- =====================================================================
-- SafeZone — Supabase schema
-- =====================================================================
-- Run this in the Supabase SQL Editor (project → SQL → New query).
-- It is idempotent: you can re-run it safely.
-- =====================================================================

-- Extensions
create extension if not exists "uuid-ossp";
create extension if not exists postgis;

-- ---------------------------------------------------------------------
-- profiles
-- ---------------------------------------------------------------------
create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    email text,
    full_name text default '',
    phone text default '',
    address text default '',
    photo_url text,
    emergency_contacts jsonb default '[]'::jsonb,
    fcm_token text,
    last_lat double precision,
    last_lng double precision,
    last_seen_at timestamptz,
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

-- ---------------------------------------------------------------------
-- user_locations (frequently-updated; drives nearby queries)
-- ---------------------------------------------------------------------
create table if not exists public.user_locations (
    user_id uuid primary key references auth.users(id) on delete cascade,
    lat double precision not null,
    lng double precision not null,
    geom geography(point, 4326)
        generated always as (st_setsrid(st_makepoint(lng, lat), 4326)::geography) stored,
    updated_at timestamptz default now()
);
create index if not exists user_locations_geom_idx
    on public.user_locations using gist (geom);

-- ---------------------------------------------------------------------
-- security_phrases
-- ---------------------------------------------------------------------
create table if not exists public.security_phrases (
    id uuid primary key default uuid_generate_v4(),
    user_id uuid references auth.users(id) on delete cascade,
    text text not null,
    action text not null check (action in ('notify_contacts', 'silent_police', 'full_sos')),
    voice_url text,
    enabled boolean default true,
    created_at timestamptz default now()
);
create index if not exists security_phrases_user_idx
    on public.security_phrases (user_id);

-- ---------------------------------------------------------------------
-- sos_events
-- ---------------------------------------------------------------------
create table if not exists public.sos_events (
    id uuid primary key default uuid_generate_v4(),
    user_id uuid references auth.users(id) on delete cascade,
    user_name text default '',
    user_photo_url text,
    lat double precision not null,
    lng double precision not null,
    geom geography(point, 4326)
        generated always as (st_setsrid(st_makepoint(lng, lat), 4326)::geography) stored,
    started_at timestamptz default now(),
    ended_at timestamptz,
    duration_seconds bigint
        generated always as (coalesce(extract(epoch from (ended_at - started_at))::bigint, 0)) stored,
    status text default 'active'
        check (status in ('active', 'resolved', 'canceled', 'high_alert')),
    trigger_source text default 'manual'
        check (trigger_source in ('manual', 'phrase', 'geofence', 'ble')),
    audio_url text,
    image_url text,
    note text
);
create index if not exists sos_events_user_idx on public.sos_events (user_id, started_at desc);
create index if not exists sos_events_geom_idx on public.sos_events using gist (geom);

-- ---------------------------------------------------------------------
-- alert_deliveries (fan-out targets for an SOS)
-- ---------------------------------------------------------------------
create table if not exists public.alert_deliveries (
    id uuid primary key default uuid_generate_v4(),
    event_id uuid references public.sos_events(id) on delete cascade,
    recipient_id uuid references auth.users(id) on delete cascade,
    user_id uuid,
    user_name text,
    user_photo_url text,
    lat double precision,
    lng double precision,
    distance_meters double precision,
    trigger_source text,
    delivered_at timestamptz default now(),
    acknowledged_at timestamptz
);
create index if not exists alert_deliveries_recipient_idx
    on public.alert_deliveries (recipient_id, delivered_at desc);

-- =====================================================================
-- RPC: nearby_users
-- =====================================================================
create or replace function public.nearby_users(
    lat double precision,
    lng double precision,
    radius_meters integer default 50
)
returns table (
    id uuid,
    name text,
    photo_url text,
    lat double precision,
    lng double precision,
    distance_meters double precision
)
language sql
security definer
set search_path = public
as $$
    select
        p.id,
        p.full_name as name,
        p.photo_url,
        ul.lat,
        ul.lng,
        st_distance(ul.geom, st_setsrid(st_makepoint(lng, lat), 4326)::geography) as distance_meters
    from user_locations ul
    join profiles p on p.id = ul.user_id
    where st_dwithin(ul.geom, st_setsrid(st_makepoint(lng, lat), 4326)::geography, radius_meters)
      and ul.user_id <> auth.uid()
    order by distance_meters asc;
$$;

-- =====================================================================
-- RPC: broadcast_sos_event
-- =====================================================================
create or replace function public.broadcast_sos_event(
    event_id uuid,
    radius_meters integer default 50
)
returns integer
language plpgsql
security definer
set search_path = public
as $$
declare
    ev public.sos_events%rowtype;
    sender public.profiles%rowtype;
    delivered integer := 0;
begin
    select * into ev from sos_events where id = event_id;
    if ev is null then return 0; end if;
    select * into sender from profiles where id = ev.user_id;

    insert into alert_deliveries (
        event_id, recipient_id, user_id, user_name, user_photo_url,
        lat, lng, distance_meters, trigger_source
    )
    select
        ev.id, ul.user_id, ev.user_id,
        coalesce(sender.full_name, ''), sender.photo_url,
        ev.lat, ev.lng,
        st_distance(ul.geom, ev.geom),
        ev.trigger_source
    from user_locations ul
    where ul.user_id <> ev.user_id
      and st_dwithin(ul.geom, ev.geom, radius_meters);

    get diagnostics delivered = row_count;
    return delivered;
end;
$$;

-- =====================================================================
-- Storage buckets
-- =====================================================================
insert into storage.buckets (id, name, public) values
    ('avatars', 'avatars', true),
    ('sos-audio', 'sos-audio', false),
    ('sos-images', 'sos-images', false),
    ('voice-phrases', 'voice-phrases', false)
on conflict (id) do nothing;

-- =====================================================================
-- Row Level Security
-- =====================================================================
alter table public.profiles enable row level security;
alter table public.user_locations enable row level security;
alter table public.security_phrases enable row level security;
alter table public.sos_events enable row level security;
alter table public.alert_deliveries enable row level security;

drop policy if exists "profiles_read_all" on public.profiles;
create policy "profiles_read_all" on public.profiles
    for select using (true);

drop policy if exists "profiles_self_write" on public.profiles;
create policy "profiles_self_write" on public.profiles
    for all using (auth.uid() = id) with check (auth.uid() = id);

drop policy if exists "locations_self_write" on public.user_locations;
create policy "locations_self_write" on public.user_locations
    for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

drop policy if exists "locations_read_all" on public.user_locations;
create policy "locations_read_all" on public.user_locations
    for select using (true);

drop policy if exists "phrases_owner" on public.security_phrases;
create policy "phrases_owner" on public.security_phrases
    for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

drop policy if exists "sos_events_owner_write" on public.sos_events;
create policy "sos_events_owner_write" on public.sos_events
    for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

drop policy if exists "sos_events_read_if_recipient" on public.sos_events;
create policy "sos_events_read_if_recipient" on public.sos_events
    for select using (
        auth.uid() = user_id
        or exists (
            select 1 from alert_deliveries ad
            where ad.event_id = sos_events.id and ad.recipient_id = auth.uid()
        )
    );

drop policy if exists "alerts_recipient_read" on public.alert_deliveries;
create policy "alerts_recipient_read" on public.alert_deliveries
    for select using (auth.uid() = recipient_id or auth.uid() = user_id);

drop policy if exists "alerts_recipient_ack" on public.alert_deliveries;
create policy "alerts_recipient_ack" on public.alert_deliveries
    for update using (auth.uid() = recipient_id) with check (auth.uid() = recipient_id);

-- =====================================================================
-- Enable Realtime on alert_deliveries
-- =====================================================================
alter publication supabase_realtime add table public.alert_deliveries;
