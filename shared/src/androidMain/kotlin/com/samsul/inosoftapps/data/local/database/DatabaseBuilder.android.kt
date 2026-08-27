package com.samsul.inosoftapps.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Returns [RoomDatabase.Builder] for Android target using Application context.
 */
actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<NewsDatabase> {
    require(context is Context) { "Context must be provided for Android database builder" }
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("news_reader.db")
    return Room.databaseBuilder<NewsDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
