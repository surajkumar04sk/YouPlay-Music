package com.stechtricker.youplay.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.stechtricker.youplay.BuildConfig
import com.stechtricker.youplay.LocalPlayerAwareWindowInsets
import com.stechtricker.youplay.ui.components.themed.Header
import com.stechtricker.youplay.ui.styling.LocalAppearance
import com.stechtricker.youplay.utils.secondary

@ExperimentalAnimationApi
@Composable
fun About() {
    val (colorPalette, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current

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
        Header(title = "About") {
            BasicText(
                text = "v${BuildConfig.VERSION_NAME} by stechtricker",
                style = typography.s.secondary
            )
        }

        SettingsEntryGroupText(title = "Created by")

        SettingsEntry(
            title = "TechRobusto",
            text = "Connect With Youtube",
            onClick = {
                uriHandler.openUri("https://www.youtube.com/@techrobusto2104")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "For App")

        SettingsEntry(
            title = "Share",
            text = "Share with Friends",
            onClick = {
                uriHandler.openUri("https://www.youtube.com/@techrobusto2104")
            }
        )
        SettingsEntry(
            title = "Rate",
            text = "Leave a Review",
            onClick = {
                uriHandler.openUri("market://details?id=com.stechtricker.youplay")
            }
        )



    }
}
