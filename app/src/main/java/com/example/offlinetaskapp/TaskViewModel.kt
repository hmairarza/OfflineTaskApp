
package com.example.offlinetaskapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) :
    AndroidViewModel(application) {

    private val taskDao =
        TaskDatabase.getDatabase(application).taskDao()

    val tasks = taskDao.getAllTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addTask(
        title: String,
        description: String,
        deadline: String
    ) {
        viewModelScope.launch {
            taskDao.insertTask(
                Task(
                    title = title,
                    description = description,
                    deadline = deadline
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }
}
