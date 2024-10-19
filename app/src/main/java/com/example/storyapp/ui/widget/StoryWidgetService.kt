package com.example.storyapp.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService

class StoryWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return StoryRemoteViewsFactory(applicationContext)
    }
}
