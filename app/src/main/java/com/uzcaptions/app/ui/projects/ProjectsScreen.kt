package com.uzcaptions.app.ui.projects

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uzcaptions.app.ui.common.formatTimeShort
import com.uzcaptions.app.ui.common.getDisplayName
import com.uzcaptions.app.ui.common.getVideoDurationMs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel,
    onOpenProject: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val context = LocalContext.current

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val duration = getVideoDurationMs(context, uri)
            val name = getDisplayName(context, uri)
            viewModel.createProject(name, uri.toString(), duration) { newId ->
                onOpenProject(newId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UzCaptions") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Sozlamalar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { pickVideoLauncher.launch(arrayOf("video/*")) }) {
                Icon(Icons.Default.Add, contentDescription = "Video qo'shish")
            }
        }
    ) { padding ->
        if (projects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null)
                Text(
                    "Hali loyihalar yo'q. Pastdagi + tugmasi orqali video yuklang, " +
                        "ilova o'zbekcha nutqni subtitrga aylantiradi.",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProject(project.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                Icon(Icons.Default.Movie, contentDescription = null)
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(project.title, fontWeight = FontWeight.Bold)
                                    Text(
                                        formatTimeShort(project.durationMs),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteProject(project) }) {
                                Icon(Icons.Default.Delete, contentDescription = "O'chirish")
                            }
                        }
                    }
                }
            }
        }
    }
}
