package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.Friend
import com.example.data.model.FriendStatus
import com.example.data.model.UserStatus
import com.example.ui.theme.*

@Composable
fun YapinFriendsScreen(
    friends: List<Friend>,
    currentTab: FriendStatus,
    onTabSelected: (FriendStatus) -> Unit,
    onOpenDm: (String) -> Unit,
    onSendFriendRequest: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onToggleSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var addFriendInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YapinChatBg)
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(YapinCardBg)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("friends_btn_toggle_sidebar")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle Sidebar",
                    tint = TextWhite
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = YapinCyan,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Friends",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Add Friend pill shortcut
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (currentTab == FriendStatus.PENDING) StatusOnline else Color.Transparent)
                    .border(
                        1.dp,
                        if (currentTab == FriendStatus.PENDING) Color.Transparent else StatusOnline,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onTabSelected(FriendStatus.PENDING) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("btn_tab_add_friend")
            ) {
                Text(
                    text = "Add Friend",
                    color = if (currentTab == FriendStatus.PENDING) TextWhite else StatusOnline,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Tabs Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(YapinCardBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FriendTabButton(
                title = "Online",
                count = friends.count { !it.isPending && it.user.status != UserStatus.OFFLINE },
                isSelected = currentTab == FriendStatus.ONLINE,
                onClick = { onTabSelected(FriendStatus.ONLINE) }
            )

            FriendTabButton(
                title = "All",
                count = friends.count { !it.isPending },
                isSelected = currentTab == FriendStatus.ALL,
                onClick = { onTabSelected(FriendStatus.ALL) }
            )

            val pendingCount = friends.count { it.isPending }
            FriendTabButton(
                title = "Pending",
                count = pendingCount,
                badgeColor = if (pendingCount > 0) StatusDnd else null,
                isSelected = currentTab == FriendStatus.PENDING,
                onClick = { onTabSelected(FriendStatus.PENDING) }
            )
        }

        HorizontalDivider(thickness = 1.dp, color = YapinBorder)

        // --- Content Area ---
        if (currentTab == FriendStatus.PENDING) {
            // Add Friend Input Header + Pending List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "ADD FRIEND",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You can add friends with their YAPIN username and tag (e.g. Shadow#1337).",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(YapinInputBg)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = addFriendInput,
                        onValueChange = { addFriendInput = it },
                        placeholder = {
                            Text(
                                text = "Enter username#0000",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_add_friend_field"),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (addFriendInput.isNotBlank()) {
                                onSendFriendRequest(addFriendInput)
                                addFriendInput = ""
                            }
                        },
                        enabled = addFriendInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = YapinPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_send_friend_request")
                    ) {
                        Text("Send Request", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "PENDING REQUESTS (${friends.count { it.isPending }})",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Friends List
        val filteredFriends = when (currentTab) {
            FriendStatus.ONLINE -> friends.filter { !it.isPending && it.user.status != UserStatus.OFFLINE }
            FriendStatus.ALL -> friends.filter { !it.isPending }
            FriendStatus.PENDING -> friends.filter { it.isPending }
            FriendStatus.BLOCKED -> friends.filter { it.isBlocked }
        }.filter {
            if (searchQuery.isBlank()) true
            else it.user.username.contains(searchQuery, ignoreCase = true)
        }

        if (filteredFriends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (currentTab == FriendStatus.PENDING) "No pending friend requests." else "No friends found in this list.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFriends, key = { it.user.id }) { friend ->
                    FriendCard(
                        friend = friend,
                        onOpenDm = { onOpenDm(friend.user.id) },
                        onAccept = { onAcceptRequest(friend.user.id) },
                        onReject = { onRejectRequest(friend.user.id) },
                        onRemove = { onRemoveFriend(friend.user.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendTabButton(
    title: String,
    count: Int,
    badgeColor: Color? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) YapinBorder else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (isSelected) TextWhite else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeColor ?: YapinInputBg)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "$count",
                    color = TextWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FriendCard(
    friend: Friend,
    onOpenDm: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(YapinCardBg)
            .padding(12.dp)
            .testTag("friend_card_${friend.user.username}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(user = friend.user, size = 44.dp)
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.user.username,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = friend.user.customStatus ?: friend.lastActive,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (friend.isPending) {
            if (friend.isIncoming) {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(StatusOnline)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept Request",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onReject,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(YapinInputBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reject Request",
                        tint = StatusDnd,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Text(
                    text = "Outgoing Request",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Action buttons: DM & More
            IconButton(
                onClick = onOpenDm,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(YapinInputBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Message",
                    tint = TextWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
