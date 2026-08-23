package com.example.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.Track

object ArtworkUtils {

    fun getTrackArtworkBitmap(context: Context, track: Track): Bitmap? {
        // 1. Try from coverUrl (content:// or http:// or file://)
        if (track.coverUrl.isNotBlank()) {
            try {
                val uri = Uri.parse(track.coverUrl)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) return bitmap
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        // 2. Try from local audio URI with MediaMetadataRetriever
        if (track.localUri.isNotBlank()) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.parse(track.localUri))
                val embeddedArt = retriever.embeddedPicture
                retriever.release()
                if (embeddedArt != null) {
                    val bitmap = BitmapFactory.decodeByteArray(embeddedArt, 0, embeddedArt.size)
                    if (bitmap != null) return bitmap
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        // 3. Fallback to drawable resource
        val res = track.coverDrawableRes ?: R.drawable.cover_cyberpunk_1787235201442
        return try {
            BitmapFactory.decodeResource(context.resources, res)
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun TrackCoverImage(
    track: Track?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val fallbackRes = track?.coverDrawableRes ?: R.drawable.cover_cyberpunk_1787235201442

    // If we have a coverUrl (album art URI or web URL) or localUri, use Coil
    val imageModel: Any = when {
        track != null && track.coverUrl.isNotBlank() -> track.coverUrl
        track != null && track.localUri.isNotBlank() -> track.localUri
        else -> fallbackRes
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageModel)
            .crossfade(true)
            .error(fallbackRes)
            .placeholder(fallbackRes)
            .build(),
        contentDescription = contentDescription ?: track?.title ?: "Cover",
        contentScale = contentScale,
        modifier = modifier
    )
}
