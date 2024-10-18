package com.example.storyapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.Result
import com.example.storyapp.data.remote.response.StoryDetailResponse
import com.example.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch

class DetailViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _story = MutableLiveData<StoryDetailResponse>()
    val story: LiveData<StoryDetailResponse> = _story

    fun getStory(token: String, id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = storyRepository.getStory(token, id)
            if (result is Result.Success) {
                _isLoading.value = false
                _story.value = result.data
            } else {
                _isLoading.value = false
                Result.Error("Failed to fetch story")
            }
        }
    }

}