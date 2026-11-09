package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class YapinRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    // Current User
    private val _currentUser = MutableStateFlow(
        User(
            id = "user_me",
            username = "CommanderYap",
            tag = "1337",
            avatarEmoji = "⚡",
            avatarBgColorHex = 0xFF7C3AED,
            status = UserStatus.ONLINE,
            customStatus = "Grinding ranked 🎮 | Yapin Dev",
            role = UserRole.OWNER,
            isSelf = true
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Settings
    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    // Registered bot / demo users
    val demoUsers = listOf(
        User("user_valk", "NeonValkyrie", "9920", "🌸", 0xFFEC4899, UserStatus.ONLINE, "Listening to synthwave 🎧", UserRole.ADMIN),
        User("user_samurai", "CyberSamurai", "4040", "🗡️", 0xFF06B6D4, UserStatus.IDLE, "AFK for ramen", UserRole.MODERATOR),
        User("user_pixel", "PixelQueen", "7711", "👑", 0xFF8B5CF6, UserStatus.ONLINE, "Cooking clip montages 🎬", UserRole.MEMBER),
        User("user_lofi", "LoFiCoder", "1010", "☕", 0xFF10B981, UserStatus.DND, "Do not disturb | Deep code session", UserRole.MEMBER),
        User("user_bot", "YapinBot", "0000", "🤖", 0xFF3B82F6, UserStatus.ONLINE, "Type /help for Yapin commands", UserRole.BOT),
        User("user_ghost", "GhostRider", "2026", "💀", 0xFFEF4444, UserStatus.OFFLINE, null, UserRole.MEMBER),
        User("user_synth", "SynthWave", "8080", "🌊", 0xFFF59E0B, UserStatus.ONLINE, "Live streaming in #clips", UserRole.MEMBER)
    )

    // Servers
    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    // Discoverable Servers (to join)
    private val _discoverableServers = MutableStateFlow<List<Server>>(emptyList())
    val discoverableServers: StateFlow<List<Server>> = _discoverableServers.asStateFlow()

    // Friends
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    // Messages grouped by channelId or friendId
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Active Voice Room
    private val _activeVoiceRoom = MutableStateFlow<ActiveVoiceRoom?>(null)
    val activeVoiceRoom: StateFlow<ActiveVoiceRoom?> = _activeVoiceRoom.asStateFlow()

    // Typing indicators per channel/DM
    private val _typingUsers = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val typingUsers: StateFlow<Map<String, Set<String>>> = _typingUsers.asStateFlow()

    init {
        seedInitialData()
        startVoiceActivitySimulation()
    }

    private fun seedInitialData() {
        val s1Channels = listOf(
            Channel("c_announcements", "srv_yapin", "announcements", ChannelType.TEXT, "OFFICIAL", "Official updates and announcements from the YAPIN crew."),
            Channel("c_general", "srv_yapin", "general", ChannelType.TEXT, "TEXT CHANNELS", "The main lobby for everyone to yap and connect!"),
            Channel("c_feedback", "srv_yapin", "feedback-ideas", ChannelType.TEXT, "TEXT CHANNELS", "Share feature suggestions and Discord-grade ideas."),
            Channel("c_clips", "srv_yapin", "gameplay-clips", ChannelType.TEXT, "MEDIA & CREATIVE", "Post your legendary gamer moments and screenshots."),
            Channel("c_voice_stage", "srv_yapin", "Main Stage 🎙️", ChannelType.VOICE, "VOICE CHANNELS"),
            Channel("c_voice_lofi", "srv_yapin", "Lo-Fi Lounge ☕", ChannelType.VOICE, "VOICE CHANNELS"),
            Channel("c_voice_squad", "srv_yapin", "Squad Room #1 🎮", ChannelType.VOICE, "VOICE CHANNELS")
        )

        val s2Channels = listOf(
            Channel("c_cyber_chat", "srv_cyberpunk", "cyber-chat", ChannelType.TEXT, "GENERAL", "Night City gamers unite."),
            Channel("c_squad_finder", "srv_cyberpunk", "squad-finder", ChannelType.TEXT, "GENERAL", "LFG ranked Valorant & Apex."),
            Channel("c_cyber_voice", "srv_cyberpunk", "Comms Alpha 🎧", ChannelType.VOICE, "VOICE ROOMS")
        )

        val s3Channels = listOf(
            Channel("c_lofi_chat", "srv_lofi", "cafe-chat", ChannelType.TEXT, "LOUNGE", "Chill vibes only."),
            Channel("c_study_stream", "srv_lofi", "Deep Focus Room", ChannelType.VOICE, "AUDIO ROOMS")
        )

        val s4Channels = listOf(
            Channel("c_code_general", "srv_dev", "kotlin-compose", ChannelType.TEXT, "DEVELOPMENT", "Architecture, Compose UI, Jetpack libraries."),
            Channel("c_code_voice", "srv_dev", "Pair Coding 💻", ChannelType.VOICE, "DEV COMMS")
        )

        val initialServers = listOf(
            Server(
                id = "srv_yapin",
                name = "YAPIN Headquarters",
                iconEmoji = "⚡",
                iconBgColorHex = 0xFF7C3AED,
                description = "Official YAPIN community. Connect, Chat, and Create with creators and gamers worldwide.",
                bannerGradient = listOf(0xFF6D28D9, 0xFF1E1B4B),
                memberCount = 1420,
                onlineCount = 486,
                isJoined = true,
                isOwner = true,
                channels = s1Channels
            ),
            Server(
                id = "srv_cyberpunk",
                name = "Neon Cyberpunk Arena",
                iconEmoji = "👾",
                iconBgColorHex = 0xFF06B6D4,
                description = "High octane gaming community. Custom lobbies, tournaments, and squad voice comms.",
                bannerGradient = listOf(0xFF0891B2, 0xFF022C22),
                memberCount = 890,
                onlineCount = 312,
                isJoined = true,
                channels = s2Channels
            ),
            Server(
                id = "srv_lofi",
                name = "Lo-Fi Chill & Beats",
                iconEmoji = "☕",
                iconBgColorHex = 0xFFEC4899,
                description = "Relax, study, and listen to aesthetic lo-fi beats with fellow night owls.",
                bannerGradient = listOf(0xFFBE185D, 0xFF31102A),
                memberCount = 640,
                onlineCount = 190,
                isJoined = true,
                channels = s3Channels
            ),
            Server(
                id = "srv_dev",
                name = "Android & Kotlin Devs",
                iconEmoji = "💻",
                iconBgColorHex = 0xFF10B981,
                description = "Modern Android development with Jetpack Compose, Kotlin Coroutines, and clean architecture.",
                bannerGradient = listOf(0xFF047857, 0xFF064E3B),
                memberCount = 520,
                onlineCount = 145,
                isJoined = true,
                channels = s4Channels
            )
        )

        val discoverable = listOf(
            Server(
                id = "srv_anime",
                name = "Anime & Manga Oasis",
                iconEmoji = "🌸",
                iconBgColorHex = 0xFFF43F5E,
                description = "Seasonal anime discussions, manga recommendations, cosplay, and watch parties.",
                memberCount = 3400,
                onlineCount = 1120,
                isJoined = false,
                channels = listOf(Channel("c_anime_1", "srv_anime", "general", ChannelType.TEXT, "TEXT"))
            ),
            Server(
                id = "srv_music",
                name = "Electronic Beats & Synth",
                iconEmoji = "🎹",
                iconBgColorHex = 0xFF8B5CF6,
                description = "EDM, Synthwave, beatmakers, music production tips and sound design showcase.",
                memberCount = 2100,
                onlineCount = 740,
                isJoined = false,
                channels = listOf(Channel("c_music_1", "srv_music", "beats-lounge", ChannelType.TEXT, "TEXT"))
            ),
            Server(
                id = "srv_speedrun",
                name = "Speedrunners League",
                iconEmoji = "⏱️",
                iconBgColorHex = 0xFFEAB308,
                description = "World record attempts, routing guides, glitch discoveries, and live run voice rooms.",
                memberCount = 1150,
                onlineCount = 420,
                isJoined = false,
                channels = listOf(Channel("c_speed_1", "srv_speedrun", "pb-announcements", ChannelType.TEXT, "TEXT"))
            )
        )

        _servers.value = initialServers
        _discoverableServers.value = discoverable

        // Seed Friends
        _friends.value = listOf(
            Friend(demoUsers[0], lastMessage = "Let's hop into voice room soon! 🎧", lastActive = "Online"),
            Friend(demoUsers[1], lastMessage = "GG on that last match 🔥", lastActive = "5m ago"),
            Friend(demoUsers[2], lastMessage = "Did you check the new Yapin updates?", lastActive = "Online"),
            Friend(demoUsers[3], lastMessage = "Working on the Compose navigation stack.", lastActive = "1h ago"),
            Friend(demoUsers[4], lastMessage = "Welcome to YAPIN! Ready to roll.", lastActive = "Always Online"),
            Friend(
                user = User("user_pending1", "ShadowStriker", "5544", "🥷", 0xFF6366F1, UserStatus.ONLINE),
                isPending = true,
                isIncoming = true,
                lastActive = "Sent 10m ago"
            ),
            Friend(
                user = User("user_pending2", "SolarisX", "7890", "☀️", 0xFFF97316, UserStatus.OFFLINE),
                isPending = true,
                isIncoming = false,
                lastActive = "Sent yesterday"
            )
        )

        // Seed Messages in #general
        val timeNow = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        _messages.value = mapOf(
            "c_general" to listOf(
                Message(
                    id = "m1",
                    targetId = "c_general",
                    sender = demoUsers[4], // YapinBot
                    content = "🎉 Welcome to **YAPIN**! The premier high-performance communication platform for creators and gamers. Connect, Chat, Create!",
                    timestamp = "10:15 AM",
                    reactions = listOf(
                        MessageReaction("🚀", 14, setOf("user_valk", "user_me")),
                        MessageReaction("🔥", 9, setOf("user_pixel")),
                        MessageReaction("💜", 18, setOf("user_me"))
                    )
                ),
                Message(
                    id = "m2",
                    targetId = "c_general",
                    sender = demoUsers[0], // NeonValkyrie
                    content = "Just tested the low-latency voice room! The audio waveform feedback and instant mute controls are super smooth 🎙️✨",
                    timestamp = "10:22 AM",
                    reactions = listOf(
                        MessageReaction("👍", 8, setOf("user_samurai")),
                        MessageReaction("🎧", 6, setOf("user_me"))
                    )
                ),
                Message(
                    id = "m3",
                    targetId = "c_general",
                    sender = demoUsers[1], // CyberSamurai
                    content = "The dark charcoal UI with neon accents looks incredible. So much cleaner than standard chat apps.",
                    timestamp = "10:35 AM",
                    replyToId = "m2",
                    replyToUser = "NeonValkyrie",
                    replyToContent = "Just tested the low-latency voice room...",
                    reactions = listOf(
                        MessageReaction("💯", 11, setOf("user_me", "user_lofi"))
                    )
                ),
                Message(
                    id = "m4",
                    targetId = "c_general",
                    sender = demoUsers[2], // PixelQueen
                    content = "Who's ready for gaming night? We'll jump into the Squad Room voice channel in an hour! 🎮",
                    timestamp = "11:02 AM",
                    reactions = listOf(
                        MessageReaction("🎮", 7, setOf("user_me")),
                        MessageReaction("🙌", 5, setOf("user_valk"))
                    )
                )
            ),
            "c_announcements" to listOf(
                Message(
                    id = "m_a1",
                    targetId = "c_announcements",
                    sender = _currentUser.value,
                    content = "📢 **YAPIN 2.0 Live!**\n\n• Multi-panel desktop & tablet responsive layout\n• Real-time animated Voice Rooms with green speaking rings\n• Discord-style Server Discovery & Channel management\n• Direct Messaging & Friend Requests\n• Custom User Status & Theme Accents\n\nEnjoy the next level of community chat!",
                    timestamp = "Yesterday at 9:00 AM",
                    reactions = listOf(
                        MessageReaction("🎉", 42, setOf("user_me")),
                        MessageReaction("⚡", 35, setOf("user_valk")),
                        MessageReaction("❤️", 28, setOf("user_pixel"))
                    )
                )
            ),
            "user_valk" to listOf(
                Message(
                    id = "dm_1",
                    targetId = "user_valk",
                    sender = demoUsers[0],
                    content = "Hey Commander! Are we streaming the new tournament on YAPIN tonight?",
                    timestamp = "11:45 AM"
                ),
                Message(
                    id = "dm_2",
                    targetId = "user_valk",
                    sender = _currentUser.value,
                    content = "Definitely! Setting up the Main Stage voice channel right now.",
                    timestamp = "11:47 AM",
                    reactions = listOf(MessageReaction("🔥", 1, setOf("user_valk")))
                ),
                Message(
                    id = "dm_3",
                    targetId = "user_valk",
                    sender = demoUsers[0],
                    content = "Awesome, see you there! 🎧",
                    timestamp = "11:48 AM"
                )
            )
        )
    }

    // Voice Activity background simulation: updates participant speaking states and waveforms
    private fun startVoiceActivitySimulation() {
        scope.launch {
            while (true) {
                delay(800)
                _activeVoiceRoom.update { current ->
                    if (current == null) null
                    else {
                        val updatedParticipants = current.participants.map { participant ->
                            if (participant.user.isSelf) {
                                // Self speaking state is governed by mic status
                                participant.copy(
                                    isSpeaking = !current.isMicMuted && Math.random() > 0.4,
                                    audioLevel = if (!current.isMicMuted) (0.2f + (Math.random().toFloat() * 0.7f)) else 0f
                                )
                            } else {
                                // Other participants talk intermittently
                                val speaking = !participant.isMuted && Math.random() > 0.45
                                participant.copy(
                                    isSpeaking = speaking,
                                    audioLevel = if (speaking) (0.3f + (Math.random().toFloat() * 0.65f)) else 0f
                                )
                            }
                        }
                        current.copy(participants = updatedParticipants)
                    }
                }
            }
        }
    }

    // --- Message Actions ---

    fun sendMessage(
        targetId: String,
        content: String,
        replyTo: Message? = null,
        attachmentName: String? = null
    ) {
        if (content.isBlank() && attachmentName == null) return

        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val newMessage = Message(
            id = "msg_${UUID.randomUUID()}",
            targetId = targetId,
            sender = _currentUser.value,
            content = content.trim(),
            timestamp = timeStr,
            replyToId = replyTo?.id,
            replyToUser = replyTo?.sender?.username,
            replyToContent = replyTo?.content?.take(50),
            attachmentName = attachmentName
        )

        _messages.update { currentMap ->
            val list = currentMap[targetId].orEmpty() + newMessage
            currentMap + (targetId to list)
        }

        // Trigger bot / friend simulated reply after 1.5 seconds if talking to friends or in general
        simulateChatResponse(targetId, content)
    }

    private fun simulateChatResponse(targetId: String, userText: String) {
        scope.launch {
            delay(1200)
            // Show typing indicator
            val respondent = when (targetId) {
                "user_valk" -> demoUsers[0]
                "user_samurai" -> demoUsers[1]
                "user_pixel" -> demoUsers[2]
                "c_general" -> demoUsers[(0..3).random()]
                "c_feedback" -> demoUsers[4] // Bot
                else -> null
            } ?: return@launch

            _typingUsers.update { current ->
                val existing = current[targetId].orEmpty()
                current + (targetId to (existing + respondent.username))
            }

            delay(1500)

            _typingUsers.update { current ->
                val existing = current[targetId].orEmpty()
                current + (targetId to (existing - respondent.username))
            }

            val replyText = generateContextualReply(respondent, userText)
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            val replyMessage = Message(
                id = "msg_${UUID.randomUUID()}",
                targetId = targetId,
                sender = respondent,
                content = replyText,
                timestamp = timeStr,
                reactions = listOf(MessageReaction("👍", 1, setOf(respondent.id)))
            )

            _messages.update { currentMap ->
                val list = currentMap[targetId].orEmpty() + replyMessage
                currentMap + (targetId to list)
            }
        }
    }

    private fun generateContextualReply(user: User, prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            user.role == UserRole.BOT -> "🤖 [YapinBot]: Request received! Latency is 12ms. Server health is optimal at 99.99% uptime."
            lower.contains("hello") || lower.contains("hey") || lower.contains("hi") -> "Hey there ${currentUser.value.username}! How's your day going? 🎮"
            lower.contains("voice") || lower.contains("call") || lower.contains("mic") -> "I'm in the voice room right now, audio quality is crystal clear! 🔊"
            lower.contains("game") || lower.contains("play") -> "Count me in for the next round! Let me boot up my rig."
            lower.contains("cool") || lower.contains("nice") || lower.contains("awesome") -> "Right?! YAPIN feels so responsive and sleek."
            else -> listOf(
                "Totally agree with that! 💯",
                "That's awesome, appreciate you sharing!",
                "Check out the voice room whenever you get a minute!",
                "Let's goooo! 🔥🚀",
                "That clip was crazy, definitely clipping it."
            ).random()
        }
    }

    fun toggleReaction(targetId: String, messageId: String, emoji: String) {
        val myId = _currentUser.value.id
        _messages.update { currentMap ->
            val list = currentMap[targetId] ?: return@update currentMap
            val updated = list.map { msg ->
                if (msg.id != messageId) msg
                else {
                    val existing = msg.reactions.find { it.emoji == emoji }
                    val newReactions = if (existing == null) {
                        msg.reactions + MessageReaction(emoji, 1, setOf(myId))
                    } else if (existing.userIds.contains(myId)) {
                        if (existing.count <= 1) {
                            msg.reactions.filter { it.emoji != emoji }
                        } else {
                            msg.reactions.map {
                                if (it.emoji == emoji) it.copy(count = it.count - 1, userIds = it.userIds - myId)
                                else it
                            }
                        }
                    } else {
                        msg.reactions.map {
                            if (it.emoji == emoji) it.copy(count = it.count + 1, userIds = it.userIds + myId)
                            else it
                        }
                    }
                    msg.copy(reactions = newReactions)
                }
            }
            currentMap + (targetId to updated)
        }
    }

    fun editMessage(targetId: String, messageId: String, newContent: String) {
        _messages.update { currentMap ->
            val list = currentMap[targetId] ?: return@update currentMap
            val updated = list.map { msg ->
                if (msg.id == messageId) msg.copy(content = newContent, isEdited = true)
                else msg
            }
            currentMap + (targetId to updated)
        }
    }

    fun deleteMessage(targetId: String, messageId: String) {
        _messages.update { currentMap ->
            val list = currentMap[targetId] ?: return@update currentMap
            currentMap + (targetId to list.filterNot { it.id == messageId })
        }
    }

    // --- Server & Channel Management ---

    fun createServer(name: String, emoji: String, description: String) {
        val serverId = "srv_${UUID.randomUUID().toString().take(6)}"
        val defaultChannels = listOf(
            Channel("c_${UUID.randomUUID().toString().take(6)}", serverId, "general", ChannelType.TEXT, "TEXT CHANNELS", "General discussion for $name"),
            Channel("c_${UUID.randomUUID().toString().take(6)}", serverId, "Lounge 🎙️", ChannelType.VOICE, "VOICE CHANNELS")
        )
        val newServer = Server(
            id = serverId,
            name = name,
            iconEmoji = emoji.ifBlank { "🌟" },
            iconBgColorHex = listOf(0xFF7C3AED, 0xFF06B6D4, 0xFFEC4899, 0xFF10B981).random(),
            description = description,
            memberCount = 1,
            onlineCount = 1,
            isJoined = true,
            isOwner = true,
            channels = defaultChannels
        )
        _servers.update { it + newServer }
    }

    fun joinServer(server: Server) {
        _discoverableServers.update { list -> list.filterNot { it.id == server.id } }
        val joined = server.copy(isJoined = true, memberCount = server.memberCount + 1, onlineCount = server.onlineCount + 1)
        _servers.update { it + joined }
    }

    fun leaveServer(serverId: String) {
        val server = _servers.value.find { it.id == serverId } ?: return
        _servers.update { list -> list.filterNot { it.id == serverId } }
        _discoverableServers.update { it + server.copy(isJoined = false) }
    }

    fun createChannel(serverId: String, name: String, type: ChannelType, category: String) {
        val channelId = "c_${UUID.randomUUID().toString().take(6)}"
        val cleanName = if (type == ChannelType.TEXT) name.lowercase().replace(" ", "-") else name
        val newChannel = Channel(
            id = channelId,
            serverId = serverId,
            name = cleanName,
            type = type,
            category = category.uppercase(),
            topic = "Welcome to ${if (type == ChannelType.TEXT) "#" else "🔊 "}$cleanName!"
        )
        _servers.update { serverList ->
            serverList.map { srv ->
                if (srv.id == serverId) srv.copy(channels = srv.channels + newChannel)
                else srv
            }
        }
    }

    fun deleteChannel(serverId: String, channelId: String) {
        _servers.update { serverList ->
            serverList.map { srv ->
                if (srv.id == serverId) srv.copy(channels = srv.channels.filterNot { it.id == channelId })
                else srv
            }
        }
    }

    // --- Voice Room ---

    fun joinVoiceChannel(server: Server?, channel: Channel) {
        val myUser = _currentUser.value
        val initialParticipants = listOf(
            VoiceParticipant(myUser, isMuted = false, isDeafened = false, isSpeaking = true, audioLevel = 0.65f),
            VoiceParticipant(demoUsers[0], isMuted = false, isSpeaking = false, audioLevel = 0f),
            VoiceParticipant(demoUsers[1], isMuted = true, isSpeaking = false, audioLevel = 0f)
        )
        _activeVoiceRoom.value = ActiveVoiceRoom(
            server = server,
            channel = channel,
            participants = initialParticipants,
            isMicMuted = false,
            isDeafened = false
        )
    }

    fun leaveVoiceChannel() {
        _activeVoiceRoom.value = null
    }

    fun toggleVoiceMute() {
        _activeVoiceRoom.update { current ->
            if (current == null) null
            else {
                val newMuted = !current.isMicMuted
                val updatedParts = current.participants.map {
                    if (it.user.isSelf) it.copy(isMuted = newMuted, isSpeaking = false, audioLevel = 0f)
                    else it
                }
                current.copy(isMicMuted = newMuted, participants = updatedParts)
            }
        }
    }

    fun toggleVoiceDeafen() {
        _activeVoiceRoom.update { current ->
            if (current == null) null
            else {
                val newDeafened = !current.isDeafened
                val newMuted = if (newDeafened) true else current.isMicMuted
                val updatedParts = current.participants.map {
                    if (it.user.isSelf) it.copy(isDeafened = newDeafened, isMuted = newMuted)
                    else it
                }
                current.copy(isDeafened = newDeafened, isMicMuted = newMuted, participants = updatedParts)
            }
        }
    }

    fun toggleScreenShare() {
        _activeVoiceRoom.update { current ->
            current?.copy(isScreenSharing = !current.isScreenSharing)
        }
    }

    // --- Friends & Social ---

    fun sendFriendRequest(usernameWithTag: String): Boolean {
        val parts = usernameWithTag.split("#")
        val username = parts.getOrNull(0)?.trim() ?: return false
        val tag = parts.getOrNull(1)?.trim() ?: "0001"

        val newFriendUser = User(
            id = "user_${UUID.randomUUID().toString().take(6)}",
            username = username,
            tag = tag,
            avatarEmoji = listOf("🦊", "🦁", "🐼", "🚀", "🎮", "👾").random(),
            avatarBgColorHex = 0xFF6366F1,
            status = UserStatus.ONLINE
        )
        val friendEntry = Friend(
            user = newFriendUser,
            isPending = true,
            isIncoming = false,
            lastActive = "Sent just now"
        )
        _friends.update { it + friendEntry }
        return true
    }

    fun acceptFriendRequest(userId: String) {
        _friends.update { list ->
            list.map { friend ->
                if (friend.user.id == userId) friend.copy(isPending = false, isIncoming = false, lastMessage = "You are now friends on YAPIN! 🎉")
                else friend
            }
        }
    }

    fun rejectFriendRequest(userId: String) {
        _friends.update { list -> list.filterNot { it.user.id == userId } }
    }

    fun removeFriend(userId: String) {
        _friends.update { list -> list.filterNot { it.user.id == userId } }
    }

    // --- Profile & Settings ---

    fun updateProfile(
        username: String,
        customStatus: String?,
        avatarEmoji: String,
        status: UserStatus
    ) {
        _currentUser.update {
            it.copy(
                username = username.ifBlank { it.username },
                customStatus = customStatus,
                avatarEmoji = avatarEmoji,
                status = status
            )
        }
    }

    fun updateSettings(settings: UserSettings) {
        _userSettings.value = settings
        _currentUser.update {
            it.copy(
                username = settings.displayName,
                avatarEmoji = settings.avatarEmoji,
                avatarBgColorHex = settings.avatarBgColorHex
            )
        }
    }
}
