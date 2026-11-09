package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: UserStatus,
    size: Dp = 12.dp,
    borderWidth: Dp = 2.dp
) {
    val color = when (status) {
        UserStatus.ONLINE -> StatusOnline
        UserStatus.IDLE -> StatusIdle
        UserStatus.DND -> StatusDnd
        UserStatus.OFFLINE -> StatusOffline
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(borderWidth, YapinChannelBg, CircleShape)
    )
}

@Composable
fun UserAvatar(
    user: User,
    size: Dp = 40.dp,
    showStatus: Boolean = true,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ringColor = if (isSpeaking) StatusOnline else Color.Transparent
    val ringWidth = if (isSpeaking) 2.5.dp else 0.dp

    Box(
        modifier = modifier
            .size(size)
            .testTag("avatar_${user.username}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(user.avatarBgColorHex))
                .border(ringWidth, ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.avatarEmoji,
                fontSize = (size.value * 0.5f).sp
            )
        }

        if (showStatus) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
            ) {
                StatusBadge(status = user.status, size = (size * 0.35f).coerceAtLeast(10.dp))
            }
        }
    }
}

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    if (role == UserRole.MEMBER) return

    val (bg, textColor) = when (role) {
        UserRole.OWNER -> Pair(Color(0xFF382C0E), Color(0xFFFFD700))
        UserRole.ADMIN -> Pair(Color(0xFF3B1528), Color(0xFFF472B6))
        UserRole.MODERATOR -> Pair(Color(0xFF0C2E3A), Color(0xFF22D3EE))
        UserRole.BOT -> Pair(Color(0xFF1E2140), Color(0xFF818CF8))
        UserRole.MEMBER -> Pair(Color.Transparent, Color.Transparent)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = role.title,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
