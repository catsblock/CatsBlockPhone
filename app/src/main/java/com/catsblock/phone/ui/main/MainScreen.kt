package com.catsblock.phone.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catsblock.phone.ui.dialer.DialerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Pesquisar contatos ou histórico...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        singleLine = true
                    )
                },
                actions = {
                    IconButton(onClick = { /* Abrir painel de configurações */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("Principal") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("Teclado") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> CallHistoryListContent()
                1 -> DialerScreen()
            }
        }
    }
}

@Composable
fun CallHistoryListContent() {
    val dummyCalls = listOf(
        CallRecordItem("João Silva", "(11) 98765-4321", "14:32", true, "Ligação atendida"),
        CallRecordItem("Maria Souza", "(11) 91234-5678", "12:15", false, "Ligação Caiu", true),
        CallRecordItem("Desconhecido", "(11) 99888-7766", "10:00", false, "Ligação Não atendida", false)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "T", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Telefone",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "(11) 90000-0000",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dummyCalls) { call ->
                CallCardItem(call = call)
            }
        }
    }
}

data class CallRecordItem(
    val name: String,
    val number: String,
    val time: String,
    val isAccepted: Boolean,
    val statusText: String,
    val hasAudio: Boolean = false
)

@Composable
fun CallCardItem(call: CallRecordItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Abre detalhes da chamada */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (call.isAccepted) "↗" else "↙",
                color = if (call.isAccepted) MaterialTheme.colorScheme.primary else Color.Red,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1.dp)) {
                Text(text = call.name, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${call.statusText} • ${call.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (call.isAccepted) MaterialTheme.colorScheme.onSurfaceVariant else Color.Red
                )
            }
            if (call.hasAudio) {
                TextButton(onClick = { /* Abre painel flutuante de áudio */ }) {
                    Text(text = "🎧 Áudio")
                }
            }
        }
    }
}
