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

    // Loading & Progress
    const val LOADING_DEFAULT = "Memuat berita..."
    const val LOADING_MESSAGE = "Memuat berita terbaru..."
    const val LOADING_DETAIL_MESSAGE = "Memuat detail berita..."

    // Empty & Error States
    const val EMPTY_ARTICLE_TITLE = "Tidak ada berita"
    const val EMPTY_ARTICLE_MESSAGE = "Coba muat ulang atau periksa kata kunci pencarian Anda."
    const val ARTICLE_NOT_FOUND = "Berita tidak ditemukan"
    const val NO_INTERNET_TITLE = "Koneksi Terputus"
    const val NO_INTERNET_MESSAGE = "Tidak ada koneksi internet. Menampilkan berita tersimpan."
    const val TIMEOUT_MESSAGE = "Koneksi time out. Coba beberapa saat lagi."
    const val SERVER_ERROR_DEFAULT = "Terjadi kesalahan pada server."
    const val EMPTY_DATA_ERROR = "Tidak ada berita ditemukan."
    const val DATA_NOT_FOUND_ERROR = "Data tidak ditemukan."
    const val UNKNOWN_ERROR = "Terjadi kesalahan."
    const val LOAD_FAILED_MESSAGE = "Gagal memuat berita terbaru."

    // Media & Accessibility Descriptions
    const val FULLSCREEN_IMAGE_DESC = "Gambar layar penuh"
    const val CLOSE_BUTTON_DESC = "Tutup"
    const val AUTHOR_ICON_DESC = "Penulis"
    const val DATE_ICON_DESC = "Tanggal publikasi"
    const val IMAGE_PLACEHOLDER_DESC = "Placeholder gambar"
    const val ORIGINAL_SOURCE_LABEL = "Tautan Sumber Asli (Ketuk untuk membaca di browser):"

    // Category Display Labels
    const val CATEGORY_ALL_LABEL = "Semua"
    const val CATEGORY_BUSINESS_LABEL = "Bisnis"
    const val CATEGORY_TECHNOLOGY_LABEL = "Teknologi"
    const val CATEGORY_SPORTS_LABEL = "Olahraga"
    const val CATEGORY_HEALTH_LABEL = "Kesehatan"
    const val CATEGORY_SCIENCE_LABEL = "Sains"
    const val CATEGORY_ENTERTAINMENT_LABEL = "Hiburan"

    /**
     * Formats empty search results message with given query.
     */
    fun searchEmptyMessage(query: String): String = "Tidak ada berita dengan kata kunci '$query'"

    /**
     * Formats API error message with fallback to error code or default server error message.
     */
    fun formatApiErrorMessage(message: String?, code: String?): String {
        if (!message.isNullOrBlank()) return message
        return if (!code.isNullOrBlank()) {
            "Gagal memuat berita dari server (Kode: $code)"
        } else {
            SERVER_ERROR_DEFAULT
        }
    }
}
