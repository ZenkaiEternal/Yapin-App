package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainNavMode
import com.example.ui.theme.*

@Composable
fun YapinChannelSidebar(
    server: Server?,
    currentMode: MainNavMode,
    selectedChannelId: String,
    selectedDmUserId: String?,
    friends: List<Friend>,
    currentUser: User,
    activeVoiceRoom: ActiveVoiceRoom?,
    onSelectChannel: (Channel) -> Unit,
    onSelectDmUser: (String) -> Unit,
    onOpenFriendsHub: () -> Unit,
    onOpenAddChannel: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUserProfile: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit,
    onLeaveServer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showServerMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(YapinChannelBg)
    ) {
        // --- Top Header ---
        if (currentMode == MainNavMode.SERVERS && server != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { showServerMenu = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = server.name,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Server Menu",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showServerMenu,
                    onDismissRequest = { showServerMenu = false },
                    modifier = Modifier.background(YapinCardBg)
                ) {
                    DropdownMenuItem(
                        text = { Text("Create Channel", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = StatusOnline) },
                        onClick = {
                            showServerMenu = false
                            onOpenAddChannel()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Server Settings", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = YapinPurpleLight) },
                        onClick = {
                            showServerMenu = false
                            onOpenSettings()
                        }
                    )
                    if (!server.isOwner) {
                        HorizontalDivider(color = YapinBorder)
                        DropdownMenuItem(
                            text = { Text("Leave Server", color = StatusDnd) },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = StatusDnd) },
                            onClick = {
                                showServerMenu = false
                                onLeaveServer(server.id)
                            }
                        )
                    }
                }
            }
        } else {
            // Direct Messages Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Direct Messages",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = YapinBorder)

        // --- Main Content: Channels or DMs ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (currentMode == MainNavMode.SERVERS && server != null) {
                // Group channels by category
                val categories = server.channels.groupBy { it.category }

                categories.forEach { (categoryName, channelList) ->
                    item(key = "cat_$categoryName") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 4.dp, start = 8.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = categoryName,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            IconButton(
                                onClick = onOpenAddChannel,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Channel",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    items(channelList, key = { it.id }) { channel ->
                        val isSelected = selectedChannelId == channel.id
                        val isVoiceConnected = activeVoiceRoom?.channel?.id == channel.id

                        ChannelRow(
                            channel = channel,
                            isSelected = isSelected,
                            isVoiceConnected = isVoiceConnected,
                            onClick = { onSelectChannel(channel) }
                        )
                    }
                }
            } else {
                // Direct Messages / Friends section
                item {
                    // Friends Tab Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentMode == MainNavMode.FRIENDS) YapinCardBg else Color.Transparent)
                            .clickable { onOpenFriendsHub() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("dm_friends_hub_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Friends",
                            tint = if (currentMode == MainNavMode.FRIENDS) TextWhite else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Friends",
                            color = if (currentMode == MainNavMode.FRIENDS) TextWhite else TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        val pendingCount = friends.count { it.isPending && it.isIncoming }
                        if (pendingCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StatusDnd)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$pendingCount",
                                    color = TextWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "DIRECT MESSAGES",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 6.dp)
                    )
                }

                val activeFriends = friends.filterNot { it.isPending }
                items(activeFriends, key = { it.user.id }) { friend ->
                    val isSelected = currentMode == MainNavMode.DIRECT_MESSAGES && selectedDmUserId == friend.user.id
                    DirectMessageRow(
                        friend = friend,
                        isSelected = isSelected,
                        onClick = { onSelectDmUser(friend.user.id) }
                    )
                }
            }
        }

        // --- Bottom User Bar ---
        UserDeck(
            user = currentUser,
            isMicMuted = activeVoiceRoom?.isMicMuted ?: false,
            isDeafened = activeVoiceRoom?.isDeafened ?: false,
            onOpenProfile = onOpenUserProfile,
            onToggleMic = onToggleMic,
            onToggleDeafen = onToggleDeafen,
            onOpenSettings = onOpenSettings
        )
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isSelected: Boolean,
    isVoiceConnected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isVoiceConnected -> Color(0xFF0D3328)
        isSelected -> YapinCardBg
        else -> Color.Transparent
    }
    val contentColor = when {
        isVoiceConnected -> StatusOnline
        isSelected -> TextWhite
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag("channel_item_${channel.name}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (channel.type == ChannelType.TEXT) Icons.Default.Tag else Icons.Default.VolumeUp,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = channel.name,
            color = contentColor,
            fontWeight = if (isSelected || isVoiceConnected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isVoiceConnected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF065F46))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Connected",
                    color = StatusOnline,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DirectMessageRow(
    friend: Friend,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) YapinCardBg else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag("dm_user_${friend.user.username}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(user = friend.user, size = 32.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.user.username,
                color = if (isSelected) TextWhite else TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            friend.user.customStatus?.let {
                Text(
                    text = it,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun UserDeck(
    user: User,
    isMicMuted: Boolean,
    isDeafened: Boolean,
    onOpenProfile: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(YapinRailBg)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag("user_deck"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User profile snippet
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .clickable { onOpenProfile() }
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(user = user, size = 34.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "#${user.tag}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Action icons: Mic, Headset, Settings
        IconButton(
            onClick = onToggleMic,
            modifier = Modifier
                .size(32.dp)
                .testTag("btn_toggle_mic")
        ) {
            Icon(
                imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isMicMuted) "Unmute Mic" else "Mute Mic",
                tint = if (isMicMuted) StatusDnd else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onToggleDeafen,
            modifier = Modifier
                .size(32.dp)
                .testTag("btn_toggle_deafen")
        ) {
            Icon(
                imageVector = if (isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                contentDescription = if (isDeafened) "Undeafen" else "Deafen",
                tint = if (isDeafened) StatusDnd else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(32.dp)
                .testTag("btn_open_settings")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
