import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.example.storyapp.R
import com.example.storyapp.ui.widget.StoryAppWidget

fun updateStoryWidget(context: Context) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val componentName = ComponentName(context, StoryAppWidget::class.java)

    // Meminta pembaruan data untuk widget
    appWidgetManager.notifyAppWidgetViewDataChanged(
        appWidgetManager.getAppWidgetIds(componentName),
        R.id.stack_view
    )
}
