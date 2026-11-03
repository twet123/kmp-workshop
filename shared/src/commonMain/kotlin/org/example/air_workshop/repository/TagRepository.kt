package org.example.air_workshop.repository

import kotlinx.coroutines.flow.Flow
import org.example.air_workshop.model.Tag

interface TagRepository {
    fun observeAllTags(): Flow<List<Tag>>
    suspend fun getAllTags(): List<Tag>
    suspend fun getTagById(id: Long): Tag?
    suspend fun insertTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
    suspend fun deleteTagById(id: Long)
}
