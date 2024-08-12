package com.stechtricker.youplay.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stechtricker.compose.persist.persist
import com.stechtricker.innertube.Innertube
import com.stechtricker.innertube.utils.plus
import com.stechtricker.youplay.LocalPlayerAwareWindowInsets
import com.stechtricker.youplay.ui.components.ShimmerHost
import com.stechtricker.youplay.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.stechtricker.youplay.ui.styling.LocalAppearance
import com.stechtricker.youplay.utils.center
import com.stechtricker.youplay.utils.secondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Composable
inline fun <T : Innertube.Item> ItemsPage(
    tag: String,
    crossinline headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    noinline itemPlaceholderContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialPlaceholderCount: Int = 8,
    continuationPlaceholderCount: Int = 3,
    emptyItemsText: String = "No items found",
    noinline itemsPageProvider: (suspend (String?) -> Result<Innertube.ItemsPage<T>?>?)? = null,
) {
    val (_, typography) = LocalAppearance.current

    val updatedItemsPageProvider by rememberUpdatedState(itemsPageProvider)

    val lazyListState = rememberLazyListState()

    var itemsPage by persist<Innertube.ItemsPage<T>?>(tag)

    LaunchedEffect(lazyListState, updatedItemsPageProvider) {
        val currentItemsPageProvider = updatedItemsPageProvider ?: return@LaunchedEffect

        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" } }
            .collect { shouldLoadMore ->
                if (!shouldLoadMore) return@collect

                withContext(Dispatchers.IO) {
                    currentItemsPageProvider(itemsPage?.continuation)
                }?.onSuccess {
                    if (it == null) {
                        if (itemsPage == null) {
                            itemsPage = Innertube.ItemsPage(null, null)
                        }
                    } else {
                        itemsPage += it
                    }
                }
            }
    }

    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = modifier
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = "header",
            ) {
                headerContent(null)
            }

            items(
                items = itemsPage?.items ?: emptyList(),
                key = Innertube.Item::key,
                itemContent = itemContent
            )

            if (itemsPage != null && itemsPage?.items.isNullOrEmpty()) {
                item(key = "empty") {
                    BasicText(
                        text = emptyItemsText,
                        style = typography.xs.secondary.center,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 32.dp)
                            .fillMaxWidth()
                    )
                }
            }

            if (!(itemsPage != null && itemsPage?.continuation == null)) {
                item(key = "loading") {
                    val isFirstLoad = itemsPage?.items.isNullOrEmpty()
                    ShimmerHost(
                        modifier = Modifier
                            .run {
                                if (isFirstLoad) fillParentMaxSize() else this
                            }
                    ) {
                        repeat(if (isFirstLoad) initialPlaceholderCount else continuationPlaceholderCount) {
                            itemPlaceholderContent()
                        }
                    }
                }
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }
}
