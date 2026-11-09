package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveVoiceRoom
import com.example.data.model.Server
import com.example.ui.MainNavMode
import com.example.ui.theme.*

@Composable
fun YapinServerRail(
    servers: List<Server>,
    selectedServerId: String?,
    currentMode: MainNavMode,
    activeVoiceRoom: ActiveVoiceRoom?,
    onSelectHome: () -> Unit,
    onSelectFriends: () -> Unit,
    onSelectServer: (String) -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenAddServer: () -> Unit,
    onOpenVoiceOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(YapinRailBg)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // YAPIN App / Home / DM Button
        val isHomeSelected = currentMode == MainNavMode.DIRECT_MESSAGES
        ServerRailItem(
            isSelected = isHomeSelected,
            onClick = onSelectHome,
            testTag = "rail_home_btn"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(if (isHomeSelected) RoundedCornerShape(14.dp) else CircleShape)
                    .background(if (isHomeSelected) YapinPurple else YapinCardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Direct Messages",
                    tint = if (isHomeSelected) TextWhite else YapinPurpleLight,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Friends Hub shortcut
        val isFriendsSelected = currentMode == MainNavMode.FRIENDS
        ServerRailItem(
            isSelected = isFriendsSelected,
            onClick = onSelectFriends,
            testTag = "rail_friends_btn"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(if (isFriendsSelected) RoundedCornerShape(14.dp) else CircleShape)
                    .background(if (isFriendsSelected) YapinCyan else YapinCardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Friends",
                    tint = if (isFriendsSelected) TextWhite else YapinCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Divider
        HorizontalDivider(
            modifier = Modifier
                .width(36.dp)
                .padding(vertical = 4.dp),
            thickness = 2.dp,
            color = YapinBorder
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Server List
        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(servers, key = { it.id }) { server ->
                val isSelected = currentMode == MainNavMode.SERVERS && selectedServerId == server.id
                ServerRailItem(
                    isSelected = isSelected,
                    onClick = { onSelectServer(server.id) },
                    testTag = "rail_server_${server.id}"
                ) {
                    val cornerRadius by animateDpAsState(
                        targetValue = if (isSelected) 14.dp else 24.dp,
                        label = "corner"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) Color(server.iconBgColorHex) else YapinCardBg,
                        label = "color"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = server.iconEmoji,
                            fontSize = 22.sp
                        )
                    }
                }
            }

            item {
                // Add Server Button (+)
                ServerRailItem(
                    isSelected = false,
                    onClick = onOpenAddServer,
                    testTag = "rail_add_server_btn"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(YapinCardBg)
                            .border(1.dp, YapinBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add a Server",
                            tint = StatusOnline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            item {
                // Discover Servers Button (Compass)
                val isDiscoverSelected = currentMode == MainNavMode.DISCOVER
                ServerRailItem(
                    isSelected = isDiscoverSelected,
                    onClick = onOpenDiscover,
                    testTag = "rail_discover_btn"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(if (isDiscoverSelected) RoundedCornerShape(14.dp) else CircleShape)
                            .background(if (isDiscoverSelected) StatusOnline else YapinCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Discover Servers",
                            tint = if (isDiscoverSelected) TextWhite else StatusOnline,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Active Voice Room Indicator (if connected)
        if (activeVoiceRoom != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF064E3B))
                    .border(1.5.dp, StatusOnline, RoundedCornerShape(12.dp))
                    .clickable { onOpenVoiceOverlay() }
                    .testTag("rail_active_voice_badge"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Connected to Voice",
                    tint = StatusOnline,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ServerRailItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    val pillHeight by animateDpAsState(
        targetValue = if (isSelected) 36.dp else 0.dp,
        label = "pill"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pill Indicator on the left edge
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(pillHeight)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(if (isSelected) TextWhite else Color.Transparent)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Center item
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
