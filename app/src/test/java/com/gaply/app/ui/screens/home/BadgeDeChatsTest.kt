package com.gaply.app.ui.screens.home

import com.gaply.app.domain.model.Chat
import com.gaply.app.domain.model.ChatType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas del contador de la campana del Descubrir: mensajes sin leer más
 * matches directos a los que el usuario todavía no entró.
 */
class BadgeDeChatsTest {

    private fun chat(
        id: String,
        type: ChatType = ChatType.DIRECT,
        unread: Int = 0,
        lastReadAt: Long? = 1L,
    ) = Chat(
        id = id,
        type = type,
        title = "Persona $id",
        unreadCount = unread,
        lastReadAt = lastReadAt,
    )

    @Test
    fun `sin chats el badge queda en cero`() {
        assertEquals(0, badgeDeChats(emptyList()))
    }

    @Test
    fun `suma los mensajes sin leer de todos los chats`() {
        val chats = listOf(
            chat("a", unread = 3),
            chat("b", type = ChatType.GROUP, unread = 2),
        )

        assertEquals(5, badgeDeChats(chats))
    }

    @Test
    fun `un match directo que nunca abri cuenta uno`() {
        assertEquals(1, badgeDeChats(listOf(chat("a", lastReadAt = null))))
    }

    @Test
    fun `un match con mensajes sin leer no se cuenta dos veces`() {
        assertEquals(2, badgeDeChats(listOf(chat("a", unread = 2, lastReadAt = null))))
    }

    @Test
    fun `un match ya abierto sin mensajes no cuenta`() {
        assertEquals(0, badgeDeChats(listOf(chat("a", lastReadAt = 10L))))
    }

    @Test
    fun `los chats de grupo solo cuentan por no leidos`() {
        val grupo = chat("g", type = ChatType.GROUP, unread = 0, lastReadAt = null)

        assertEquals(0, badgeDeChats(listOf(grupo)))
    }
}
