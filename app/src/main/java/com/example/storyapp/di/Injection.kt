package com.example.storyapp.di

import android.content.Context
import com.example.storyapp.data.remote.retrofit.ApiConfig
import com.example.storyapp.data.repository.AuthRepository

object Injection {

    fun authRepository(context: Context): AuthRepository {
        val apiService = ApiConfig.getApiService()
        return AuthRepository.getInstance(apiService)
    }
    
}