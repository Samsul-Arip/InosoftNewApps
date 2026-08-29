package com.samsul.inosoftapps.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.samsul.inosoftapps.config.BuildKonfig
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Returns [RoomDatabase.Builder] for iOS target using NSDocumentDirectory.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<NewsDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val dbFilePath = requireNotNull(documentDirectory?.path) + "/" + BuildKonfig.DATABASE_NAME
    return Room.databaseBuilder<NewsDatabase>(
        name = dbFilePath
    )
}
