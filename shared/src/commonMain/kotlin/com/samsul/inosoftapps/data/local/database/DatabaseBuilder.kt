package com.samsul.inosoftapps.data.local.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Platform-specific Room database builder provider.
 * @param context Application context for Android, null for iOS.
 */
expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<NewsDatabase>

/**
 * Builds and initializes [NewsDatabase] with [BundledSQLiteDriver] and IO coroutine context.
 */
fun createNewsDatabase(builder: RoomDatabase.Builder<NewsDatabase>): NewsDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
