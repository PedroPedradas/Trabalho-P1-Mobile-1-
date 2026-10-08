package com.pedro.tasklist

// val impede alterações no objeto. A descrição pode ser null.
data class Task(val id: Long, val title: String, val description: String?)
