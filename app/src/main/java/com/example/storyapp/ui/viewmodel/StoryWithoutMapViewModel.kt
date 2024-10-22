package com.example.storyapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.local.database.ListStoryItem
import com.example.storyapp.data.repository.StoryRepository

class StoryWithoutMapViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun stories(token: String): LiveData<PagingData<ListStoryItem>> {
        return storyRepository.getPaginatedStories(token).cachedIn(viewModelScope)
    }
}