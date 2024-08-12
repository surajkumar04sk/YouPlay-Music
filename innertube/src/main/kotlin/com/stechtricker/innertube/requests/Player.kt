package com.stechtricker.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import com.stechtricker.innertube.Innertube
import com.stechtricker.innertube.models.Context
import com.stechtricker.innertube.models.PlayerResponse
import com.stechtricker.innertube.models.bodies.PlayerBody
import com.stechtricker.innertube.utils.runCatchingNonCancellable
import kotlinx.serialization.Serializable

suspend fun Innertube.player(body: PlayerBody) = runCatchingNonCancellable {
    val response = client.post(player) {
        setBody(body)
        mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
    }.body<PlayerResponse>()

    if (response.playabilityStatus?.status == "OK") {
        response
    } else {
        @Serializable
        data class AudioStream(
            val url: String,
            val bitrate: Long
        )

        @Serializable
        data class PipedResponse(
            val audioStreams: List<AudioStream>
        )

        val safePlayerResponse = client.post(player) {
            setBody(
                body.copy(
                    context = Context.DefaultAgeRestrictionBypass.copy(
                        thirdParty = Context.ThirdParty(
                            embedUrl = "https://www.youtube.com/watch?v=${body.videoId}"
                        )
                    ),
                )
            )
            mask("playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId")
        }.body<PlayerResponse>()

        if (safePlayerResponse.playabilityStatus?.status != "OK") {
            return@runCatchingNonCancellable response
        }

        val audioStreams = client.get("https://watchapi.whatever.social/streams/${body.videoId}") {
            contentType(ContentType.Application.Json)
        }.body<PipedResponse>().audioStreams

        safePlayerResponse.copy(
            streamingData = safePlayerResponse.streamingData?.copy(
                adaptiveFormats = safePlayerResponse.streamingData.adaptiveFormats?.map { adaptiveFormat ->
                    adaptiveFormat.copy(
                        url = audioStreams.find { it.bitrate == adaptiveFormat.bitrate }?.url
                    )
                }
            )
        )
    }
}
