package com.pedro.tasklist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pedro.tasklist.databinding.ActivityXmlListBinding
import com.pedro.tasklist.databinding.ItemXmlTaskBinding

class XmlListActivity : ComponentActivity() {
    private lateinit var binding: ActivityXmlListBinding
    private val sampleTasks = listOf(
        Task(1, "Estudar Mobile", "Revisar XML e Kotlin"),
        Task(2, "Fazer atividade", null),
        Task(3, "Ler README", "Conferir como executar o projeto")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXmlListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.taskList.layoutManager = LinearLayoutManager(this)
        binding.taskList.adapter = TaskAdapter(sampleTasks) { task ->
            startActivity(Intent(this, XmlDetailActivity::class.java).apply {
                putExtra("title", task.title)
                putExtra("description", task.description)
            })
        }
    }
}

private class TaskAdapter(private val tasks: List<Task>, private val open: (Task) -> Unit) :
    RecyclerView.Adapter<TaskAdapter.Holder>() {
    class Holder(val binding: ItemXmlTaskBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemXmlTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun getItemCount() = tasks.size
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val task = tasks[position]
        holder.binding.taskTitle.text = task.title
        holder.binding.taskStatus.text = task.description ?: "Sem descrição"
        holder.binding.root.setOnClickListener { open(task) }
    }
}
