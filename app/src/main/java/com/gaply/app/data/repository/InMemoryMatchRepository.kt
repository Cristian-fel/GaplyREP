package com.gaply.app.data.repository

import com.gaply.app.domain.model.ChatMessage
import com.gaply.app.domain.model.ChatRoom
import com.gaply.app.domain.model.FeedCandidates
import com.gaply.app.domain.model.RepoResult
import com.gaply.app.domain.model.SwipeAction
import com.gaply.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Respaldo local del módulo Matches (AppContainer.USE_FIREBASE = false):
 * sin nube devuelve listas vacías, para probar la UI sin conexión.
 */
class InMemoryMatchRepository : MatchRepository {

    override suspend fun getCandidates(): RepoResult<FeedCandidates> =
        RepoResult.Success(FeedCandidates())

    override suspend fun registerSwipe(
        targetId: String,
        action: SwipeAction,
        isPerson: Boolean,
    ): RepoResult<String> = RepoResult.Success(targetId)

    override suspend fun getChats(): RepoResult<List<ChatRoom>> =
        RepoResult.Success(emptyList())

    override suspend fun syncGroupChat(
        activityId: String,
        groupTitle: String,
        joined: Boolean,
    ): RepoResult<String> = RepoResult.Success("act_$activityId")

    override fun observeMessages(chatId: String): Flow<List<ChatMessage>> =
        flowOf(emptyList())

    override suspend fun sendMessage(chatId: String, text: String): RepoResult<String> =
        RepoResult.Success(chatId)
}
