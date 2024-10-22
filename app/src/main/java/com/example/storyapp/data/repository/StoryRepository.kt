package com.example.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyapp.data.Result
import com.example.storyapp.data.local.database.ListStoryItem
import com.example.storyapp.data.local.database.StoryDatabase
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

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase
) {

    fun getPaginatedStories(token: String): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false
            ),
            remoteMediator = StoryRemoteMediator(
                database = storyDatabase,
                apiService = apiService,
                token = token
            ),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

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

    suspend fun getAllStoriesWithMap(
        token: String,
        location: Int = 1
    ): Result<StoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllStoriesWithMap("Bearer $token", location)

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
            storyDatabase: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, storyDatabase)
            }.also { instance = it }
    }
}