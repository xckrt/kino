package com.example.kino.screens

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kino.components.TicketCard
import com.example.kino.data.TicketWithMovie
import com.example.kino.dialogs.QrZoomDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    tickets: List<TicketWithMovie>,
    onBackClick: () -> Unit,
    onPayClick: (String) -> Unit,
    onCancelClick: (String) -> Unit,
    onLeaveReviewClick: (Int, String) -> Unit
) {
    var expandedQrData by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Мои билеты",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (expandedQrData != null) {
            QrZoomDialog(
                qrData = expandedQrData!!,
                onDismiss = { expandedQrData = null }
            )
        }

        if (tickets.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "У вас пока нет билетов",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tickets.reversed(), key = { it.ticket.id }) { item ->
                    TicketCard(
                        item = item,
                        onPayClick = { onPayClick(item.ticket.id) },
                        onCancelClick = { onCancelClick(item.ticket.id) },
                        onQrClick = { expandedQrData = it },
                        onReviewClick = { onLeaveReviewClick(item.movie.filmId, item.movie.title) }
                    )
                }
            }
        }
    }
}