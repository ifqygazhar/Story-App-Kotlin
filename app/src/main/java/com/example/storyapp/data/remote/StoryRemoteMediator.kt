package com.example.storyapp.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.storyapp.data.local.database.ListStoryItem
import com.example.storyapp.data.local.database.RemoteEntity
import com.example.storyapp.data.local.database.StoryDatabase
import com.example.storyapp.data.remote.retrofit.ApiService

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService,
    private val token: String
) : RemoteMediator<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response =
                apiService.getAllStoriesPaging("Bearer $token", page, state.config.pageSize)
            Log.d("RemoteMediator", "Fetched ${response.size} stories from API for page $page")

            val endOfPaginationReached = response.isEmpty()
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    Log.d("RemoteMediator", "Clearing remote keys and stories in the database")
                    database.remoteDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                }

                val keys = response.map { story ->
                    RemoteEntity(
                        id = story.id,
                        prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1,
                        nextKey = if (endOfPaginationReached) null else page + 1
                    )
                }

                Log.d("RemoteMediator", "Inserting ${keys.size} remote keys into database")
                database.remoteDao().insertAll(keys)

                Log.d("RemoteMediator", "Inserting ${response.size} stories into database")
                database.storyDao().insertStory(response)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            Log.e("RemoteMediator", "Error fetching or saving stories: ${exception.message}")
            return MediatorResult.Error(exception)
        }

    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): RemoteEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { story ->
            database.remoteDao().getRemoteEntityId(story.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): RemoteEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { story ->
            database.remoteDao().getRemoteEntityId(story.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteDao().getRemoteEntityId(id)
            }
        }
    }
}
