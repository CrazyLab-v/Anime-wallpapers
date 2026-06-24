package com.example

import org.junit.Assert.*
import org.junit.Test
import java.net.URL

class ExampleUnitTest {
  @Test
  fun testHtmlParsing() {
    try {
      println("=== FETCHING HOME PAGE ===")
      val homeHtml = URL("https://chaoslabs.site/10/").readText()
      
      // 1. Categories Parsing
      val categoryRegex = """class="category-chip"\s+href="/10/\?category=([^"#\s]+)[^"]*">\s*<span[^>]*class="category-icon"[^>]*>([^<]+)</span>\s*<span[^>]*class="category-name">([^<]+)</span>""".toRegex(RegexOption.IGNORE_CASE)
      val categories = categoryRegex.findAll(homeHtml).map { match ->
        val id = match.groupValues[1]
        val icon = match.groupValues[2]
        val name = match.groupValues[3].trim()
        mapOf("id" to id, "icon" to icon, "name" to name)
      }.toList()
      
      println("Parsed Categories (${categories.size}):")
      categories.forEach { println(" - ${it["icon"]} ${it["name"]} (id=${it["id"]})") }
      assertTrue(categories.isNotEmpty())

      // 2. Albums Parsing
      val albumRegex = """class="album-card"\s+href="([^"]+)"[\s\S]*?<img\s+src="([^"]+)"[^>]*>[\s\S]*?class="album-count">([^<]+)</span>[\s\S]*?<h3>([^<]+)</h3>""".toRegex(RegexOption.IGNORE_CASE)
      val albums = albumRegex.findAll(homeHtml).map { match ->
        val href = match.groupValues[1].replace("&amp;", "&")
        val coverImg = match.groupValues[2]
        val countStr = match.groupValues[3]
        val title = match.groupValues[4]
        mapOf("href" to href, "cover" to coverImg, "count" to countStr, "title" to title)
      }.toList()
      
      println("Parsed Albums (${albums.size}):")
      albums.forEach { println(" - ${it["title"]} (${it["count"]}), cover=${it["cover"]}, href=${it["href"]}") }
      assertTrue(albums.isNotEmpty())

      // 3. Wallpapers Parsing (fetch first album detail)
      if (albums.isNotEmpty()) {
        val firstAlbumHref = albums[0]["href"] ?: ""
        val fullAlbumUrl = "https://chaoslabs.site$firstAlbumHref"
        println("=== FETCHING ALBUM: $fullAlbumUrl ===")
        val albumHtml = URL(fullAlbumUrl).readText()
        
        val wallpaperRegex = """class="thumb-button"[\s\S]*?data-preview="([^"]+)"\s+data-title="([^"]+)"""".toRegex(RegexOption.IGNORE_CASE)
        val wallpapers = wallpaperRegex.findAll(albumHtml).map { match ->
          val previewUrl = match.groupValues[1]
          val title = match.groupValues[2]
          mapOf("url" to "https://chaoslabs.site/10/$previewUrl", "title" to title)
        }.toList()
        
        println("Parsed Wallpapers in first album (${wallpapers.size}):")
        wallpapers.forEach { println(" - ${it["title"]}: ${it["url"]}") }
        assertTrue(wallpapers.isNotEmpty())
      }
    } catch (e: Exception) {
      e.printStackTrace()
      fail("Parsing failed: ${e.message}")
    }
  }
}
