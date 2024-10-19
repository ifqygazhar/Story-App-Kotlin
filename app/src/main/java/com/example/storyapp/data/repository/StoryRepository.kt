package com.example.storyapp.data.repository

import com.example.storyapp.data.Result
import com.example.storyapp.data.remote.response.StoryDetailResponse
import com.example.storyapp.data.remote.response.StoryResponse
import com.example.storyapp.data.remote.response.StoryUploadResponse
import com.example.storyapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.IOException

class StoryRepository private constructor(private val apiService: ApiService) {

    suspend fun getAllStories(
        token: String,
        page: Int? = null,
        size: Int? = null,
        location: Int = 0
    ): Result<StoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllStories("Bearer $token", page, size, location)

                if (!response.error) {
                    Result.Success(response)
                } else {
                    Result.Error(response.message ?: "Unknown error occurred")
                }
            } catch (e: IOException) {
                Result.Error("Network error: ${e.message}")
            } catch (e: HttpException) {
                Result.Error("HTTP error: ${e.message}")
            } catch (e: Exception) {
                Result.Error("An unexpected error occurred: ${e.message}")
            }

        }
    }

    suspend fun getStory(token: String, id: String): Result<StoryDetailResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStory("Bearer $token", id)
                if (!response.error) {
                    Result.Success(response)
                } else {
                    Result.Error(response.message ?: "Unknown error occurred")
                }
            } catch (e: IOException) {
                Result.Error("Network error: ${e.message}")
            } catch (e: HttpException) {
                Result.Error("HTTP error: ${e.message}")
            } catch (e: Exception) {
                Result.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    suspend fun uploadStory(
        token: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ): Result<StoryUploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.uploadStory("Bearer $token", description, photo, lat, lon)

                if (!response.error) {
                    Result.Success(response)
                } else {
                    Result.Error(response.message ?: "Unknown error occurred")
                }
            } catch (e: IOException) {
                Result.Error("Network error: ${e.message}")
            } catch (e: HttpException) {
                Result.Error("HTTP error: ${e.message}")
            } catch (e: Exception) {
                Result.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService)
            }.also { instance = it }
    }
}