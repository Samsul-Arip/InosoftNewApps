package com.samsul.inosoftapps.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module (providing platform database builder, etc.).
 */
expect val platformModule: Module
