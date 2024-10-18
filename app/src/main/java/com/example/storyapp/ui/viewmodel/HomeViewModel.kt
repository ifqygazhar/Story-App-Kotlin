package com.example.storyapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.Result
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _stories = MutableLiveData<Result<List<ListStoryItem>>>()
    val stories: LiveData<Result<List<ListStoryItem>>> = _stories

    fun getAllStories(token: String, page: Int? = null, size: Int? = null, location: Int = 0) {
        _isLoading.value = true
        viewModelScope.launch {
            _stories.value =
                storyRepository.getAllStories(token, page, size, location).let { result ->
                    if (result is Result.Success) {
                        _isLoading.value = false
                        Result.Success(result.data.listStory)
                    } else {
                        _isLoading.value = false
                        Result.Error("Failed to fetch stories")
                    }
                }
        }
    }
}