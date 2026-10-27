
package com.example.offlinetaskapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinetaskapp.ui.theme.OfflineTaskAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OfflineTaskAppTheme {
                val taskViewModel: TaskViewModel = viewModel()

                TaskScreen(viewModel = taskViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var deleteTask by remember { mutableStateOf<Task?>(null) }

    val filteredTasks = tasks.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "TaskMate",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Pengelola Tugas Kuliah Offline",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    title = ""
                    description = ""
                    deadline = ""
                    editingTask = null
                    showAddForm = true
                }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Tugas Kuliahmu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Total tugas: ${tasks.size}")
                    Text("Selesai: ${tasks.count { it.isCompleted }}")
                    Text("Belum selesai: ${tasks.count { !it.isCompleted }}")
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cari tugas...") },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Daftar Tugas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("${filteredTasks.size} tugas")
            }

            Spacer(Modifier.height(8.dp))

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isBlank())
                            "Belum ada tugas.\nTekan + untuk menambahkan tugas."
                        else
                            "Tugas tidak ditemukan.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = {
                                            viewModel.updateTask(
                                                task.copy(isCompleted = it)
                                            )
                                        }
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            task.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            if (task.isCompleted) "Selesai"
                                            else "Belum selesai",
                                            color = if (task.isCompleted)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                if (task.deadline.isNotBlank()) {
                                    Text("Tenggat: ${task.deadline}")
                                }

                                if (task.description.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(task.description)
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedTask = task }
                                    ) {
                                        Text("Detail")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            editingTask = task
                                            title = task.title
                                            description = task.description
                                            deadline = task.deadline
                                            showAddForm = true
                                        }
                                    ) {
                                        Text("Edit")
                                    }

                                    TextButton(
                                        onClick = { deleteTask = task }
                                    ) {
                                        Text("Hapus")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddForm) {
        AlertDialog(
            onDismissRequest = { showAddForm = false },
            title = {
                Text(if (editingTask == null) "Tambah Tugas" else "Edit Tugas")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul tugas") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi") }
                    )
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Tenggat (contoh: 30/10/2026)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            val oldTask = editingTask

                            if (oldTask == null) {
                                viewModel.addTask(
                                    title.trim(),
                                    description.trim(),
                                    deadline.trim()
                                )
                            } else {
                                viewModel.updateTask(
                                    oldTask.copy(
                                        title = title.trim(),
                                        description = description.trim(),
                                        deadline = deadline.trim()
                                    )
                                )
                            }

                            showAddForm = false
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddForm = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    selectedTask?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTask = null },
            title = { Text("Detail Tugas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Judul: ${task.title}")
                    Text("Deskripsi: ${task.description.ifBlank { "-" }}")
                    Text("Tenggat: ${task.deadline.ifBlank { "-" }}")
                    Text(
                        "Status: ${
                            if (task.isCompleted) "Selesai"
                            else "Belum selesai"
                        }"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTask = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    deleteTask?.let { task ->
        AlertDialog(
            onDismissRequest = { deleteTask = null },
            title = { Text("Hapus Tugas?") },
            text = { Text("Tugas \"${task.title}\" akan dihapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(task)
                        deleteTask = null
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTask = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
