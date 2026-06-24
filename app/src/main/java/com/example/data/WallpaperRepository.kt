package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class WallpaperRepository {
    private val baseUrl = "https://chaoslabs.site"
    private val subPath = "/10/"

    // Regular Expression patterns
    private val categoryRegex = """class="category-chip"\s+href="/10/\?category=([^"#\s]+)[^"]*">\s*<span[^>]*class="category-icon"[^>]*>([^<]+)</span>\s*<span[^>]*class="category-name">([^<]+)</span>""".toRegex(RegexOption.IGNORE_CASE)
    private val albumRegex = """class="album-card"\s+href="([^"]+)"[\s\S]*?<img\s+src="([^"]+)"[^>]*>[\s\S]*?class="album-count">([^<]+)</span>[\s\S]*?<h3>([^<]+)</h3>""".toRegex(RegexOption.IGNORE_CASE)
    private val wallpaperRegex = """class="thumb-button"[\s\S]*?data-preview="([^"]+)"\s+data-title="([^"]+)"""".toRegex(RegexOption.IGNORE_CASE)

    suspend fun getCategoriesAndFeaturedAlbums(): Pair<List<Category>, List<Album>> = withContext(Dispatchers.IO) {
        val html = URL("$baseUrl$subPath").readText(Charsets.UTF_8)
        
        val categories = categoryRegex.findAll(html).map { match ->
            val id = match.groupValues[1]
            val icon = match.groupValues[2]
            val name = match.groupValues[3].trim()
            Category(id = id, icon = icon, name = name)
        }.toList()

        val albums = albumRegex.findAll(html).map { match ->
            val href = match.groupValues[1].replace("&amp;", "&")
            val coverImg = match.groupValues[2]
            val countStr = match.groupValues[3]
            val title = match.groupValues[4]
            Album(
                href = href,
                coverUrl = "$baseUrl$subPath$coverImg",
                countString = countStr,
                title = title,
                categoryId = extractCategoryId(href)
            )
        }.toList()

        Pair(categories, albums)
    }

    suspend fun getAlbumsByCategory(categoryId: String): List<Album> = withContext(Dispatchers.IO) {
        val fullUrl = "$baseUrl$subPath?category=$categoryId"
        val html = URL(fullUrl).readText(Charsets.UTF_8)

        albumRegex.findAll(html).map { match ->
            val href = match.groupValues[1].replace("&amp;", "&")
            val coverImg = match.groupValues[2]
            val countStr = match.groupValues[3]
            val title = match.groupValues[4]
            Album(
                href = href,
                coverUrl = "$baseUrl$subPath$coverImg",
                countString = countStr,
                title = title,
                categoryId = categoryId
            )
        }.toList()
    }

    suspend fun getWallpapersByAlbum(album: Album): List<Wallpaper> = withContext(Dispatchers.IO) {
        val fullUrl = if (album.href.startsWith("http")) album.href else "$baseUrl${album.href}"
        val html = URL(fullUrl).readText(Charsets.UTF_8)

        wallpaperRegex.findAll(html).map { match ->
            val previewUrl = match.groupValues[1]
            val title = match.groupValues[2]
            Wallpaper(
                url = "$baseUrl$subPath$previewUrl",
                title = title,
                albumTitle = album.title,
                categoryId = album.categoryId
            )
        }.toList()
    }

    private fun extractCategoryId(href: String): String {
        return href.substringAfter("category=").substringBefore("&").substringBefore("#")
    }
}
