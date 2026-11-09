package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveVoiceRoom
import com.example.data.model.VoiceParticipant
import com.example.ui.theme.*

@Composable
fun YapinVoiceOverlay(
    voiceRoom: ActiveVoiceRoom,
    onCollapse: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onToggleScreenShare: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YapinRailBg)
            .padding(16.dp)
            .testTag("voice_room_overlay")
    ) {
        // --- Header Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(YapinCardBg)
                    .testTag("btn_collapse_voice")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Minimize Voice",
                    tint = TextWhite
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔊 ${voiceRoom.channel.name}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusOnline)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WebRTC Connected • 16ms • Opus 48kHz",
                        color = StatusOnline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quality / Settings badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(YapinCardBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "HD AUDIO",
                    color = YapinCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Live Animated Waveform Bar ---
        VoiceWaveformVisualizer(
            isSpeaking = voiceRoom.participants.any { it.isSpeaking },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(YapinCardBg)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Participant Grid ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(voiceRoom.participants, key = { it.user.id }) { participant ->
                VoiceParticipantCard(participant = participant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Bottom Voice Control Deck ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(YapinCardBg)
                .border(1.dp, YapinBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute Button
            VoiceActionButton(
                icon = if (voiceRoom.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (voiceRoom.isMicMuted) "Unmute" else "Mute",
                isActive = !voiceRoom.isMicMuted,
                activeColor = YapinPurple,
                inactiveColor = StatusDnd,
                onClick = onToggleMute,
                testTag = "voice_btn_mute"
            )

            // Deafen Button
            VoiceActionButton(
                icon = if (voiceRoom.isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                label = if (voiceRoom.isDeafened) "Undeafen" else "Deafen",
                isActive = !voiceRoom.isDeafened,
                activeColor = YapinCardBg,
                inactiveColor = StatusDnd,
                onClick = onToggleDeafen,
                testTag = "voice_btn_deafen"
            )

            // Screen Share Button
            VoiceActionButton(
                icon = if (voiceRoom.isScreenSharing) Icons.Default.StopScreenShare else Icons.Default.ScreenShare,
                label = if (voiceRoom.isScreenSharing) "Stop Share" else "Share Screen",
                isActive = voiceRoom.isScreenSharing,
                activeColor = YapinCyan,
                inactiveColor = YapinCardBg,
                onClick = onToggleScreenShare,
                testTag = "voice_btn_screenshare"
            )

            // Disconnect Button
            VoiceActionButton(
                icon = Icons.Default.CallEnd,
                label = "Disconnect",
                isActive = true,
                activeColor = StatusDnd,
                inactiveColor = StatusDnd,
                onClick = onDisconnect,
                testTag = "voice_btn_disconnect"
            )
        }
    }
}

@Composable
private fun VoiceParticipantCard(participant: VoiceParticipant) {
    val ringColor = if (participant.isSpeaking) StatusOnline else Color.Transparent
    val ringWidth = if (participant.isSpeaking) 3.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(YapinCardBg)
            .border(ringWidth, ringColor, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(participant.user.avatarBgColorHex))
                    .border(ringWidth, ringColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.user.avatarEmoji,
                    fontSize = 30.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (participant.user.isSelf) "${participant.user.username} (You)" else participant.user.username,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (participant.isSpeaking) {
                Text(
                    text = "Speaking...",
                    color = StatusOnline,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (participant.isMuted) {
                Text(
                    text = "Muted",
                    color = StatusDnd,
                    fontSize = 10.sp
                )
            }
        }

        // Indicator badges top-right
        if (participant.isMuted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(StatusDnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MicOff,
                    contentDescription = "Muted",
                    tint = TextWhite,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun VoiceActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else inactiveColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextWhite,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun VoiceWaveformVisualizer(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val barCount = 32
        val barWidth = 4.dp.toPx()
        val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)

        for (i in 0 until barCount) {
            val factor = if (isSpeaking) {
                kotlin.math.sin(phase + i * 0.35f) * 0.5f + 0.5f
            } else {
                0.15f
            }
            val barHeight = (size.height * factor).coerceAtLeast(4.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = (size.height - barHeight) / 2

            drawRoundRect(
                color = if (isSpeaking) Color(0xFF10B981) else Color(0xFF374151),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
    }
}
