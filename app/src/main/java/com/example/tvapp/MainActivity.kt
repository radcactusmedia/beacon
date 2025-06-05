package com.example.tvapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: TvViewModel = viewModel()
            TvApp(viewModel)
        }
    }
}

@Composable
fun TvApp(viewModel: TvViewModel) {
    var messageIndex by remember { mutableStateOf(0) }
    var timeString by remember { mutableStateOf("") }
    val zone = ZoneId.of("America/Phoenix")

    LaunchedEffect(Unit) {
        while (true) {
            timeString = ZonedDateTime.now(zone).format(DateTimeFormatter.ofPattern("h:mma", java.util.Locale.US)).lowercase()
            delay(60_000)
        }
    }

    LaunchedEffect(messageIndex) {
        delay(600_000)
        messageIndex = (messageIndex + 1) % 3
    }

    val date = ZonedDateTime.now(zone)
    val daySuffix = when (val d = date.dayOfMonth) {
        11,12,13 -> "th"
        else -> when (d % 10) { 1 -> "st";2->"nd";3->"rd";else->"th" }
    }
    val dateString = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", java.util.Locale.US)) + daySuffix + ", " + date.year

    val message = when (messageIndex) {
        0 -> "The time is currently $timeString in Chandler, AZ"
        1 -> "You're shopping at Walmart 6480"
        else -> "Today is $dateString"
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(message) }, backgroundColor = androidx.compose.ui.graphics.Color(0xFF0053E2))
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            SlideshowPlayer(viewModel)
        }
    }
}
