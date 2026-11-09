package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.ui.theme.*

@Composable
fun YapinChatArea(
    channel: Channel?,
    dmUser: User?,
    messages: List<Message>,
    typingUsers: Set<String>,
    replyingTo: Message?,
    editingMessage: Message?,
    activeVoiceRoom: ActiveVoiceRoom?,
    onToggleSidebar: () -> Unit,
    onToggleMembers: () -> Unit,
    onSendMessage: (String, String?) -> Unit,
    onToggleReaction: (Message, String) -> Unit,
    onReplyTo: (Message) -> Unit,
    onEditMessage: (Message) -> Unit,
    onDeleteMessage: (Message) -> Unit,
    onCancelReplyOrEdit: () -> Unit,
    onExpandVoiceOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    var showEmojiSheet by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    // If editing, fill the input with the message content
    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            messageText = editingMessage.content
        }
    }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val title = when {
        channel != null -> "#${channel.name}"
        dmUser != null -> "@${dmUser.username}"
        else -> "YAPIN"
    }

    val topic = when {
        channel != null -> channel.topic
        dmUser != null -> dmUser.customStatus ?: "Direct Message"
        else -> ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YapinChatBg)
    ) {
        // --- Top Header ---
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
                    .testTag("btn_toggle_sidebar")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle Navigation",
                    tint = TextWhite
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (topic.isNotBlank()) {
                    Text(
                        text = topic,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Voice Room Status Pill (if connected)
            if (activeVoiceRoom != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF064E3B))
                        .border(1.dp, StatusOnline, RoundedCornerShape(16.dp))
                        .clickable { onExpandVoiceOverlay() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("btn_voice_status_pill")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusOnline)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Voice Connected",
                            color = StatusOnline,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Toggle Members Drawer Button
            IconButton(
                onClick = onToggleMembers,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_toggle_members")
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Toggle Members List",
                    tint = TextSecondary
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = YapinBorder)

        // --- Message List Area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(YapinCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = YapinPurpleLight,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Welcome to $title!",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This is the start of the $title conversation. Say hello to everyone!",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageItem(
                            message = message,
                            onToggleReaction = { emoji -> onToggleReaction(message, emoji) },
                            onReply = { onReplyTo(message) },
                            onEdit = { onEditMessage(message) },
                            onDelete = { onDeleteMessage(message) }
                        )
                    }
                }
            }
        }

        // --- Typing Indicator ---
        AnimatedVisibility(visible = typingUsers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${typingUsers.joinToString(", ")} is typing...",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        // --- Reply / Edit Banner ---
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YapinCardBg)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null,
                        tint = YapinPurpleLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Replying to ${replyingTo.sender.username}",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = onCancelReplyOrEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Reply",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else if (editingMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YapinCardBg)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = YapinCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Editing message",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = {
                        onCancelReplyOrEdit()
                        messageText = ""
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Edit",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- Bottom Message Input Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(YapinInputBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment Button (+)
                IconButton(
                    onClick = { showAttachmentSheet = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_attach_media")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Attachment",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text Input
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text(
                            text = "Message $title",
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
                        .testTag("input_message_field"),
                    maxLines = 4
                )

                // Emoji Button
                IconButton(
                    onClick = { showEmojiSheet = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_emoji_picker")
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Insert Emoji",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText, null)
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (messageText.isNotBlank()) YapinPurple else YapinCardBg)
                    .testTag("btn_send_message")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = if (messageText.isNotBlank()) TextWhite else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Emoji Picker Modal Sheet
    if (showEmojiSheet) {
        EmojiPickerSheet(
            onEmojiSelected = { emoji ->
                messageText += emoji
                showEmojiSheet = false
            },
            onDismiss = { showEmojiSheet = false }
        )
    }

    // Attachment Selector Modal Sheet
    if (showAttachmentSheet) {
        AttachmentSheet(
            onSelectAttachment = { name ->
                onSendMessage(messageText.ifBlank { "Shared an attachment" }, name)
                messageText = ""
                showAttachmentSheet = false
            },
            onDismiss = { showAttachmentSheet = false }
        )
    }
}

@Composable
private fun MessageItem(
    message: Message,
    onToggleReaction: (String) -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showActionMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { showActionMenu = !showActionMenu }
            .padding(6.dp)
            .testTag("message_item_${message.id}")
    ) {
        // Quoted reply snippet if present
        if (message.replyToUser != null) {
            Row(
                modifier = Modifier
                    .padding(start = 28.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(10.dp)
                        .border(
                            width = 1.5.dp,
                            color = YapinBorder,
                            shape = RoundedCornerShape(topStart = 4.dp)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "@${message.replyToUser}: ${message.replyToContent.orEmpty()}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            UserAvatar(user = message.sender, size = 38.dp, showStatus = false)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.sender.username,
                        color = Color(message.sender.role.badgeColorHex),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    RoleBadge(role = message.sender.role)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.timestamp,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    if (message.isEdited) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(edited)",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Message Text
                Text(
                    text = message.content,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Attachment Card if present
                if (message.attachmentName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(YapinCardBg)
                            .border(1.dp, YapinBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = YapinCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = message.attachmentName,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Ready to preview • 1.4 MB",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Reactions Row
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        message.reactions.forEach { reaction ->
                            val hasReacted = reaction.userIds.contains("user_me")
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (hasReacted) Color(0xFF2E234A) else YapinCardBg)
                                    .border(
                                        width = 1.dp,
                                        color = if (hasReacted) YapinPurpleLight else YapinBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onToggleReaction(reaction.emoji) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = reaction.emoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${reaction.count}",
                                        color = if (hasReacted) YapinPurpleLight else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Toolbar (shows on click / hover)
        AnimatedVisibility(visible = showActionMenu) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, start = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(YapinCardBg)
                    .border(1.dp, YapinBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("👍", "🔥", "🚀", "❤️", "🎮").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clickable {
                                onToggleReaction(emoji)
                                showActionMenu = false
                            }
                            .padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        onReply()
                        showActionMenu = false
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = "Reply",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (message.sender.isSelf) {
                    IconButton(
                        onClick = {
                            onEdit()
                            showActionMenu = false
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onDelete()
                            showActionMenu = false
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = StatusDnd,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiPickerSheet(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = YapinCardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "COMMUNITY EMOTES",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val emojis = listOf(
                "🎮", "👾", "🔥", "🚀", "💜", "✨",
                "👑", "🛡️", "🤖", "🎧", "☕", "🌸",
                "💯", "⚡", "🙌", "😂", "😎", "🍿",
                "🎯", "🕹️", "🏆", "🍕", "🍔", "💎"
            )

            val rows = emojis.chunked(6)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    row.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .clickable { onEmojiSelected(emoji) }
                                .padding(8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentSheet(
    onSelectAttachment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = YapinCardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SHARE MEDIA & FILES",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val attachments = listOf(
                Triple("Victory_Royale_Screen.png", "1.8 MB Image", Icons.Default.Image),
                Triple("Yapin_Tournament_Highlights.mp4", "12.4 MB Clip", Icons.Default.PlayCircle),
                Triple("Custom_Config_Preset.json", "24 KB File", Icons.Default.FilePresent),
                Triple("Aesthetic_Wallpaper_4K.jpg", "3.2 MB Artwork", Icons.Default.Palette)
            )

            attachments.forEach { (name, size, icon) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectAttachment(name) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = YapinPurpleLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = name, color = TextWhite, fontWeight = FontWeight.Medium)
                        Text(text = size, color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
