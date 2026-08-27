package com.samsul.inosoftapps.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.samsul.inosoftapps.data.local.dao.ArticleDao
import com.samsul.inosoftapps.data.local.entity.ArticleEntity

/**
 * Room Database for caching articles locally.
 * Uses `@ConstructedBy` to support Kotlin Multiplatform instantiation.
 */
@Database(
    entities = [ArticleEntity::class],
    version = 1,
    exportSchema = false
)
@ConstructedBy(NewsDatabaseConstructor::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}

/**
 * Room constructor required for Multiplatform code generation.
 * The Room KSP compiler generates the `actual` implementation.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object NewsDatabaseConstructor : RoomDatabaseConstructor<NewsDatabase> {
    override fun initialize(): NewsDatabase
}
