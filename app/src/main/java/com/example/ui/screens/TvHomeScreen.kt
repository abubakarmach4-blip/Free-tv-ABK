package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Channel
import com.example.data.model.ChannelCategories
import com.example.data.repository.SyncState
import com.example.ui.components.CategoryTabs
import com.example.ui.components.ChannelCard
import com.example.ui.components.PlaylistSourcesDialog
import com.example.ui.components.VideoPlayerSection
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TvViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvHomeScreen(
    viewModel: TvViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recentChannels by viewModel.recentChannels.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val showPlaylistDialog by viewModel.showPlaylistDialog.collectAsStateWithLifecycle()
    val playlistSources by viewModel.playlistSources.collectAsStateWithLifecycle()

    // Fullscreen video player covers everything
    if (playerState.isFullscreen && playerState.currentChannel != null) {
        VideoPlayerSection(
            playerState = playerState,
            exoPlayer = viewModel.getExoPlayer(),
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onPreviousChannel = { viewModel.playPreviousChannel() },
            onNextChannel = { viewModel.playNextChannel() },
            onRetry = { viewModel.retryPlayback() },
            onToggleFullscreen = { viewModel.toggleFullscreen() },
            onClosePlayer = { viewModel.closePlayer() }
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(CyanNeon, ElectricBlue))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LiveTv,
                                contentDescription = null,
                                tint = BackgroundDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AutoStream",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TV",
                                    color = CyanNeon,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "Free Public Broadcasters & Live Streams",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Sync Status Indicator or Refresh Button
                    if (syncState is SyncState.Syncing) {
                        CircularProgressIndicator(
                            color = CyanNeon,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(22.dp)
                                .padding(end = 6.dp)
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.refreshCatalog() },
                            modifier = Modifier.testTag("top_bar_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh Catalog",
                                tint = CyanNeon
                            )
                        }
                    }

                    // Playlist source manager
                    IconButton(
                        onClick = { viewModel.setShowPlaylistDialog(true) },
                        modifier = Modifier.testTag("top_bar_playlist_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudSync,
                            contentDescription = "Playlist Sources",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Active Player (In-line portrait player)
            if (playerState.currentChannel != null) {
                Box(modifier = Modifier.padding(12.dp)) {
                    VideoPlayerSection(
                        playerState = playerState,
                        exoPlayer = viewModel.getExoPlayer(),
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onPreviousChannel = { viewModel.playPreviousChannel() },
                        onNextChannel = { viewModel.playNextChannel() },
                        onRetry = { viewModel.retryPlayback() },
                        onToggleFullscreen = { viewModel.toggleFullscreen() },
                        onClosePlayer = { viewModel.closePlayer() }
                    )
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search channels, news, sports, documentaries...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = BorderGlass,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_field")
                )
            }

            // Category Tabs
            CategoryTabs(
                categories = ChannelCategories.ALL_CATEGORIES,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    viewModel.selectCategory(category)
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Horizontal sections for Favorites / Recents if present and no active search
            if (searchQuery.isBlank() && selectedCategory == ChannelCategories.ALL) {
                if (recentChannels.isNotEmpty()) {
                    ChannelHorizontalSection(
                        title = "Recently Watched",
                        icon = Icons.Filled.History,
                        channels = recentChannels.take(8),
                        onChannelClick = { viewModel.playChannel(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }

                if (favorites.isNotEmpty()) {
                    ChannelHorizontalSection(
                        title = "Favorite Channels",
                        icon = Icons.Filled.Star,
                        channels = favorites,
                        onChannelClick = { viewModel.playChannel(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
            }

            // Main Channel Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                if (channels.isEmpty()) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tv,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "No channels matching \"$searchQuery\""
                            else
                                "No channels found in $selectedCategory",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching another term, checking another category, or updating your playlist source.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.testTag("channels_grid")
                    ) {
                        items(channels, key = { it.id }) { channel ->
                            ChannelCard(
                                channel = channel,
                                onClick = { viewModel.playChannel(channel) },
                                onToggleFavorite = { viewModel.toggleFavorite(channel) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Playlist Sources & Cache Management Dialog
    if (showPlaylistDialog) {
        PlaylistSourcesDialog(
            sources = playlistSources,
            syncState = syncState,
            totalChannels = channels.size,
            onRefresh = { url -> viewModel.refreshCatalog(url) },
            onAddSource = { name, url -> viewModel.addCustomPlaylistSource(name, url) },
            onRemoveSource = { id -> viewModel.removePlaylistSource(id) },
            onDismiss = { viewModel.setShowPlaylistDialog(false) }
        )
    }
}

@Composable
private fun ChannelHorizontalSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyanNeon,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(channels, key = { "row_${it.id}" }) { channel ->
                Box(modifier = Modifier.width(160.dp)) {
                    ChannelCard(
                        channel = channel,
                        onClick = { onChannelClick(channel) },
                        onToggleFavorite = { onToggleFavorite(channel) }
                    )
                }
            }
        }
    }
}
