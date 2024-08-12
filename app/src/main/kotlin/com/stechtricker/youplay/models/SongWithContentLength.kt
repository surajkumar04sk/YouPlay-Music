package com.stechtricker.youplay.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded

@Immutable
data class SongWithContentLength(
    @Embedded val song: Song,
    val contentLength: Long?
)
