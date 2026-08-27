package com.samsul.inosoftapps

import android.app.Application
import com.samsul.inosoftapps.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

/**
 * Main Android Application class initializing Koin dependency injection with Android context.
 */
class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger(Level.INFO)
            androidContext(this@NewsApplication)
        }
    }
}
