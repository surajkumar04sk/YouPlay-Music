package com.stechtricker.innertube.models.bodies

import com.stechtricker.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBody(
    val context: Context = Context.DefaultWeb,
    val continuation: String,
)
