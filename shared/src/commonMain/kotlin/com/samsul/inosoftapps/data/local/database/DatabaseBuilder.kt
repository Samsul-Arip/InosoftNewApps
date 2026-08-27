package com.samsul.inosoftapps.data.local.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Builds and initializes [NewsDatabase] with [BundledSQLiteDriver] and IO coroutine context.
 */
fun createNewsDatabase(builder: RoomDatabase.Builder<NewsDatabase>): NewsDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
