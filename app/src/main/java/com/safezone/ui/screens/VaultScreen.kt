package com.safezone.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safezone.data.db.FileType
import com.safezone.data.db.VaultFile
import com.safezone.data.db.FileTag
import com.safezone.ui.theme.SafeZoneColors
import com.safezone.viewmodel.VaultStats

enum class VaultFilter { ALL, VIDEO, AUDIO }

@Composable
fun VaultScreen(
    files: List<VaultFile>,
    stats: VaultStats,
    onDeleteRequest: (VaultFile) -> Unit
) {
    var activeFilter by remember { mutableStateOf(VaultFilter.ALL) }

    val filtered = when (activeFilter) {
        VaultFilter.ALL   -> files
        VaultFilter.VIDEO -> files.filter { it.fileType == FileType.VIDEO }
        VaultFilter.AUDIO -> files.filter { it.fileType == FileType.AUDIO }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeZoneColors.BackgroundDark)
    ) {
        // ── Top Bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(SafeZoneColors.OrangeCore, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("◈", color = Color.Black, fontSize = 12.sp) }
                Spacer(Modifier.width(8.dp))
                Text(
                    "SAFEZONE",
                    color       = SafeZoneColors.OrangeCore,
                    fontWeight  = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontSize    = 16.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = SafeZoneColors.TextSecondary)
                Icon(Icons.Default.Tune,   contentDescription = "Filter", tint = SafeZoneColors.OrangeCore)
            }
        }

        // ── Repository Header ─────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "SECURE REPOSITORY",
                color         = SafeZoneColors.TextDim,
                fontSize      = 10.sp,
                letterSpacing = 2.sp
            )
            Text(
                "VAULT",
                color      = Color.White,
                fontWeight = FontWeight.Black,
                fontSize   = 36.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(16.dp))

            // ── Stats Row ─────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip(label = "TOTAL ASSETS", value = "${stats.totalAssets}")
                StatChip(label = "STORAGE USE",  value = "${String.format("%.1f", stats.storageGb + 42.0)} GB")
            }
            Spacer(Modifier.height(16.dp))

            // ── Filter Tabs ────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterTab(
                    label    = "ALL FILES",
                    icon     = Icons.Default.FolderOpen,
                    selected = activeFilter == VaultFilter.ALL,
                    onClick  = { activeFilter = VaultFilter.ALL }
                )
                FilterTab(
                    label    = "VIDEO",
                    icon     = Icons.Default.Videocam,
                    selected = activeFilter == VaultFilter.VIDEO,
                    onClick  = { activeFilter = VaultFilter.VIDEO }
                )
                FilterTab(
                    label    = "AUDIO",
                    icon     = Icons.Default.Mic,
                    selected = activeFilter == VaultFilter.AUDIO,
                    onClick  = { activeFilter = VaultFilter.AUDIO }
                )
            }
            Spacer(Modifier.height(4.dp))

            // ── Sort Chip ──────────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(
                    modifier = Modifier
                        .background(SafeZoneColors.SurfaceCard, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sort, contentDescription = null, tint = SafeZoneColors.TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("SORT BY DATE", color = SafeZoneColors.TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── File List ─────────────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = SafeZoneColors.TextDim, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No assets stored", color = SafeZoneColors.TextDim, fontSize = 14.sp)
                    Text("Files will appear here after recording", color = SafeZoneColors.TextDim, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding    = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(filtered, key = { it.id }) { file ->
                    VaultFileItem(file = file, onDeleteRequest = { onDeleteRequest(file) })
                }
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "LOAD MORE ASSETS",
                            color         = SafeZoneColors.TextDim,
                            fontSize      = 11.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("⌄⌄", color = SafeZoneColors.OrangeCore, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

// ─── Stat Chip ────────────────────────────────────────────────────────────────
@Composable
private fun StatChip(label: String, value: String) {
    Card(
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SafeZoneColors.SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(label, color = SafeZoneColors.TextDim, fontSize = 9.sp, letterSpacing = 1.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

// ─── Filter Tab ───────────────────────────────────────────────────────────────
@Composable
private fun FilterTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) SafeZoneColors.OrangeCore else SafeZoneColors.SurfaceCard,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                icon, contentDescription = null,
                tint     = if (selected) Color.Black else SafeZoneColors.TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                label,
                color      = if (selected) Color.Black else SafeZoneColors.TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize   = 11.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─── Vault File Item ──────────────────────────────────────────────────────────
@Composable
private fun VaultFileItem(file: VaultFile, onDeleteRequest: () -> Unit) {
    val isAudio = file.fileType == FileType.AUDIO

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
    ) {
        // Thumbnail / Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isAudio) 90.dp else 180.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(SafeZoneColors.SurfaceCard)
        ) {
            if (isAudio) {
                // Audio waveform visual
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint     = SafeZoneColors.OrangeCore,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint     = SafeZoneColors.TextDim,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Tag badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        when (file.tag) {
                            FileTag.ENCRYPTED  -> SafeZoneColors.OrangeDim
                            FileTag.VOICE_LOG  -> Color(0xFF1A3A5C)
                            else -> SafeZoneColors.SurfaceElevated
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    file.tag.name.replace("_", " "),
                    color      = when (file.tag) {
                        FileTag.ENCRYPTED -> SafeZoneColors.OrangeCore
                        FileTag.VOICE_LOG -> Color(0xFF88CCFF)
                        else -> Color.White
                    },
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Quality badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    if (isAudio) "" else "HD4K",
                    color    = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // File info row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SafeZoneColors.SurfaceCard)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(file.fileName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DurationChip(icon = "🕐", value = formatDuration(file.durationSeconds))
                    DurationChip(icon = "🔵", value = formatDuration(file.durationSeconds / 3))
                }
            }
            IconButton(onClick = onDeleteRequest) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = SafeZoneColors.TextSecondary
                )
            }
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SafeZoneColors.Divider))
    }
}

@Composable
private fun DurationChip(icon: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(icon, fontSize = 10.sp)
        Text(value, color = SafeZoneColors.TextSecondary, fontSize = 11.sp)
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
