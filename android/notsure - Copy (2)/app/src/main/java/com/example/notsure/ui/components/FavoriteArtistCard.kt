package com.example.notsure.ui.components

//import androidx.compose.material3.Card
//import androidx.compose.runtime.Composable
//import com.example.notsure.data.model.FavoriteArtist
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Text
//import androidx.compose.material3.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
import androidx.navigation.*
import coil.compose.AsyncImage
import java.util.*
import androidx.compose.ui.res.painterResource
import com.example.notsure.R

//@Composable
//fun FavoriteArtistCard(artist: FavoriteArtist, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .padding(end = 12.dp)
//            .width(160.dp)
//            .clickable { onClick() },
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column {
//            AsyncImage(
//                model = artist.image ?: R.drawable.artsy_logo,
//                contentDescription = artist.artistName,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(120.dp),
//                error = painterResource(R.drawable.artsy_logo)
//            )
//
//            Text(
//                artist.artistName,
//                modifier = Modifier.padding(8.dp),
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//    }
//}

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notsure.data.model.FavoriteArtist
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
@Composable
fun FavoriteArtistCard(
    artist: FavoriteArtist,
    onClick: () -> Unit
) {
    var now by remember {mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while(true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val relativeTime = getRelativeTime(artist.addedAt, now)

    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name?.takeIf{it.isNotBlank()}?: "Artist ID: ${artist.artistId}",
                style = MaterialTheme.typography.titleMedium)

            val subtitle = listOfNotNull(
                artist.nationality?.takeIf {it.isNotBlank() },
                artist.birthday?.takeIf{it.isNotBlank()}
            ).joinToString(", ")

            if (subtitle.isNotEmpty()){
                Spacer(modifier = Modifier.height(2.dp))
                Text(text =subtitle, style= MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (relativeTime.isNotBlank()){
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = relativeTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}



fun getRelativeTime(timestamp: String, nowMillis: Long): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val addedTime = format.parse(timestamp)
        val diff = (nowMillis-addedTime.time).coerceAtLeast(0)

        val seconds = diff/1000
        val minutes = seconds/60
        val hours = minutes/60
        val days = hours/24

        when {
            seconds<60 -> "$seconds second${if(seconds != 1L) "s" else ""} ago"
            minutes<60 -> "$minutes minute${if(minutes != 1L) "s" else ""} ago"
            hours<24 -> "$hours hour${if(hours != 1L) "s" else ""} ago"
            days<7 -> "$days day${if(days != 1L) "s" else ""} ago"
            else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(addedTime)
        }
    } catch (e: Exception) {
        timestamp
    }
}
