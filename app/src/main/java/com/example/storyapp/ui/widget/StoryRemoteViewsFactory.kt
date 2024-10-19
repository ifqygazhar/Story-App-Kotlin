package com.example.storyapp.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.storyapp.R
import com.example.storyapp.data.Result
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.di.Injection
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking

class StoryRemoteViewsFactory(private val context: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private val storyList = mutableListOf<ListStoryItem>()
    private val storyRepository: StoryRepository = Injection.storyRepository(context)

    override fun onCreate() {
        // Inisialisasi jika diperlukan
    }

    override fun onDataSetChanged() {
        runBlocking {
            val token = UserPreferences(context).getToken()
            if (token != null) {
                val result =
                    storyRepository.getAllStories(token, page = null, size = null, location = 0)
                when (result) {
                    is Result.Success -> {
                        storyList.clear()
                        storyList.addAll(result.data.listStory)
                    }

                    is Result.Error -> {
                        storyList.clear()
                    }

                    is Result.Loading -> {}
                }
            } else {
                storyList.clear()
            }
        }
    }

    override fun onDestroy() {
        storyList.clear()
    }

    override fun getCount(): Int {
        return storyList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.story_app_widget)

        if (storyList.isEmpty()) {
            views.setViewVisibility(R.id.empty_view, View.VISIBLE)
            views.setViewVisibility(R.id.stack_view, View.GONE)
            return views
        }

        views.setViewVisibility(R.id.empty_view, View.GONE)
        views.setViewVisibility(R.id.stack_view, View.VISIBLE)

        val story = storyList[position]
        val itemViews = RemoteViews(context.packageName, R.layout.story_widget_item)

        val imageUrl = story.photoUrl
        val bitmap = getBitmapFromUrl(context, imageUrl)

        if (bitmap != null) {
            itemViews.setImageViewBitmap(R.id.imageView, bitmap)
        } else {
            itemViews.setImageViewResource(R.id.imageView, R.drawable.ic_placeholder)
        }

        return itemViews
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}

private fun getBitmapFromUrl(context: Context, url: String): Bitmap? {
    return try {
        Picasso.get()
            .load(url)
            .get()
    } catch (e: Exception) {
        null
    }
}
