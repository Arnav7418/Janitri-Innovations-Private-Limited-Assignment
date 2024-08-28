package com.example.janitri

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.example.janitri.ui.theme.JanitriTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var database: ColorDatabase
    private lateinit var viewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            ColorDatabase::class.java,
            "colors_database"
        ).build()

        viewModel = MyViewModel(database, this)

        setContent {
            JanitriTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MyViewModel) {
    val context = LocalContext.current
    var colors by remember { mutableStateOf(emptyList<ColorItem>()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }


    LaunchedEffect(viewModel) {
        viewModel.getAllColors { updatedColors ->
            colors = updatedColors
        }
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(2000)
            showToast = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Color App") },
                actions = {
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = {
                                isRefreshing = true
                                viewModel.syncColors()
                                viewModel.getAllColors { updatedColors ->
                                    colors = updatedColors
                                    isRefreshing = false
                                    toastMessage = "Colors synced"
                                    showToast = true



                                }
                            }
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync Button"
                                )
                            }
                        }
                        if (!isRefreshing) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF5658A5),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.addColor()
                    viewModel.getAllColors { updatedColors ->
                        colors = updatedColors
                        toastMessage = "New color added"
                        showToast = true
                    }
                },
                icon = { Icon(Icons.Filled.AddCircle, "Add color") },
                text = { Text("Add color") },
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize()
            ) {
                items(colors) { colorItem ->
                    ColorCard(code = colorItem.code, time = colorItem.time)
                }
            }

            AnimatedVisibility(
                visible = showToast,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = toastMessage,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}
@Composable
fun ColorCard(code: String, time: Long) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1.5f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(code)))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "Created at\n${formatDate(time)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}