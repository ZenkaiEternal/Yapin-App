package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelType
import com.example.data.model.User
import com.example.data.model.UserSettings
import com.example.data.model.UserStatus
import com.example.ui.theme.*

@Composable
fun CreateServerDialog(
    onDismiss: () -> Unit,
    onCreateServer: (String, String, String) -> Unit
) {
    var serverName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⚡") }
    var description by remember { mutableStateOf("") }

    val emojis = listOf("⚡", "🎮", "👾", "🔥", "🚀", "☕", "🌸", "💻", "💎", "🛡️")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YapinCardBg,
        title = {
            Text("Create Your Server", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Your server is where you and your friends hang out. Make yours and start chatting.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("SERVER ICON", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.forEach { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) YapinPurple else YapinInputBg)
                                .border(
                                    1.dp,
                                    if (isSelected) TextWhite else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("SERVER NAME", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    placeholder = { Text("e.g. Squad Hangout", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YapinPurpleLight,
                        unfocusedBorderColor = YapinBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_server_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("DESCRIPTION", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("What's this server about?", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YapinPurpleLight,
                        unfocusedBorderColor = YapinBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (serverName.isNotBlank()) {
                        onCreateServer(serverName, selectedEmoji, description)
                    }
                },
                enabled = serverName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = YapinPurple),
                modifier = Modifier.testTag("btn_confirm_create_server")
            ) {
                Text("Create Server", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun CreateChannelDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onCreateChannel: (String, ChannelType, String) -> Unit
) {
    var channelName by remember { mutableStateOf("") }
    var channelType by remember { mutableStateOf(ChannelType.TEXT) }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "TEXT CHANNELS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YapinCardBg,
        title = {
            Text("Create Channel", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("CHANNEL TYPE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Text Channel Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (channelType == ChannelType.TEXT) Color(0xFF2E234A) else YapinInputBg)
                        .border(
                            1.dp,
                            if (channelType == ChannelType.TEXT) YapinPurpleLight else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { channelType = ChannelType.TEXT }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tag, contentDescription = null, tint = TextWhite)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Text Channel", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Post messages, images, memes, and opinions", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Voice Channel Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (channelType == ChannelType.VOICE) Color(0xFF103A2B) else YapinInputBg)
                        .border(
                            1.dp,
                            if (channelType == ChannelType.VOICE) StatusOnline else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { channelType = ChannelType.VOICE }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = StatusOnline)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Voice Channel", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Hang out together with WebRTC low-latency voice and video", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("CHANNEL NAME", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = channelName,
                    onValueChange = { channelName = it },
                    placeholder = { Text("new-channel", color = TextMuted) },
                    prefix = {
                        Text(
                            text = if (channelType == ChannelType.TEXT) "# " else "🔊 ",
                            color = TextSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YapinPurpleLight,
                        unfocusedBorderColor = YapinBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_channel_name"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (channelName.isNotBlank()) {
                        onCreateChannel(channelName, channelType, selectedCategory)
                    }
                },
                enabled = channelName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = YapinPurple),
                modifier = Modifier.testTag("btn_confirm_create_channel")
            ) {
                Text("Create Channel", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun UserProfileModal(
    currentUser: User,
    onDismiss: () -> Unit,
    onUpdateProfile: (String, String?, String, UserStatus) -> Unit
) {
    var username by remember { mutableStateOf(currentUser.username) }
    var customStatus by remember { mutableStateOf(currentUser.customStatus.orEmpty()) }
    var selectedEmoji by remember { mutableStateOf(currentUser.avatarEmoji) }
    var selectedStatus by remember { mutableStateOf(currentUser.status) }

    val emojis = listOf("⚡", "👾", "🌸", "🗡️", "👑", "☕", "🤖", "🔥", "🎧", "🦊")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YapinCardBg,
        title = {
            Text("Edit User Profile", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Large Avatar Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(currentUser.avatarBgColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedEmoji, fontSize = 36.sp)
                    }
                }

                Text("CHOOSE AVATAR", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.take(5).forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedEmoji == emoji) YapinPurple else YapinInputBg)
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.drop(5).forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedEmoji == emoji) YapinPurple else YapinInputBg)
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("DISPLAY NAME", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YapinPurpleLight,
                        unfocusedBorderColor = YapinBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_username"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("CUSTOM STATUS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = customStatus,
                    onValueChange = { customStatus = it },
                    placeholder = { Text("What's on your mind? (e.g. Grinding Valorant 🎮)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YapinPurpleLight,
                        unfocusedBorderColor = YapinBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("ONLINE STATUS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserStatus.values().forEach { st ->
                        val isSelected = selectedStatus == st
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) YapinBorder else YapinInputBg)
                                .clickable { selectedStatus = st }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${st.emoji} ${st.label.take(6)}",
                                color = if (isSelected) TextWhite else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateProfile(username, customStatus.ifBlank { null }, selectedEmoji, selectedStatus)
                },
                colors = ButtonDefaults.buttonColors(containerColor = YapinPurple),
                modifier = Modifier.testTag("btn_save_profile")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun YapinSettingsDialog(
    userSettings: UserSettings,
    currentUser: User,
    onDismiss: () -> Unit,
    onSaveSettings: (UserSettings) -> Unit
) {
    var activeTab by remember { mutableStateOf("Voice & Audio") }
    var micSensitivity by remember { mutableStateOf(userSettings.micSensitivity) }
    var noiseSuppression by remember { mutableStateOf(userSettings.noiseSuppression) }
    var soundEffects by remember { mutableStateOf(userSettings.soundEffectsEnabled) }
    var notifications by remember { mutableStateOf(userSettings.notificationsEnabled) }

    // Simulated mic test bar animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_test")
    val meterLevel by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_meter"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YapinCardBg,
        title = {
            Text("User Settings", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Tab switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(YapinInputBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Voice & Audio", "Account", "Notifications").forEach { tab ->
                        val isSelected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) YapinPurple else Color.Transparent)
                                .clickable { activeTab = tab }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) TextWhite else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (activeTab) {
                    "Voice & Audio" -> {
                        Text("MIC TEST", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Having mic issues? Test your audio input sensitivity in real-time.", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Mic Meter Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(YapinInputBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = meterLevel * micSensitivity)
                                    .fillMaxHeight()
                                    .background(StatusOnline)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("INPUT SENSITIVITY: ${(micSensitivity * 100).toInt()}%", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = micSensitivity,
                            onValueChange = { micSensitivity = it },
                            colors = SliderDefaults.colors(
                                thumbColor = YapinPurpleLight,
                                activeTrackColor = YapinPurple,
                                inactiveTrackColor = YapinInputBg
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Krisp Noise Suppression", color = TextWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Removes background keyboard and fan noise", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = noiseSuppression,
                                onCheckedChange = { noiseSuppression = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = StatusOnline)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("In-App Audio Chimes", color = TextWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Play sound on message send and voice join", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = soundEffects,
                                onCheckedChange = { soundEffects = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = StatusOnline)
                            )
                        }
                    }

                    "Account" -> {
                        Text("ACCOUNT DETAILS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Username", color = TextSecondary, fontSize = 12.sp)
                        Text(currentUser.fullHandle, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Email", color = TextSecondary, fontSize = 12.sp)
                        Text("commander@yapin.gg", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Role & Permissions", color = TextSecondary, fontSize = 12.sp)
                        Text("Global Server Creator / Verified", color = StatusOnline, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    "Notifications" -> {
                        Text("NOTIFICATION PREFERENCES", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Push Notifications", color = TextWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Receive alerts for direct messages and mentions", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = notifications,
                                onCheckedChange = { notifications = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = StatusOnline)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveSettings(
                        userSettings.copy(
                            micSensitivity = micSensitivity,
                            noiseSuppression = noiseSuppression,
                            soundEffectsEnabled = soundEffects,
                            notificationsEnabled = notifications
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = YapinPurple),
                modifier = Modifier.testTag("btn_save_settings")
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}
