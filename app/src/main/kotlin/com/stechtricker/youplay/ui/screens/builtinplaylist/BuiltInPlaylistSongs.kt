package com.stechtricker.youplay.ui.screens.builtinplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stechtricker.compose.persist.persistList
import com.stechtricker.youplay.Database
import com.stechtricker.youplay.LocalPlayerAwareWindowInsets
import com.stechtricker.youplay.LocalPlayerServiceBinder
import com.stechtricker.youplay.R
import com.stechtricker.youplay.enums.BuiltInPlaylist
import com.stechtricker.youplay.models.Song
import com.stechtricker.youplay.models.SongWithContentLength
import com.stechtricker.youplay.ui.components.LocalMenuState
import com.stechtricker.youplay.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.stechtricker.youplay.ui.components.themed.Header
import com.stechtricker.youplay.ui.components.themed.InHistoryMediaItemMenu
import com.stechtricker.youplay.ui.components.themed.NonQueuedMediaItemMenu
import com.stechtricker.youplay.ui.components.themed.SecondaryTextButton
import com.stechtricker.youplay.ui.items.SongItem
import com.stechtricker.youplay.ui.styling.Dimensions
import com.stechtricker.youplay.ui.styling.LocalAppearance
import com.stechtricker.youplay.ui.styling.px
import com.stechtricker.youplay.utils.asMediaItem
import com.stechtricker.youplay.utils.enqueue
import com.stechtricker.youplay.utils.forcePlayAtIndex
import com.stechtricker.youplay.utils.forcePlayFromBeginning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun BuiltInPlaylistSongs(builtInPlaylist: BuiltInPlaylist) {
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs by persistList<Song>("${builtInPlaylist.name}/songs")

    LaunchedEffect(Unit) {
        when (builtInPlaylist) {
            BuiltInPlaylist.Favorites -> Database
                .favorites()

            BuiltInPlaylist.Offline -> Database
                .songsWithContentLength()
                .flowOn(Dispatchers.IO)
                .map { songs ->
                    songs.filter { song ->
                        song.contentLength?.let {
                            binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                        } ?: false
                    }.map(SongWithContentLength::song)
                }
        }.collect { songs = it }
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSize = thumbnailSizeDp.px

    val lazyListState = rememberLazyListState()

    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(
                    title = when (builtInPlaylist) {
                        BuiltInPlaylist.Favorites -> "Favorites"
                        BuiltInPlaylist.Offline -> "Offline"
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    SecondaryTextButton(
                        text = "Enqueue",
                        enabled = songs.isNotEmpty(),
                        onClick = {
                            binder?.player?.enqueue(songs.map(Song::asMediaItem))
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
                contentType = { _, song -> song },
            ) { index, song ->
                SongItem(
                    song = song,
                    thumbnailSizeDp = thumbnailSizeDp,
                    thumbnailSizePx = thumbnailSize,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    when (builtInPlaylist) {
                                        BuiltInPlaylist.Favorites -> NonQueuedMediaItemMenu(
                                            mediaItem = song.asMediaItem,
                                            onDismiss = menuState::hide
                                        )

                                        BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                            song = song,
                                            onDismiss = menuState::hide
                                        )
                                    }
                                }
                            },
                            onClick = {
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(
                                    songs.map(Song::asMediaItem),
                                    index
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(
            lazyListState = lazyListState,
            iconId = R.drawable.shuffle,
            onClick = {
                if (songs.isNotEmpty()) {
                    binder?.stopRadio()
                    binder?.player?.forcePlayFromBeginning(
                        songs.shuffled().map(Song::asMediaItem)
                    )
                }
            }
        )
    }
}
