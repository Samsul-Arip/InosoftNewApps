package com.samsul.inosoftapps.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Returns [RoomDatabase.Builder] for Android target using Application context.
 */
fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<NewsDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("news_reader.db")
    return Room.databaseBuilder<NewsDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
