package com.stechtricker.youplay.ui.screens.settings

import android.text.format.Formatter
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import com.stechtricker.youplay.LocalPlayerAwareWindowInsets
import com.stechtricker.youplay.LocalPlayerServiceBinder
import com.stechtricker.youplay.enums.CoilDiskCacheMaxSize
import com.stechtricker.youplay.enums.ExoPlayerDiskCacheMaxSize
import com.stechtricker.youplay.ui.components.themed.Header
import com.stechtricker.youplay.ui.styling.LocalAppearance
import com.stechtricker.youplay.utils.coilDiskCacheMaxSizeKey
import com.stechtricker.youplay.utils.exoPlayerDiskCacheMaxSizeKey
import com.stechtricker.youplay.utils.rememberPreference

@OptIn(ExperimentalCoilApi::class)
@ExperimentalAnimationApi
@Composable
fun CacheSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    var coilDiskCacheMaxSize by rememberPreference(
        coilDiskCacheMaxSizeKey,
        CoilDiskCacheMaxSize.`128MB`
    )
    var exoPlayerDiskCacheMaxSize by rememberPreference(
        exoPlayerDiskCacheMaxSizeKey,
        ExoPlayerDiskCacheMaxSize.`2GB`
    )

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        Header(title = "Cache")

        SettingsDescription(text = "When the cache runs out of space, the resources that haven't been accessed for the longest time are cleared")

        Coil.imageLoader(context).diskCache?.let { diskCache ->
            val diskCacheSize = remember(diskCache) {
                diskCache.size
            }

            SettingsGroupSpacer()

            SettingsEntryGroupText(title = "IMAGE CACHE")

            SettingsDescription(
                text = "${
                    Formatter.formatShortFileSize(
                        context,
                        diskCacheSize
                    )
                } used (${diskCacheSize * 100 / coilDiskCacheMaxSize.bytes.coerceAtLeast(1)}%)"
            )

            EnumValueSelectorSettingsEntry(
                title = "Max size",
                selectedValue = coilDiskCacheMaxSize,
                onValueSelected = { coilDiskCacheMaxSize = it }
            )
        }

        binder?.cache?.let { cache ->
            val diskCacheSize by remember {
                derivedStateOf {
                    cache.cacheSpace
                }
            }

            SettingsGroupSpacer()

            SettingsEntryGroupText(title = "SONG CACHE")

            SettingsDescription(
                text = buildString {
                    append(Formatter.formatShortFileSize(context, diskCacheSize))
                    append(" used")
                    when (val size = exoPlayerDiskCacheMaxSize) {
                        ExoPlayerDiskCacheMaxSize.Unlimited -> {}
                        else -> append(" (${diskCacheSize * 100 / size.bytes}%)")
                    }
                }
            )

            EnumValueSelectorSettingsEntry(
                title = "Max size",
                selectedValue = exoPlayerDiskCacheMaxSize,
                onValueSelected = { exoPlayerDiskCacheMaxSize = it }
            )
        }
    }
}
