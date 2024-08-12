package com.stechtricker.youplay.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalContext
import com.stechtricker.compose.persist.PersistMapCleanup
import com.stechtricker.compose.routing.RouteHandler
import com.stechtricker.compose.routing.defaultStacking
import com.stechtricker.compose.routing.defaultStill
import com.stechtricker.compose.routing.defaultUnstacking
import com.stechtricker.compose.routing.isStacking
import com.stechtricker.compose.routing.isUnknown
import com.stechtricker.compose.routing.isUnstacking
import com.stechtricker.youplay.Database
import com.stechtricker.youplay.R
import com.stechtricker.youplay.models.SearchQuery
import com.stechtricker.youplay.query
import com.stechtricker.youplay.ui.components.themed.Scaffold
import com.stechtricker.youplay.ui.screens.albumRoute
import com.stechtricker.youplay.ui.screens.artistRoute
import com.stechtricker.youplay.ui.screens.builtInPlaylistRoute
import com.stechtricker.youplay.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import com.stechtricker.youplay.ui.screens.globalRoutes
import com.stechtricker.youplay.ui.screens.localPlaylistRoute
import com.stechtricker.youplay.ui.screens.localplaylist.LocalPlaylistScreen
import com.stechtricker.youplay.ui.screens.playlistRoute
import com.stechtricker.youplay.ui.screens.search.SearchScreen
import com.stechtricker.youplay.ui.screens.searchResultRoute
import com.stechtricker.youplay.ui.screens.searchRoute
import com.stechtricker.youplay.ui.screens.searchresult.SearchResultScreen
import com.stechtricker.youplay.ui.screens.settings.SettingsScreen
import com.stechtricker.youplay.ui.screens.settingsRoute
import com.stechtricker.youplay.utils.homeScreenTabIndexKey
import com.stechtricker.youplay.utils.pauseSearchHistoryKey
import com.stechtricker.youplay.utils.preferences
import com.stechtricker.youplay.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen(onPlaylistUrl: (String) -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl
            )
        }

        host {
            val (tabIndex, onTabChanged) = rememberPreference(
                homeScreenTabIndexKey,
                defaultValue = 0
            )

            Scaffold(
                topIconButtonId = R.drawable.equalizer,
                onTopIconButtonClick = { settingsRoute() },
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, "Quick picks", R.drawable.sparkles)
                    Item(1, "Songs", R.drawable.musical_notes)
                    Item(2, "Playlists", R.drawable.playlist)
                    Item(3, "Artists", R.drawable.person)
                    Item(4, "Albums", R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicks(
                            onAlbumClick = { albumRoute(it) },
                            onArtistClick = { artistRoute(it) },
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        1 -> HomeSongs(
                            onSearchClick = { searchRoute("") }
                        )

                        2 -> HomePlaylists(
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        3 -> HomeArtistList(
                            onArtistClick = { artistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        4 -> HomeAlbums(
                            onAlbumClick = { albumRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
                    }
                }
            }
        }
    }
}
