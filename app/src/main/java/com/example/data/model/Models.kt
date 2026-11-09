package com.example.data.model

enum class UserStatus(val label: String, val emoji: String) {
    ONLINE("Online", "🟢"),
    IDLE("Idle", "🟡"),
    DND("Do Not Disturb", "🔴"),
    OFFLINE("Invisible / Offline", "⚪")
}

enum class UserRole(val title: String, val badgeColorHex: Long) {
    OWNER("Server Owner 👑", 0xFFFFD700),
    ADMIN("Administrator ⚡", 0xFFEC4899),
    MODERATOR("Moderator 🛡️", 0xFF06B6D4),
    MEMBER("Member", 0xFF9CA3AF),
    BOT("BOT 🤖", 0xFF8B5CF6)
}

data class User(
    val id: String,
    val username: String,
    val tag: String = "0001",
    val avatarEmoji: String = "👾",
    val avatarBgColorHex: Long = 0xFF7C3AED,
    val status: UserStatus = UserStatus.ONLINE,
    val customStatus: String? = null,
    val role: UserRole = UserRole.MEMBER,
    val isSelf: Boolean = false
) {
    val fullHandle: String get() = "$username#$tag"
}

enum class ChannelType {
    TEXT,
    VOICE
}

data class Channel(
    val id: String,
    val serverId: String,
    val name: String,
    val type: ChannelType = ChannelType.TEXT,
    val category: String = "TEXT CHANNELS",
    val topic: String = "Welcome to #$name!",
    val isUnread: Boolean = false
)

data class Server(
    val id: String,
    val name: String,
    val iconEmoji: String = "🎮",
    val iconBgColorHex: Long = 0xFF6D28D9,
    val description: String = "",
    val bannerGradient: List<Long> = listOf(0xFF4C1D95, 0xFF1E1B4B),
    val memberCount: Int = 128,
    val onlineCount: Int = 42,
    val isJoined: Boolean = true,
    val isOwner: Boolean = false,
    val channels: List<Channel> = emptyList()
)

data class MessageReaction(
    val emoji: String,
    val count: Int,
    val userIds: Set<String> = emptySet()
)

data class Message(
    val id: String,
    val targetId: String, // channelId or dmFriendId
    val sender: User,
    val content: String,
    val timestamp: String,
    val isEdited: Boolean = false,
    val replyToId: String? = null,
    val replyToUser: String? = null,
    val replyToContent: String? = null,
    val reactions: List<MessageReaction> = emptyList(),
    val attachmentUrl: String? = null,
    val attachmentName: String? = null
)

data class VoiceParticipant(
    val user: User,
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val isSpeaking: Boolean = false,
    val audioLevel: Float = 0f
)

data class ActiveVoiceRoom(
    val server: Server?,
    val channel: Channel,
    val connectedAt: Long = System.currentTimeMillis(),
    val participants: List<VoiceParticipant> = emptyList(),
    val isMicMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val isScreenSharing: Boolean = false
)

enum class FriendStatus {
    ONLINE,
    ALL,
    PENDING,
    BLOCKED
}

data class Friend(
    val user: User,
    val isPending: Boolean = false,
    val isIncoming: Boolean = false,
    val isBlocked: Boolean = false,
    val lastMessage: String? = null,
    val lastActive: String = "Just now"
)

data class UserSettings(
    val displayName: String = "CommanderYap",
    val tag: String = "1337",
    val bio: String = "Building high-performance communities on YAPIN.",
    val avatarEmoji: String = "⚡",
    val avatarBgColorHex: Long = 0xFF7C3AED,
    val themeAccent: String = "Electric Purple",
    val micMuted: Boolean = false,
    val deafened: Boolean = false,
    val noiseSuppression: Boolean = true,
    val micSensitivity: Float = 0.75f,
    val notificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val allowDMsFromServerMembers: Boolean = true
)
