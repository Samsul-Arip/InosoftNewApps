package com.samsul.inosoftapps.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.samsul.inosoftapps.util.AppConstants

/**
 * Returns [RoomDatabase.Builder] for Android target using Application context.
 */
actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<NewsDatabase> {
    require(context is Context) { "Context must be provided for Android database builder" }
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(AppConstants.DATABASE_NAME)
    return Room.databaseBuilder<NewsDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
