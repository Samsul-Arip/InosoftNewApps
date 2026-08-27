package com.samsul.inosoftapps.util

/**
 * Centralized string resources for titles, actions, placeholders, status banners, and error messages.
 * Prevents magic strings and facilitates localization / test consistency.
 */
object AppStrings {

    // App Titles & Headers
    const val APP_NAME = "News Reader"
    const val ARTICLE_DETAIL_TITLE = "Detail Berita"

    // Search & Actions
    const val SEARCH_PLACEHOLDER = "Cari berita dari cache..."
    const val SEARCH_BUTTON_DESC = "Cari berita"
    const val CLOSE_SEARCH_DESC = "Tutup pencarian"
    const val CLEAR_SEARCH_DESC = "Hapus pencarian"
    const val REFRESH_BUTTON_DESC = "Muat ulang"
    const val BACK_BUTTON_DESC = "Kembali"
    const val RETRY_BUTTON = "Coba Lagi"
    const val READ_FULL_ARTICLE = "Baca Artikel Lengkap"

    // Offline & Status Messages
    const val OFFLINE_BANNER_TEXT = "Mode Offline — Menampilkan berita yang tersimpan"
    const val OFFLINE_BANNER_DESC = "Mode Offline"

    // Empty & Error States
    const val EMPTY_ARTICLE_TITLE = "Tidak ada berita"
    const val EMPTY_ARTICLE_MESSAGE = "Coba muat ulang atau periksa kata kunci pencarian Anda."
    const val ARTICLE_NOT_FOUND = "Berita tidak ditemukan"
    const val NO_INTERNET_MESSAGE = "Tidak ada koneksi internet. Menampilkan berita tersimpan."
    const val TIMEOUT_MESSAGE = "Koneksi time out. Coba beberapa saat lagi."
    const val SERVER_ERROR_DEFAULT = "Terjadi kesalahan pada server."
    const val EMPTY_DATA_ERROR = "Tidak ada berita ditemukan."
    const val DATA_NOT_FOUND_ERROR = "Data tidak ditemukan."
    const val UNKNOWN_ERROR = "Terjadi kesalahan."
    const val LOAD_FAILED_MESSAGE = "Gagal memuat berita terbaru."

    // Media & Fullscreen Image Viewer
    const val FULLSCREEN_IMAGE_DESC = "Gambar layar penuh"
    const val CLOSE_BUTTON_DESC = "Tutup"
}
