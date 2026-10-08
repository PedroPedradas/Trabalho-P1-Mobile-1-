package com.pedro.tasklist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

@Serializable data object TasksRoute : NavKey
@Serializable data object NewTaskRoute : NavKey
@Serializable data class DetailRoute(val id: Long) : NavKey

class MainActivity : ComponentActivity() {
    private val model: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val state by model.state.collectAsStateWithLifecycle()
                val stack = rememberNavBackStack(TasksRoute)
                NavDisplay(
                    backStack = stack,
                    onBack = { if (stack.size > 1) stack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<TasksRoute> {
                            ListScreen(state, onNew = { stack.add(NewTaskRoute) },
                                onOpen = { stack.add(DetailRoute(it)) },
                                onXml = { startActivity(Intent(this@MainActivity, XmlListActivity::class.java)) },
                                onRetry = model::retry)
                        }
                        entry<DetailRoute> { route ->
                            val task = (state as? UiState.Content)?.tasks?.firstOrNull { it.id == route.id }
                            DetailScreen(task, onBack = { stack.removeLastOrNull() })
                        }
                        entry<NewTaskRoute> {
                            FormScreen(onBack = { stack.removeLastOrNull() }, onSave = { title, description, onError ->
                                model.create(title, description,
                                    onSuccess = { stack.removeLastOrNull() }, onError = onError)
                            })
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ListScreen(state: UiState, onNew: () -> Unit, onOpen: (Long) -> Unit,
                       onXml: () -> Unit, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp)) {
        Text("Minhas tarefas", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNew, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("+ Nova tarefa") }
        TextButton(onClick = onXml, modifier = Modifier.heightIn(min = 48.dp)) { Text("Ver demonstração XML") }
        when (state) {
            UiState.Loading -> { CircularProgressIndicator(); Text("Carregando tarefas…") }
            UiState.Empty -> Text("Nenhuma tarefa ainda. Toque em Nova tarefa para começar.")
            is UiState.Error -> { Text(state.message); Button(onClick = onRetry) { Text("Tentar novamente") } }
            is UiState.Content -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(state.tasks, key = { it.id }) { task ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onOpen(task.id) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(task.title, style = MaterialTheme.typography.titleMedium)
                            Text(task.description ?: "Sem descrição")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailScreen(task: Task?, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Detalhes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        if (task == null) Text("Tarefa não encontrada ou ainda carregando.") else {
            Text(task.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text(task.description ?: "Sem descrição")
            Spacer(Modifier.height(12.dp))

        }
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Voltar") }
    }
}

@Composable
// Os campos usam rememberSaveable para sobreviver à rotação e recriação.
private fun FormScreen(onBack: () -> Unit, onSave: (String, String?, () -> Unit) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Nova tarefa", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = title, onValueChange = { title = it.take(80); saveFailed = false },
            label = { Text("Título") }, singleLine = true, isError = attempted && title.isBlank(),
            supportingText = { if (attempted && title.isBlank()) Text("O título é obrigatório") },
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it.take(500) },
            label = { Text("Descrição (opcional)") }, modifier = Modifier.fillMaxWidth())
        if (saveFailed) Text("Não foi possível salvar. Tente novamente.", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(enabled = !saving, onClick = {
            attempted = true
            if (title.isNotBlank()) {
                saving = true
                onSave(title, description) { saving = false; saveFailed = true }
            }
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (saving) "Salvando…" else "Salvar") }
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancelar") }
    }
}
