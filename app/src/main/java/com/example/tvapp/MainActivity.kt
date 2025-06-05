package com.example.tvapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

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
    val uiState by viewModel.uiState.collectAsState()
    var time by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            time = java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())
            delay(1000)
        }
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text(time) })
    }) { padding ->
        Column(Modifier.padding(padding)) {
            Text(text = uiState.message, style = MaterialTheme.typography.h5, modifier = Modifier.padding(16.dp))
            LazyColumn {
                items(uiState.playlist.items.size) { index ->
                    val item = uiState.playlist.items[index]
                    Button(onClick = { viewModel.play(item) }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(item.title)
                    }
                }
            }
            PlayerView(viewModel)
        }
    }
}
