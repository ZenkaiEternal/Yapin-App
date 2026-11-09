package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.YapinRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainNavMode {
    SERVERS,
    DIRECT_MESSAGES,
    DISCOVER,
    FRIENDS
}

data class YapinUiState(
    val currentMode: MainNavMode = MainNavMode.SERVERS,
    val selectedServerId: String = "srv_yapin",
    val selectedChannelId: String = "c_general",
    val selectedDmUserId: String? = null,
    val friendsTab: FriendStatus = FriendStatus.ONLINE,
    val isCreateServerDialogOpen: Boolean = false,
    val isCreateChannelDialogOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isMembersDrawerOpen: Boolean = false,
    val isUserProfileModalOpen: Boolean = false,
    val isVoiceOverlayExpanded: Boolean = false,
    val replyingToMessage: Message? = null,
    val editingMessage: Message? = null,
    val toastMessage: String? = null
)

class YapinViewModel(
    private val repository: YapinRepository = YapinRepository()
) : ViewModel() {

    val currentUser = repository.currentUser
    val userSettings = repository.userSettings
    val servers = repository.servers
    val discoverableServers = repository.discoverableServers
    val friends = repository.friends
    val messages = repository.messages
    val activeVoiceRoom = repository.activeVoiceRoom
    val typingUsers = repository.typingUsers

    private val _uiState = MutableStateFlow(YapinUiState())
    val uiState: StateFlow<YapinUiState> = _uiState.asStateFlow()

    // Current active server
    val currentServer: StateFlow<Server?> = combine(servers, _uiState) { sList, state ->
        sList.find { it.id == state.selectedServerId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current active channel
    val currentChannel: StateFlow<Channel?> = combine(currentServer, _uiState) { srv, state ->
        srv?.channels?.find { it.id == state.selectedChannelId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current active messages
    val activeMessages: StateFlow<List<Message>> = combine(
        messages,
        _uiState
    ) { msgMap, state ->
        val targetId = if (state.currentMode == MainNavMode.DIRECT_MESSAGES && state.selectedDmUserId != null) {
            state.selectedDmUserId
        } else {
            state.selectedChannelId
        }
        msgMap[targetId].orEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectServer(serverId: String) {
        val srv = servers.value.find { it.id == serverId }
        val defaultChannel = srv?.channels?.firstOrNull { it.type == ChannelType.TEXT }?.id
            ?: srv?.channels?.firstOrNull()?.id ?: ""

        _uiState.update {
            it.copy(
                currentMode = MainNavMode.SERVERS,
                selectedServerId = serverId,
                selectedChannelId = defaultChannel,
                selectedDmUserId = null,
                replyingToMessage = null,
                editingMessage = null
            )
        }
    }

    fun selectChannel(channel: Channel) {
        if (channel.type == ChannelType.VOICE) {
            // Join voice channel directly
            joinVoiceChannel(currentServer.value, channel)
            _uiState.update { it.copy(isVoiceOverlayExpanded = true) }
        } else {
            _uiState.update {
                it.copy(
                    selectedChannelId = channel.id,
                    replyingToMessage = null,
                    editingMessage = null
                )
            }
        }
    }

    fun openDirectMessages() {
        val firstFriend = friends.value.firstOrNull { !it.isPending }?.user?.id
        _uiState.update {
            it.copy(
                currentMode = MainNavMode.DIRECT_MESSAGES,
                selectedDmUserId = firstFriend,
                replyingToMessage = null,
                editingMessage = null
            )
        }
    }

    fun openFriendsHub() {
        _uiState.update {
            it.copy(
                currentMode = MainNavMode.FRIENDS,
                replyingToMessage = null,
                editingMessage = null
            )
        }
    }

    fun selectDmUser(userId: String) {
        _uiState.update {
            it.copy(
                currentMode = MainNavMode.DIRECT_MESSAGES,
                selectedDmUserId = userId,
                replyingToMessage = null,
                editingMessage = null
            )
        }
    }

    fun openDiscover() {
        _uiState.update {
            it.copy(
                currentMode = MainNavMode.DISCOVER
            )
        }
    }

    fun setFriendsTab(tab: FriendStatus) {
        _uiState.update { it.copy(friendsTab = tab) }
    }

    // Messaging actions
    fun sendMessage(content: String, attachmentName: String? = null) {
        val state = _uiState.value
        val targetId = if (state.currentMode == MainNavMode.DIRECT_MESSAGES && state.selectedDmUserId != null) {
            state.selectedDmUserId
        } else {
            state.selectedChannelId
        }

        if (state.editingMessage != null) {
            repository.editMessage(targetId, state.editingMessage.id, content)
            _uiState.update { it.copy(editingMessage = null) }
        } else {
            repository.sendMessage(targetId, content, state.replyingToMessage, attachmentName)
            _uiState.update { it.copy(replyingToMessage = null) }
        }
    }

    fun setReplyingTo(message: Message?) {
        _uiState.update { it.copy(replyingToMessage = message, editingMessage = null) }
    }

    fun setEditing(message: Message?) {
        _uiState.update { it.copy(editingMessage = message, replyingToMessage = null) }
    }

    fun toggleReaction(message: Message, emoji: String) {
        val targetId = if (_uiState.value.currentMode == MainNavMode.DIRECT_MESSAGES && _uiState.value.selectedDmUserId != null) {
            _uiState.value.selectedDmUserId!!
        } else {
            _uiState.value.selectedChannelId
        }
        repository.toggleReaction(targetId, message.id, emoji)
    }

    fun deleteMessage(message: Message) {
        val targetId = if (_uiState.value.currentMode == MainNavMode.DIRECT_MESSAGES && _uiState.value.selectedDmUserId != null) {
            _uiState.value.selectedDmUserId!!
        } else {
            _uiState.value.selectedChannelId
        }
        repository.deleteMessage(targetId, message.id)
    }

    // Voice actions
    fun joinVoiceChannel(server: Server?, channel: Channel) {
        repository.joinVoiceChannel(server, channel)
    }

    fun leaveVoiceChannel() {
        repository.leaveVoiceChannel()
        _uiState.update { it.copy(isVoiceOverlayExpanded = false) }
    }

    fun toggleVoiceMute() = repository.toggleVoiceMute()
    fun toggleVoiceDeafen() = repository.toggleVoiceDeafen()
    fun toggleScreenShare() = repository.toggleScreenShare()

    fun setVoiceOverlayExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isVoiceOverlayExpanded = expanded) }
    }

    // Server actions
    fun createServer(name: String, emoji: String, description: String) {
        repository.createServer(name, emoji, description)
        _uiState.update { it.copy(isCreateServerDialogOpen = false) }
        showToast("Server '$name' created!")
    }

    fun joinServer(server: Server) {
        repository.joinServer(server)
        selectServer(server.id)
        showToast("Joined ${server.name}!")
    }

    fun leaveServer(serverId: String) {
        repository.leaveServer(serverId)
        val nextServer = servers.value.firstOrNull { it.id != serverId }
        if (nextServer != null) {
            selectServer(nextServer.id)
        } else {
            openDirectMessages()
        }
        showToast("Left server")
    }

    fun createChannel(name: String, type: ChannelType, category: String) {
        val serverId = _uiState.value.selectedServerId
        repository.createChannel(serverId, name, type, category)
        _uiState.update { it.copy(isCreateChannelDialogOpen = false) }
        showToast("Channel #$name created!")
    }

    // Friends actions
    fun sendFriendRequest(usernameWithTag: String) {
        val success = repository.sendFriendRequest(usernameWithTag)
        if (success) {
            showToast("Friend request sent to $usernameWithTag")
        }
    }

    fun acceptFriend(userId: String) {
        repository.acceptFriendRequest(userId)
        showToast("Friend request accepted!")
    }

    fun rejectFriend(userId: String) {
        repository.rejectFriendRequest(userId)
        showToast("Friend request rejected")
    }

    fun removeFriend(userId: String) {
        repository.removeFriend(userId)
        showToast("Friend removed")
    }

    // Dialog toggles
    fun setCreateServerDialogOpen(open: Boolean) = _uiState.update { it.copy(isCreateServerDialogOpen = open) }
    fun setCreateChannelDialogOpen(open: Boolean) = _uiState.update { it.copy(isCreateChannelDialogOpen = open) }
    fun setSettingsOpen(open: Boolean) = _uiState.update { it.copy(isSettingsOpen = open) }
    fun setMembersDrawerOpen(open: Boolean) = _uiState.update { it.copy(isMembersDrawerOpen = open) }
    fun setUserProfileModalOpen(open: Boolean) = _uiState.update { it.copy(isUserProfileModalOpen = open) }

    fun updateProfile(username: String, customStatus: String?, avatarEmoji: String, status: UserStatus) {
        repository.updateProfile(username, customStatus, avatarEmoji, status)
        _uiState.update { it.copy(isUserProfileModalOpen = false) }
        showToast("Profile updated!")
    }

    fun updateSettings(settings: UserSettings) {
        repository.updateSettings(settings)
        _uiState.update { it.copy(isSettingsOpen = false) }
        showToast("Settings saved!")
    }

    private fun showToast(msg: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(toastMessage = msg) }
            kotlinx.coroutines.delay(2500)
            _uiState.update { if (it.toastMessage == msg) it.copy(toastMessage = null) else it }
        }
    }
}
