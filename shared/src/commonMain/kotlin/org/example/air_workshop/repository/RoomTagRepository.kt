package org.example.air_workshop.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.air_workshop.database.dao.TagDao
import org.example.air_workshop.database.entities.TagEntity
import org.example.air_workshop.model.Tag

class RoomTagRepository(
    private val tagDao: TagDao
) : TagRepository {

    override fun observeAllTags(): Flow<List<Tag>> =
        tagDao.observeAllTags().map { entities -> entities.toDomainList() }

    override suspend fun getAllTags(): List<Tag> =
        tagDao.getAllTags().toDomainList()

    override suspend fun getTagById(id: Long): Tag? =
        tagDao.getTagById(id)?.toDomain()

    override suspend fun insertTag(tag: Tag): Long =
        tagDao.insertTag(tag.toEntity())

    override suspend fun updateTag(tag: Tag) =
        tagDao.updateTag(tag.toEntity())

    override suspend fun deleteTag(tag: Tag) =
        tagDao.deleteTag(tag.toEntity())

    override suspend fun deleteTagById(id: Long) =
        tagDao.deleteTagById(id)

    private fun Tag.toEntity(): TagEntity = TagEntity(
        id = id,
        name = name,
        colorHex = colorHex
    )

    private fun TagEntity.toDomain(): Tag = Tag(
        id = id,
        name = name,
        colorHex = colorHex
    )

    private fun List<TagEntity>.toDomainList(): List<Tag> =
        this.map { it.toDomain() }
}
