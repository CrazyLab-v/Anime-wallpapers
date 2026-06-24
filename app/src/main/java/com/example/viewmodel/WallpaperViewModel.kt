package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Album
import com.example.data.Category
import com.example.data.Wallpaper
import com.example.data.WallpaperRepository
import com.example.util.WallpaperHelper
import com.example.util.WallpaperTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WallpaperRepository()
    private val prefs = application.getSharedPreferences("anime_wallpapers_prefs", Context.MODE_PRIVATE)

    // UI States
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _featuredAlbums = MutableStateFlow<List<Album>>(emptyList())
    
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    
    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _wallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val wallpapers: StateFlow<List<Wallpaper>> = _wallpapers.asStateFlow()

    private val _selectedWallpaper = MutableStateFlow<Wallpaper?>(null)
    val selectedWallpaper: StateFlow<Wallpaper?> = _selectedWallpaper.asStateFlow()

    private val _favorites = MutableStateFlow<List<Wallpaper>>(emptyList())
    val favorites: StateFlow<List<Wallpaper>> = _favorites.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationStatus: StateFlow<OperationStatus> = _operationStatus.asStateFlow()

    // Screen State for custom state navigation
    // Values: "HOME", "ALBUMS", "WALLPAPERS", "FAVORITES"
    private val _currentScreen = MutableStateFlow("HOME")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Combined stream to handle searching and filtering of albums
    val filteredAlbums: StateFlow<List<Album>> = combine(_albums, _searchQuery) { albums, query ->
        if (query.isBlank()) {
            albums
        } else {
            albums.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadFavorites()
        loadHomeData()
    }

    /**
     * Loads categories and featured albums from home page
     */
    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val (cats, featured) = repository.getCategoriesAndFeaturedAlbums()
                _categories.value = cats
                _featuredAlbums.value = featured
                
                // Auto-select the first category instead of showing "All" (HOME screen)
                val firstCat = cats.firstOrNull()
                if (firstCat != null) {
                    selectCategory(firstCat)
                } else {
                    _albums.value = featured
                    _selectedCategory.value = null
                    _currentScreen.value = "HOME"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Ошибка загрузки: проверьте интернет-соединение"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Selects a category and fetches its albums
     */
    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        _selectedAlbum.value = null
        _wallpapers.value = emptyList()
        _searchQuery.value = ""
        
        if (category == null) {
            _albums.value = _featuredAlbums.value
            _currentScreen.value = "HOME"
            return
        }

        _currentScreen.value = "ALBUMS"
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val catAlbums = repository.getAlbumsByCategory(category.id)
                _albums.value = catAlbums
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Ошибка загрузки альбомов этой категории"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Opens an album and fetches its wallpapers
     */
    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
        _selectedWallpaper.value = null
        
        if (album == null) {
            _wallpapers.value = emptyList()
            if (_selectedCategory.value == null) {
                _currentScreen.value = "HOME"
            } else {
                _currentScreen.value = "ALBUMS"
            }
            return
        }

        _currentScreen.value = "WALLPAPERS"
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val albumWallpapers = repository.getWallpapersByAlbum(album)
                // Sync favorites status
                val synced = albumWallpapers.map { wp ->
                    wp.copy(isFavorite = _favorites.value.any { fav -> fav.url == wp.url })
                }
                _wallpapers.value = synced
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Ошибка загрузки картинок из этого альбома"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Opens a wallpaper preview dialog/overlay
     */
    fun selectWallpaper(wallpaper: Wallpaper?) {
        _selectedWallpaper.value = wallpaper
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun navigateToFavorites() {
        _currentScreen.value = "FAVORITES"
    }

    fun navigateBack() {
        _error.value = null
        when (_currentScreen.value) {
            "FAVORITES" -> {
                _currentScreen.value = "ALBUMS"
            }
            "WALLPAPERS" -> {
                _selectedAlbum.value = null
                _currentScreen.value = "ALBUMS"
            }
            "ALBUMS" -> {
                // Already at root category collection listing, nothing to go back to
            }
        }
    }

    /**
     * Sets the specified wallpaper to Home, Lock or Both screens
     */
    fun applyWallpaper(context: Context, wallpaper: Wallpaper, target: WallpaperTarget) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus.Loading("Применение обоев...")
            try {
                val bitmap = WallpaperHelper.downloadBitmap(wallpaper.url)
                if (bitmap != null) {
                    val success = WallpaperHelper.setAsWallpaper(context, bitmap, target)
                    if (success) {
                        _operationStatus.value = OperationStatus.Success("Обои успешно установлены!")
                    } else {
                        _operationStatus.value = OperationStatus.Error("Не удалось установить обои")
                    }
                } else {
                    _operationStatus.value = OperationStatus.Error("Ошибка скачивания изображения")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _operationStatus.value = OperationStatus.Error("Ошибка: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Saves the specified wallpaper to local gallery
     */
    fun downloadToGallery(context: Context, wallpaper: Wallpaper) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus.Loading("Сохранение в галерею...")
            try {
                val bitmap = WallpaperHelper.downloadBitmap(wallpaper.url)
                if (bitmap != null) {
                    val success = WallpaperHelper.saveToGallery(context, bitmap, wallpaper.title)
                    if (success) {
                        _operationStatus.value = OperationStatus.Success("Сохранено в галерею в папку Pictures/AnimeWallpapers!")
                    } else {
                        _operationStatus.value = OperationStatus.Error("Не удалось сохранить изображение")
                    }
                } else {
                    _operationStatus.value = OperationStatus.Error("Ошибка скачивания изображения")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _operationStatus.value = OperationStatus.Error("Ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun dismissOperationStatus() {
        _operationStatus.value = OperationStatus.Idle
    }

    /**
     * Toggles wallpaper favorite state
     */
    fun toggleFavorite(wallpaper: Wallpaper) {
        val currentFavs = _favorites.value.toMutableList()
        val existingIndex = currentFavs.indexOfFirst { it.url == wallpaper.url }
        
        if (existingIndex >= 0) {
            currentFavs.removeAt(existingIndex)
        } else {
            currentFavs.add(wallpaper.copy(isFavorite = true))
        }

        _favorites.value = currentFavs
        saveFavorites(currentFavs)

        // Refresh favorite state in active wallpaper list
        _wallpapers.value = _wallpapers.value.map { wp ->
            if (wp.url == wallpaper.url) {
                wp.copy(isFavorite = existingIndex < 0)
            } else {
                wp
            }
        }

        // Refresh selected wallpaper state if active
        val activeSelected = _selectedWallpaper.value
        if (activeSelected?.url == wallpaper.url) {
            _selectedWallpaper.value = activeSelected.copy(isFavorite = existingIndex < 0)
        }
    }

    private fun loadFavorites() {
        val favsSet = prefs.getStringSet("favorite_wallpapers_v1", emptySet()) ?: emptySet()
        val loaded = favsSet.mapNotNull { str ->
            try {
                val parts = str.split("|")
                if (parts.size >= 4) {
                    Wallpaper(
                        url = parts[0],
                        title = parts[1],
                        albumTitle = parts[2],
                        categoryId = parts[3],
                        isFavorite = true
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
        _favorites.value = loaded
    }

    private fun saveFavorites(favs: List<Wallpaper>) {
        val stringSet = favs.map { wp ->
            "${wp.url}|${wp.title}|${wp.albumTitle}|${wp.categoryId}"
        }.toSet()
        prefs.edit().putStringSet("favorite_wallpapers_v1", stringSet).apply()
    }
}

sealed class OperationStatus {
    object Idle : OperationStatus()
    data class Loading(val message: String) : OperationStatus()
    data class Success(val message: String) : OperationStatus()
    data class Error(val message: String) : OperationStatus()
}
