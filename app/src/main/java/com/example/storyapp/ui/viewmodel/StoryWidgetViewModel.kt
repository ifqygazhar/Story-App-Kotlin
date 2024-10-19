package com.example.storyapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.Result
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch

class StoryWidgetViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _stories = MutableLiveData<Result<List<ListStoryItem>>>()
    val stories: LiveData<Result<List<ListStoryItem>>> = _stories

    fun getAllStories(token: String, page: Int? = null, size: Int? = null, location: Int = 0) {
        viewModelScope.launch {
            _stories.value =
                storyRepository.getAllStories(token, page, size, location).let { result ->
                    if (result is Result.Success) {
                        Result.Success(result.data.listStory)
                    } else {
                        Result.Error("Failed to fetch stories")
                    }
                }
        }
    }
}
