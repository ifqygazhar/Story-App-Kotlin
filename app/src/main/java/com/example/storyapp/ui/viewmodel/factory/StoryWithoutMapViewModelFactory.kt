package com.example.storyapp.ui.viewmodel.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.di.Injection
import com.example.storyapp.ui.viewmodel.StoryWithoutMapViewModel

class StoryWithoutMapViewModelFactory(
    
    private val storyRepository: StoryRepository,

    ) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryWithoutMapViewModel::class.java)) {
            return StoryWithoutMapViewModel(storyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: StoryWithoutMapViewModelFactory? = null

        fun getInstance(context: Context): StoryWithoutMapViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: StoryWithoutMapViewModelFactory(
                    Injection.storyRepository(context),
                )
            }.also { instance = it }
    }
}