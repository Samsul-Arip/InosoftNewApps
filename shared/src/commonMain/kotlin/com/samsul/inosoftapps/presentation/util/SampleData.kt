package com.samsul.inosoftapps.presentation.util

import com.samsul.inosoftapps.domain.model.Article

/**
 * Realistic sample article data for Compose Previews and tests.
 */
object SampleData {

    val sampleArticle = Article(
        id = "1",
        title = "Kotlin Multiplatform dan Jetpack Compose: Masa Depan Pengembangan Mobile Modern",
        description = "Ekosistem Kotlin Multiplatform semakin matang dengan dukungan stabil Room KMP, Koin, dan Compose Multiplatform yang memungkinkan berbagi logika dan UI 100% antar platform.",
        content = "Kotlin Multiplatform (KMP) kini telah menjadi pilihan utama bagi banyak perusahaan teknologi terkemuka untuk membangun aplikasi mobile yang efisien dan berkualitas tinggi. Dengan berbagi kode domain, data layer, dan bahkan UI menggunakan Compose Multiplatform, developer dapat menghemat waktu hingga 40% tanpa mengorbankan performa native dan tampilan platform yang konsisten.",
        author = "Samsul Aripin",
        url = "https://developer.android.com/kotlin/multiplatform",
        imageUrl = "https://picsum.photos/800/400",
        publishedAt = "27 Agu 2026, 15:30",
        sourceName = "Kompas Tekno",
        category = "technology",
        isBookmarked = true
    )

    val sampleArticles = listOf(
        sampleArticle,
        Article(
            id = "2",
            title = "Pertumbuhan Ekonomi Digital Indonesia Diproyeksikan Tembus Rekor Baru",
            description = "Sektor e-commerce dan fintech terus menjadi pendorong utama pertumbuhan ekonomi digital nasional sepanjang kuartal ketiga tahun ini.",
            content = "Pertumbuhan ekonomi digital di Indonesia menunjukkan tren yang sangat positif didorong oleh adopsi teknologi pembayaran digital dan platform marketplace yang semakin merata di berbagai daerah.",
            author = "Budi Santoso",
            url = "https://ekonomi.bisnis.com",
            imageUrl = "https://picsum.photos/800/401",
            publishedAt = "27 Agu 2026, 14:15",
            sourceName = "Bisnis Indonesia",
            category = "business",
            isBookmarked = false
        ),
        Article(
            id = "3",
            title = "Timnas Indonesia Raih Kemenangan Krusial di Babak Kualifikasi",
            description = "Penampilan disiplin dan strategi menyerang efektif membawa skuad Garuda mengamankan 3 poin penting di hadapan puluhan ribu suporter.",
            content = "Pertandingan berlangsung sengit sejak menit awal dengan kedua tim saling melancarkan serangan terbuka dan tensi tinggi sepanjang 90 menit.",
            author = "Rian Pratama",
            url = "https://bola.net",
            imageUrl = "https://picsum.photos/800/402",
            publishedAt = "27 Agu 2026, 12:00",
            sourceName = "Detik Sport",
            category = "sports",
            isBookmarked = false
        )
    )
}
