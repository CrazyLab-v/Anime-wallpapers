package com.example.data

data class Category(
    val id: String,
    val icon: String,
    val name: String
)

data class Album(
    val href: String,
    val coverUrl: String,
    val countString: String,
    val title: String,
    val categoryId: String
) {
    // Helper to get raw album name from href
    val albumId: String
        get() = href.substringAfter("album=").substringBefore("&")
}

data class Wallpaper(
    val url: String,
    val title: String,
    val albumTitle: String,
    val categoryId: String,
    val isFavorite: Boolean = false
) {
    // Unique identifier for wallpaper
    val id: String
        get() = url
}
