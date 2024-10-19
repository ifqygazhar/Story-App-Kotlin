package com.example.storyapp.ui.viewmodel.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.di.Injection
import com.example.storyapp.ui.viewmodel.StoryWidgetViewModel

class StoryWidgetViewModelFactory(private val storyRepository: StoryRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryWidgetViewModel::class.java)) {
            return StoryWidgetViewModel(storyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: StoryWidgetViewModelFactory? = null

        fun getInstance(context: Context): StoryWidgetViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: StoryWidgetViewModelFactory(
                    Injection.storyRepository(context)
                )
            }.also { instance = it }
    }
}
