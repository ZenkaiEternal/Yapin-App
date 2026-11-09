package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.User
import com.example.ui.MainNavMode
import com.example.ui.YapinViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YapinTheme {
                YapinApp()
            }
        }
    }
}

@Composable
fun YapinApp(
    viewModel: YapinViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val discoverableServers by viewModel.discoverableServers.collectAsStateWithLifecycle()
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val currentServer by viewModel.currentServer.collectAsStateWithLifecycle()
    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val activeVoiceRoom by viewModel.activeVoiceRoom.collectAsStateWithLifecycle()
    val typingUsersMap by viewModel.typingUsers.collectAsStateWithLifecycle()

    var isSidebarOpenOnMobile by remember { mutableStateOf(false) }

    // Derive active target ID for typing indicators
    val activeTargetId = if (uiState.currentMode == MainNavMode.DIRECT_MESSAGES && uiState.selectedDmUserId != null) {
        uiState.selectedDmUserId!!
    } else {
        uiState.selectedChannelId
    }
    val currentTypingUsers = typingUsersMap[activeTargetId].orEmpty()

    // Find DM target user if in DM mode
    val activeDmUser = if (uiState.currentMode == MainNavMode.DIRECT_MESSAGES && uiState.selectedDmUserId != null) {
        friends.find { it.user.id == uiState.selectedDmUserId }?.user
    } else null

    // Back button handling
    BackHandler(
        enabled = uiState.isVoiceOverlayExpanded || uiState.isMembersDrawerOpen || isSidebarOpenOnMobile || uiState.currentMode != MainNavMode.SERVERS
    ) {
        when {
            uiState.isVoiceOverlayExpanded -> viewModel.setVoiceOverlayExpanded(false)
            uiState.isMembersDrawerOpen -> viewModel.setMembersDrawerOpen(false)
            isSidebarOpenOnMobile -> isSidebarOpenOnMobile = false
            uiState.currentMode != MainNavMode.SERVERS -> viewModel.selectServer(servers.firstOrNull()?.id ?: "srv_yapin")
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(YapinRailBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val isWideScreen = maxWidth >= 800.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Server Rail (always visible on left)
            YapinServerRail(
                servers = servers,
                selectedServerId = uiState.selectedServerId,
                currentMode = uiState.currentMode,
                activeVoiceRoom = activeVoiceRoom,
                onSelectHome = {
                    viewModel.openDirectMessages()
                    if (!isWideScreen) isSidebarOpenOnMobile = true
                },
                onSelectFriends = {
                    viewModel.openFriendsHub()
                    if (!isWideScreen) isSidebarOpenOnMobile = false
                },
                onSelectServer = { serverId ->
                    viewModel.selectServer(serverId)
                    if (!isWideScreen) isSidebarOpenOnMobile = false
                },
                onOpenDiscover = {
                    viewModel.openDiscover()
                    if (!isWideScreen) isSidebarOpenOnMobile = false
                },
                onOpenAddServer = { viewModel.setCreateServerDialogOpen(true) },
                onOpenVoiceOverlay = { viewModel.setVoiceOverlayExpanded(true) }
            )

            // On Wide screens: Middle Channel Sidebar is side-by-side
            if (isWideScreen && uiState.currentMode != MainNavMode.DISCOVER && uiState.currentMode != MainNavMode.FRIENDS) {
                YapinChannelSidebar(
                    server = currentServer,
                    currentMode = uiState.currentMode,
                    selectedChannelId = uiState.selectedChannelId,
                    selectedDmUserId = uiState.selectedDmUserId,
                    friends = friends,
                    currentUser = currentUser,
                    activeVoiceRoom = activeVoiceRoom,
                    onSelectChannel = { channel -> viewModel.selectChannel(channel) },
                    onSelectDmUser = { userId -> viewModel.selectDmUser(userId) },
                    onOpenFriendsHub = { viewModel.openFriendsHub() },
                    onOpenAddChannel = { viewModel.setCreateChannelDialogOpen(true) },
                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                    onOpenUserProfile = { viewModel.setUserProfileModalOpen(true) },
                    onToggleMic = { viewModel.toggleVoiceMute() },
                    onToggleDeafen = { viewModel.toggleVoiceDeafen() },
                    onLeaveServer = { serverId -> viewModel.leaveServer(serverId) },
                    modifier = Modifier.width(240.dp)
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (uiState.currentMode) {
                    MainNavMode.DISCOVER -> {
                        YapinDiscoverScreen(
                            discoverableServers = discoverableServers,
                            onJoinServer = { server -> viewModel.joinServer(server) },
                            onToggleSidebar = { isSidebarOpenOnMobile = !isSidebarOpenOnMobile }
                        )
                    }

                    MainNavMode.FRIENDS -> {
                        YapinFriendsScreen(
                            friends = friends,
                            currentTab = uiState.friendsTab,
                            onTabSelected = { tab -> viewModel.setFriendsTab(tab) },
                            onOpenDm = { userId -> viewModel.selectDmUser(userId) },
                            onSendFriendRequest = { handle -> viewModel.sendFriendRequest(handle) },
                            onAcceptRequest = { userId -> viewModel.acceptFriend(userId) },
                            onRejectRequest = { userId -> viewModel.rejectFriend(userId) },
                            onRemoveFriend = { userId -> viewModel.removeFriend(userId) },
                            onToggleSidebar = { isSidebarOpenOnMobile = !isSidebarOpenOnMobile }
                        )
                    }

                    MainNavMode.SERVERS, MainNavMode.DIRECT_MESSAGES -> {
                        YapinChatArea(
                            channel = if (uiState.currentMode == MainNavMode.SERVERS) currentChannel else null,
                            dmUser = activeDmUser,
                            messages = activeMessages,
                            typingUsers = currentTypingUsers,
                            replyingTo = uiState.replyingToMessage,
                            editingMessage = uiState.editingMessage,
                            activeVoiceRoom = activeVoiceRoom,
                            onToggleSidebar = { isSidebarOpenOnMobile = !isSidebarOpenOnMobile },
                            onToggleMembers = { viewModel.setMembersDrawerOpen(!uiState.isMembersDrawerOpen) },
                            onSendMessage = { text, attachment -> viewModel.sendMessage(text, attachment) },
                            onToggleReaction = { msg, emoji -> viewModel.toggleReaction(msg, emoji) },
                            onReplyTo = { msg -> viewModel.setReplyingTo(msg) },
                            onEditMessage = { msg -> viewModel.setEditing(msg) },
                            onDeleteMessage = { msg -> viewModel.deleteMessage(msg) },
                            onCancelReplyOrEdit = {
                                viewModel.setReplyingTo(null)
                                viewModel.setEditing(null)
                            },
                            onExpandVoiceOverlay = { viewModel.setVoiceOverlayExpanded(true) }
                        )
                    }
                }
            }
        }

        // Mobile Sliding Drawer for Channel Sidebar (Layered above content)
        if (!isWideScreen) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isSidebarOpenOnMobile,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 72.dp) // Starts right after server rail
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    YapinChannelSidebar(
                        server = currentServer,
                        currentMode = uiState.currentMode,
                        selectedChannelId = uiState.selectedChannelId,
                        selectedDmUserId = uiState.selectedDmUserId,
                        friends = friends,
                        currentUser = currentUser,
                        activeVoiceRoom = activeVoiceRoom,
                        onSelectChannel = { channel ->
                            viewModel.selectChannel(channel)
                            isSidebarOpenOnMobile = false
                        },
                        onSelectDmUser = { userId ->
                            viewModel.selectDmUser(userId)
                            isSidebarOpenOnMobile = false
                        },
                        onOpenFriendsHub = {
                            viewModel.openFriendsHub()
                            isSidebarOpenOnMobile = false
                        },
                        onOpenAddChannel = { viewModel.setCreateChannelDialogOpen(true) },
                        onOpenSettings = { viewModel.setSettingsOpen(true) },
                        onOpenUserProfile = { viewModel.setUserProfileModalOpen(true) },
                        onToggleMic = { viewModel.toggleVoiceMute() },
                        onToggleDeafen = { viewModel.toggleVoiceDeafen() },
                        onLeaveServer = { serverId ->
                            viewModel.leaveServer(serverId)
                            isSidebarOpenOnMobile = false
                        },
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                    )

                    // Scrim overlay to close on click outside
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { isSidebarOpenOnMobile = false }
                    )
                }
            }
        }

        // Right Members Drawer
        androidx.compose.animation.AnimatedVisibility(
            visible = uiState.isMembersDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val allMembers = buildList {
                add(currentUser)
                addAll(viewModel.repositoryMembers)
            }
            YapinMembersDrawer(
                members = allMembers,
                onClose = { viewModel.setMembersDrawerOpen(false) },
                onOpenDmWithUser = { userId ->
                    viewModel.selectDmUser(userId)
                    viewModel.setMembersDrawerOpen(false)
                },
                onAddFriend = { handle -> viewModel.sendFriendRequest(handle) }
            )
        }

        // Full Voice Overlay (when expanded)
        activeVoiceRoom?.let { room ->
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.isVoiceOverlayExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                YapinVoiceOverlay(
                    voiceRoom = room,
                    onCollapse = { viewModel.setVoiceOverlayExpanded(false) },
                    onToggleMute = { viewModel.toggleVoiceMute() },
                    onToggleDeafen = { viewModel.toggleVoiceDeafen() },
                    onToggleScreenShare = { viewModel.toggleScreenShare() },
                    onDisconnect = { viewModel.leaveVoiceChannel() }
                )
            }
        }

        // Feedback Toast Snackbar
        uiState.toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E2130))
                    .border(1.dp, YapinPurpleLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(text = msg, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Modals & Dialogs
    if (uiState.isCreateServerDialogOpen) {
        CreateServerDialog(
            onDismiss = { viewModel.setCreateServerDialogOpen(false) },
            onCreateServer = { name, emoji, desc -> viewModel.createServer(name, emoji, desc) }
        )
    }

    if (uiState.isCreateChannelDialogOpen) {
        CreateChannelDialog(
            categories = listOf("TEXT CHANNELS", "VOICE CHANNELS", "GENERAL", "COMMUNITY"),
            onDismiss = { viewModel.setCreateChannelDialogOpen(false) },
            onCreateChannel = { name, type, cat -> viewModel.createChannel(name, type, cat) }
        )
    }

    if (uiState.isUserProfileModalOpen) {
        UserProfileModal(
            currentUser = currentUser,
            onDismiss = { viewModel.setUserProfileModalOpen(false) },
            onUpdateProfile = { name, status, emoji, st -> viewModel.updateProfile(name, status, emoji, st) }
        )
    }

    if (uiState.isSettingsOpen) {
        YapinSettingsDialog(
            userSettings = userSettings,
            currentUser = currentUser,
            onDismiss = { viewModel.setSettingsOpen(false) },
            onSaveSettings = { newSettings -> viewModel.updateSettings(newSettings) }
        )
    }
}

// Extension to expose demo members safely to Compose
val YapinViewModel.repositoryMembers: List<User>
    get() = (this as Any).let {
        listOf(
            User("user_valk", "NeonValkyrie", "9920", "🌸", 0xFFEC4899, com.example.data.model.UserStatus.ONLINE, "Listening to synthwave 🎧", com.example.data.model.UserRole.ADMIN),
            User("user_samurai", "CyberSamurai", "4040", "🗡️", 0xFF06B6D4, com.example.data.model.UserStatus.IDLE, "AFK for ramen", com.example.data.model.UserRole.MODERATOR),
            User("user_pixel", "PixelQueen", "7711", "👑", 0xFF8B5CF6, com.example.data.model.UserStatus.ONLINE, "Cooking clip montages 🎬", com.example.data.model.UserRole.MEMBER),
            User("user_lofi", "LoFiCoder", "1010", "☕", 0xFF10B981, com.example.data.model.UserStatus.DND, "Deep code session", com.example.data.model.UserRole.MEMBER),
            User("user_bot", "YapinBot", "0000", "🤖", 0xFF3B82F6, com.example.data.model.UserStatus.ONLINE, "Type /help for commands", com.example.data.model.UserRole.BOT),
            User("user_ghost", "GhostRider", "2026", "💀", 0xFFEF4444, com.example.data.model.UserStatus.OFFLINE, null, com.example.data.model.UserRole.MEMBER)
        )
    }
