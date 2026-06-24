package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.data.Album
import com.example.data.Category
import com.example.data.Wallpaper
import com.example.ui.theme.AnimeDarkBg
import com.example.ui.theme.AnimeMagenta
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimeYellow
import com.example.ui.theme.AnimeCherryBlossom
import com.example.ui.theme.AnimeSurface
import com.example.ui.theme.AnimeSurfaceVariant
import com.example.ui.theme.AnimeBorder
import com.example.ui.theme.AnimeTextPrimary
import com.example.ui.theme.AnimeTextSecondary
import com.example.ui.theme.AnimeOverlay
import com.example.ui.theme.MyApplicationTheme
import com.example.util.WallpaperTarget
import com.example.viewmodel.OperationStatus
import com.example.viewmodel.WallpaperViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WallpaperViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0F111A),
                                        Color(0xFF08090E)
                                    )
                                )
                            )
                            .padding(innerPadding)
                    ) {
                        WallpaperAppContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WallpaperAppContent(viewModel: WallpaperViewModel) {
    val context = LocalContext.current
    
    // States
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val filteredAlbums by viewModel.filteredAlbums.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val wallpapers by viewModel.wallpapers.collectAsStateWithLifecycle()
    val selectedWallpaper by viewModel.selectedWallpaper.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()

    // Handle Operation Dialog State
    var showOperationDialog by remember { mutableStateOf(false) }
    var operationMessage by remember { mutableStateOf("") }
    var operationIsLoading by remember { mutableStateOf(false) }
    var operationIsSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(operationStatus) {
        when (val status = operationStatus) {
            is OperationStatus.Idle -> {
                showOperationDialog = false
            }
            is OperationStatus.Loading -> {
                operationMessage = status.message
                operationIsLoading = true
                operationIsSuccess = false
                showOperationDialog = true
            }
            is OperationStatus.Success -> {
                operationMessage = status.message
                operationIsLoading = false
                operationIsSuccess = true
                showOperationDialog = true
            }
            is OperationStatus.Error -> {
                operationMessage = status.message
                operationIsLoading = false
                operationIsSuccess = false
                showOperationDialog = true
            }
        }
    }

    // Main Layout Column
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App Top Navigation / Header
        AppHeader(
            currentScreen = currentScreen,
            selectedCategory = selectedCategory,
            selectedAlbum = selectedAlbum,
            searchQuery = searchQuery,
            onSearchChange = { viewModel.setSearchQuery(it) },
            onFavoritesClick = { viewModel.navigateToFavorites() },
            onBackClick = { viewModel.navigateBack() }
        )

        // Horizontal Category Tabs (Sticky at top of Home and Albums lists)
        if (currentScreen == "HOME" || currentScreen == "ALBUMS") {
            CategoryTabs(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = { viewModel.selectCategory(it) }
            )
        }

        // Error message banner
        if (error != null) {
            ErrorBanner(
                message = error ?: "",
                onRetry = {
                    if (selectedAlbum != null) {
                        viewModel.selectAlbum(selectedAlbum)
                    } else if (selectedCategory != null) {
                        viewModel.selectCategory(selectedCategory)
                    } else {
                        viewModel.loadHomeData()
                    }
                }
            )
        }

        // Loading and Main Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading && wallpapers.isEmpty() && filteredAlbums.isEmpty()) {
                CircularProgressIndicator(
                    color = AnimeMagenta,
                    modifier = Modifier.size(50.dp)
                )
            } else {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    },
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        "HOME", "ALBUMS" -> {
                            AlbumGridScreen(
                                albums = filteredAlbums,
                                onAlbumSelect = { viewModel.selectAlbum(it) }
                            )
                        }
                        "WALLPAPERS" -> {
                            WallpaperGridScreen(
                                wallpapers = wallpapers,
                                albumTitle = selectedAlbum?.title ?: "Галерея",
                                onWallpaperSelect = { viewModel.selectWallpaper(it) }
                            )
                        }
                        "FAVORITES" -> {
                            WallpaperGridScreen(
                                wallpapers = favorites,
                                albumTitle = "Избранные Обои",
                                onWallpaperSelect = { viewModel.selectWallpaper(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Operation status notification dialog overlay (Custom Anime Style)
    if (showOperationDialog) {
        Dialog(
            onDismissRequest = { viewModel.dismissOperationStatus() },
            properties = DialogProperties(dismissOnBackPress = !operationIsLoading, dismissOnClickOutside = !operationIsLoading)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, if (operationIsSuccess) AnimeCyan else AnimeMagenta),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (operationIsLoading) {
                        CircularProgressIndicator(
                            color = AnimeMagenta,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("operation_progress")
                        )
                    } else {
                        Icon(
                            imageVector = if (operationIsSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (operationIsSuccess) AnimeCyan else AnimeMagenta,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = operationMessage,
                        color = AnimeTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (!operationIsLoading) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.dismissOperationStatus() },
                            colors = ButtonDefaults.buttonColors(containerColor = AnimeMagenta),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("operation_ok_button")
                        ) {
                            Text("ОК", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Wallpaper Preview overlay
    if (selectedWallpaper != null) {
        WallpaperPreviewOverlay(
            wallpaper = selectedWallpaper!!,
            onDismiss = { viewModel.selectWallpaper(null) },
            onFavoriteToggle = { viewModel.toggleFavorite(it) },
            onSetWallpaper = { target ->
                viewModel.applyWallpaper(context, selectedWallpaper!!, target)
            },
            onDownload = {
                viewModel.downloadToGallery(context, selectedWallpaper!!)
            }
        )
    }
}

@Composable
fun AppHeader(
    currentScreen: String,
    selectedCategory: Category?,
    selectedAlbum: Album?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top row: Back button, Logo and Favorites Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentScreen != "HOME" && currentScreen != "ALBUMS") {
                IconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = AnimeSurface),
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, AnimeBorder, CircleShape)
                        .testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = AnimeCyan
                    )
                }
            } else {
                // Stylish Anime Icon spacer/decoration
                Text(
                    text = "✦",
                    color = AnimeMagenta,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Brand Title with Cute Japanese Decorative Tag - ChaosLabs style
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (currentScreen) {
                        "HOME", "ALBUMS" -> "ChaosLabs Site / " + (selectedCategory?.name ?: "10")
                        "WALLPAPERS" -> "ChaosLabs / " + (selectedAlbum?.title ?: "GAL")
                        "FAVORITES" -> "ChaosLabs / FAVORITES"
                        else -> "ChaosLabs / 10"
                    }.uppercase(),
                    color = AnimeMagenta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val titleText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = AnimeTextPrimary)) {
                        append("カオス ")
                    }
                    withStyle(style = SpanStyle(color = AnimeCyan)) {
                        append("WALLS")
                    }
                }
                
                Text(
                    text = titleText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )
            }

            // Favorites Button
            IconButton(
                onClick = onFavoritesClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (currentScreen == "FAVORITES") AnimeMagenta else AnimeSurface
                ),
                modifier = Modifier
                    .size(44.dp)
                    .border(
                        1.dp,
                        if (currentScreen == "FAVORITES") AnimeMagenta else AnimeBorder,
                        CircleShape
                    )
                    .testTag("favorites_tab_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Избранное",
                    tint = if (currentScreen == "FAVORITES") Color.White else AnimeMagenta
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Real-time Search Box (Only on catalog listing pages: HOME, ALBUMS)
        if (currentScreen == "HOME" || currentScreen == "ALBUMS") {
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        "Поиск альбомов...",
                        color = AnimeTextSecondary,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AnimeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = AnimeTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = AnimeTextPrimary,
                    unfocusedTextColor = AnimeTextPrimary,
                    focusedContainerColor = AnimeSurfaceVariant,
                    unfocusedContainerColor = AnimeSurface,
                    disabledContainerColor = AnimeSurface,
                    cursorColor = AnimeMagenta,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, AnimeBorder, RoundedCornerShape(16.dp))
                    .testTag("search_input")
            )
        }
    }
}

@Composable
fun CategoryTabs(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelect: (Category?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // List categories dynamically
        items(categories) { cat ->
            CategoryChipItem(
                name = cat.name,
                icon = cat.icon,
                isSelected = selectedCategory?.id == cat.id,
                onClick = { onCategorySelect(cat) }
            )
        }
    }
}

@Composable
fun CategoryChipItem(
    name: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AnimeMagenta else AnimeBorder
    val bgColor = if (isSelected) AnimeMagenta.copy(alpha = 0.25f) else AnimeSurface

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("category_chip_$name")
    ) {
        Text(
            text = icon,
            color = if (isSelected) AnimeCyan else AnimeCherryBlossom,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            color = AnimeTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun AlbumGridScreen(
    albums: List<Album>,
    onAlbumSelect: (Album) -> Unit
) {
    if (albums.isEmpty()) {
        EmptyStateView(
            message = "Альбомы не найдены",
            iconStr = "☉_☉"
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(albums) { album ->
                AlbumCardItem(album = album, onClick = { onAlbumSelect(album) })
            }
        }
    }
}

@Composable
fun AlbumCardItem(
    album: Album,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnimeSurface),
        border = BorderStroke(1.dp, AnimeBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("album_card_${album.albumId}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
            ) {
                // High res cover art
                SubcomposeAsyncImage(
                    model = album.coverUrl,
                    contentDescription = album.title,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AnimeSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AnimeCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Wallpaper Count Ribbon
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AnimeMagenta)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = album.countString,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Text section
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = album.title,
                    color = AnimeTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✦ ОТКРЫТЬ ПОРТАЛ",
                    color = AnimeCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun WallpaperGridScreen(
    wallpapers: List<Wallpaper>,
    albumTitle: String,
    onWallpaperSelect: (Wallpaper) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Section Mini title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌸",
                color = AnimeMagenta,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = albumTitle.uppercase(),
                color = AnimeTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (wallpapers.isEmpty()) {
            EmptyStateView(
                message = "В этом альбоме пока нет картинок",
                iconStr = "❀"
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(wallpapers) { wp ->
                    WallpaperThumbnailItem(wallpaper = wp, onClick = { onWallpaperSelect(wp) })
                }
            }
        }
    }
}

@Composable
fun WallpaperThumbnailItem(
    wallpaper: Wallpaper,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.58f) // Vertical ratio for wallpapers
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, AnimeBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("wallpaper_thumbnail_${wallpaper.title.hashCode()}")
    ) {
        SubcomposeAsyncImage(
            model = wallpaper.url,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AnimeSurface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AnimeMagenta,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Small Favorite Icon Indicator
        if (wallpaper.isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(AnimeOverlay)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = AnimeMagenta,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun WallpaperPreviewOverlay(
    wallpaper: Wallpaper,
    onDismiss: () -> Unit,
    onFavoriteToggle: (Wallpaper) -> Unit,
    onSetWallpaper: (WallpaperTarget) -> Unit,
    onDownload: () -> Unit
) {
    var showWallpaperTargetsMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // High-resolution background illustration image (Full screen)
                SubcomposeAsyncImage(
                    model = wallpaper.url,
                    contentDescription = wallpaper.title,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF06030F)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AnimeMagenta)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Toolbar Row (Dismiss and Favorite button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = AnimeOverlay),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("preview_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White
                        )
                    }

                    // Favorite toggler
                    IconButton(
                        onClick = { onFavoriteToggle(wallpaper) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = AnimeOverlay),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("preview_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (wallpaper.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "В избранное",
                            tint = if (wallpaper.isFavorite) AnimeMagenta else Color.White
                        )
                    }
                }

                // Bottom Panel (Title, Download and Set Wallpaper Actions)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(24.dp)
                ) {
                    Text(
                        text = wallpaper.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Альбом: ${wallpaper.albumTitle}",
                        color = AnimeCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Download Button
                        IconButton(
                            onClick = onDownload,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = AnimeSurface),
                            modifier = Modifier
                                .size(52.dp)
                                .border(1.5.dp, AnimeCyan, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .testTag("preview_download_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Скачать",
                                tint = AnimeCyan
                            )
                        }

                        // Set Wallpaper Big Button
                        Button(
                            onClick = { showWallpaperTargetsMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AnimeMagenta),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .border(1.dp, AnimeCherryBlossom, RoundedCornerShape(16.dp))
                                .testTag("preview_apply_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "УСТАНОВИТЬ ОБОИ",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Sub-Overlay: Wallpaper Target Selection Dialog menu
                if (showWallpaperTargetsMenu) {
                    Dialog(
                        onDismissRequest = { showWallpaperTargetsMenu = false }
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AnimeSurface),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, AnimeBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "КУДА УСТАНОВИТЬ?",
                                    color = AnimeTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Выберите целевой экран на вашем телефоне",
                                    color = AnimeTextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        showWallpaperTargetsMenu = false
                                        onSetWallpaper(WallpaperTarget.HOME)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AnimeSurfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_home_button")
                                ) {
                                    Text("Рабочий стол", color = AnimeTextPrimary, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        showWallpaperTargetsMenu = false
                                        onSetWallpaper(WallpaperTarget.LOCK)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AnimeSurfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_lock_button")
                                ) {
                                    Text("Экран блокировки", color = AnimeTextPrimary, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        showWallpaperTargetsMenu = false
                                        onSetWallpaper(WallpaperTarget.BOTH)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AnimeMagenta),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_both_button")
                                ) {
                                    Text("Установить везде", color = Color.White, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = { showWallpaperTargetsMenu = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Отмена", color = AnimeTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String, iconStr: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = iconStr,
            fontSize = 48.sp,
            color = AnimeCherryBlossom.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = AnimeTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x33FF5252)),
        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ОШИБКА СВЯЗИ",
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    color = AnimeTextPrimary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onRetry,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x3300E5FF))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = AnimeCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Restyled Greeting composable to capture a gorgeous anime style card screenshot.
 * Keeps the old method name so that existing Robolectric screenshot tests build and pass cleanly!
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AnimeSurface),
        border = BorderStroke(2.dp, AnimeMagenta),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✦ WELCOME TO PORTAL ✦",
                color = AnimeCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Hello $name!",
                color = AnimeTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Anime Wallpaper Hub is ready. Explore stunning vertical collections.",
                color = AnimeTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
