package com.pedro.tasklist

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** O arquivo fica na área interna do app. Leituras e gravações ocorrem em IO. */
class TaskRepository(context: Context) {
    private val file = File(context.filesDir, "tasks.json")
    private val mutex = Mutex()
    private val data = MutableStateFlow<List<Task>>(emptyList())
    val tasks = data.asStateFlow()

    suspend fun load() = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!file.exists()) { data.value = emptyList(); return@withLock }
            val json = JSONArray(file.readText())
            data.value = (0 until json.length()).map { index ->
                val item = json.getJSONObject(index)
                Task(item.getLong("id"), item.getString("title"), item.optString("description").ifBlank { null })
            }
        }
    }

    suspend fun save(title: String, description: String?) = withContext(Dispatchers.IO) {
        mutex.withLock {
            require(title.isNotBlank() && title.length <= 80)
            require(description == null || description.length <= 500)
            val next = data.value + Task((data.value.maxOfOrNull { it.id } ?: 0) + 1, title, description)
            write(next)
            data.value = next
        }
    }

    private fun write(tasks: List<Task>) {
        val array = JSONArray()
        tasks.forEach { task ->
            array.put(JSONObject().put("id", task.id).put("title", task.title)
                .put("description", task.description ?: ""))
        }
        val temporary = File(file.parentFile, "tasks.tmp")
        temporary.writeText(array.toString())
        if (!temporary.renameTo(file)) {
            temporary.delete()
            error("Não foi possível salvar as tarefas")
        }
    }
}
