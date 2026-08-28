import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Build script configuration constants
val propNewsApiKey = "NEWS_API_KEY"
val propNewsBaseUrl = "NEWS_BASE_URL"
val defaultNewsBaseUrl = "DEFAULT_BASE_URL_HERE"
val defaultApiKeyFallback = "API_KEY_HERE"
val defaultCountryCode = "id"
val taskGenerateBuildConfig = "generateBuildConfig"
val iosFrameworkName = "Shared"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
}

// Load dynamic secrets and environment configuration from local.properties or system env
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val newsApiKey: String = (localProperties.getProperty(propNewsApiKey)
    ?: System.getenv(propNewsApiKey)
    ?: defaultApiKeyFallback).trim()

val newsBaseUrl: String = (localProperties.getProperty(propNewsBaseUrl)
    ?: System.getenv(propNewsBaseUrl)
    ?: defaultNewsBaseUrl).trim()

val generatedBuildConfigDir = layout.buildDirectory.dir("generated/source/buildConfig/commonMain/kotlin")

abstract class GenerateBuildConfigTask : DefaultTask() {
    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    abstract val baseUrl: Property<String>

    @get:Input
    abstract val defaultCountry: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        val targetFile = dir.resolve("com/samsul/inosoftapps/config/BuildKonfig.kt")
        targetFile.parentFile.mkdirs()
        targetFile.writeText(
            """
            package com.samsul.inosoftapps.config

            /**
             * Auto-generated build configuration constants from local.properties / environment variables.
             * DO NOT EDIT DIRECTLY.
             */
            object BuildKonfig {
                const val BASE_URL: String = "${baseUrl.get()}"
                const val API_KEY: String = "${apiKey.get()}"
                const val DEFAULT_COUNTRY: String = "${defaultCountry.get()}"
            }
            """.trimIndent()
        )
    }
}

val generateBuildConfigTask = tasks.register<GenerateBuildConfigTask>(taskGenerateBuildConfig) {
    apiKey.set(newsApiKey)
    baseUrl.set(newsBaseUrl)
    defaultCountry.set(defaultCountryCode)
    outputDir.set(generatedBuildConfigDir)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = iosFrameworkName
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedBuildConfigDir)

            dependencies {
                // Compose Multiplatform
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)

                // Lifecycle & Navigation
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.androidx.navigation.compose)

                // Coroutines, Serialization & Datetime
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                // Ktor Client 3.x
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                // Room KMP 2.7.x
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)

                // Koin DI
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.viewmodel.navigation)

                // Coil 3 (Image Loader)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor3)
            }
        }

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
            implementation(libs.koin.test)
            implementation(libs.compose.ui.test)
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.kotlin.testJunit)
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.espresso.core)
        }
    }
}

// Ensure generateBuildConfig runs before any compilation or KSP tasks
tasks.matching {
    it.name.startsWith("compile") || it.name.startsWith("ksp") || it.name.startsWith("generate")
}.configureEach {
    if (name != taskGenerateBuildConfig) {
        dependsOn(generateBuildConfigTask)
    }
}

android {
    namespace = "com.samsul.inosoftapps.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}