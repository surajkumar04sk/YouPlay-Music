package com.stechtricker.innertube.models.bodies

import com.stechtricker.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlayerBody(
    val context: Context = Context.DefaultAndroid,
    val videoId: String,
    val playlistId: String? = null
)
