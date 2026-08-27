package com.samsul.inosoftapps

import androidx.compose.ui.window.ComposeUIViewController
import com.samsul.inosoftapps.di.initKoinIos

private var isKoinInitialized = false

/**
 * Initializes Koin once on iOS.
 */
fun initKoin() {
    if (!isKoinInitialized) {
        initKoinIos()
        isKoinInitialized = true
    }
}

/**
 * UIViewController entry point for iOS application.
 * Automatically initializes Koin before rendering the Compose UI.
 */
fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}