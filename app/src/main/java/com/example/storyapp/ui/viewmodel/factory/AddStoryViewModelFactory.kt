package com.example.storyapp.ui.viewmodel.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.di.Injection
import com.example.storyapp.ui.viewmodel.AddStoryViewModel

class AddStoryViewModelFactory(
    private val storyRepository: StoryRepository,

    ) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddStoryViewModel::class.java)) {
            return AddStoryViewModel(storyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: AddStoryViewModelFactory? = null

        fun getInstance(context: Context): AddStoryViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: AddStoryViewModelFactory(
                    Injection.storyRepository(context),
                )
            }.also { instance = it }
    }
}