package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
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
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.ui.theme.*

@Composable
fun YapinMembersDrawer(
    members: List<User>,
    onClose: () -> Unit,
    onOpenDmWithUser: (String) -> Unit,
    onAddFriend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMemberForCard by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(YapinChannelBg)
            .padding(16.dp)
            .testTag("members_drawer")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MEMBERS (${members.size})",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Members",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Split members into Online and Offline
        val onlineMembers = members.filter { it.status != UserStatus.OFFLINE }
        val offlineMembers = members.filter { it.status == UserStatus.OFFLINE }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (onlineMembers.isNotEmpty()) {
                item {
                    Text(
                        text = "ONLINE — ${onlineMembers.size}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(onlineMembers, key = { it.id }) { user ->
                    MemberRow(
                        user = user,
                        onClick = { selectedMemberForCard = user }
                    )
                }
            }

            if (offlineMembers.isNotEmpty()) {
                item {
                    Text(
                        text = "OFFLINE — ${offlineMembers.size}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(offlineMembers, key = { it.id }) { user ->
                    MemberRow(
                        user = user,
                        onClick = { selectedMemberForCard = user }
                    )
                }
            }
        }
    }

    // Mini profile card dialog on tap
    selectedMemberForCard?.let { user ->
        AlertDialog(
            onDismissRequest = { selectedMemberForCard = null },
            confirmButton = {},
            containerColor = YapinCardBg,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(user = user, size = 64.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.username,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "#${user.tag}",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    RoleBadge(role = user.role)

                    user.customStatus?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = YapinCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!user.isSelf) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onOpenDmWithUser(user.id)
                                    selectedMemberForCard = null
                                    onClose()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = YapinPurple),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Message", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    onAddFriend("${user.username}#${user.tag}")
                                    selectedMemberForCard = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Friend", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun MemberRow(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(user = user, size = 32.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.username,
                    color = Color(user.role.badgeColorHex),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.role == UserRole.BOT) {
                    Spacer(modifier = Modifier.width(4.dp))
                    RoleBadge(role = user.role)
                }
            }
            user.customStatus?.let {
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
