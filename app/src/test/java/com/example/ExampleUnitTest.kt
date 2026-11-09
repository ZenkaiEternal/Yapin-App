package com.example

import com.example.data.model.ChannelType
import com.example.data.repository.YapinRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun repository_initial_state_and_chat_operations() {
        val repo = YapinRepository()
        val servers = repo.servers.value
        assertNotNull(servers)
        assertTrue(servers.isNotEmpty())

        val yapinServer = servers.find { it.id == "srv_yapin" }
        assertNotNull(yapinServer)

        // Test sending message
        repo.sendMessage("c_general", "Testing YAPIN in JUnit!")
        val generalMessages = repo.messages.value["c_general"]
        assertNotNull(generalMessages)
        assertTrue(generalMessages!!.any { it.content == "Testing YAPIN in JUnit!" })

        // Test Voice Channel Join & Leave
        val voiceChannel = yapinServer!!.channels.first { it.type == ChannelType.VOICE }
        repo.joinVoiceChannel(yapinServer, voiceChannel)
        assertNotNull(repo.activeVoiceRoom.value)
        assertEquals(voiceChannel.id, repo.activeVoiceRoom.value?.channel?.id)

        repo.toggleVoiceMute()
        assertTrue(repo.activeVoiceRoom.value?.isMicMuted == true)

        repo.leaveVoiceChannel()
        assertNull(repo.activeVoiceRoom.value)
    }
}
