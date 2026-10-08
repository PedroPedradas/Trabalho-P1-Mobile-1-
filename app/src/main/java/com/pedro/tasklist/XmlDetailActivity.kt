package com.pedro.tasklist

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.pedro.tasklist.databinding.ActivityXmlDetailBinding

class XmlDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityXmlDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.detailTitle.text = intent.getStringExtra("title") ?: "Tarefa sem título"
        binding.detailDescription.text = intent.getStringExtra("description")?.ifBlank { null } ?: "Sem descrição"
        binding.backButton.setOnClickListener { finish() }
    }
}
