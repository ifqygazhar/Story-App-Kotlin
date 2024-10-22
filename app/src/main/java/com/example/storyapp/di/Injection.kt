package com.example.storyapp.di

import android.content.Context
import com.example.storyapp.data.local.database.StoryDatabase
import com.example.storyapp.data.remote.retrofit.ApiConfig
import com.example.storyapp.data.repository.AuthRepository
import com.example.storyapp.data.repository.StoryRepository

object Injection {

    fun authRepository(context: Context): AuthRepository {
        val apiService = ApiConfig.getApiService()
        return AuthRepository.getInstance(apiService)
    }

    fun storyRepository(context: Context): StoryRepository {
        val apiService = ApiConfig.getApiService()
        val database = StoryDatabase.getDatabase(context)
        return StoryRepository.getInstance(apiService, database)
    }

}