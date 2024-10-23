import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.storyapp.ui.AddStoryActivity

abstract class AddStoryActivityContract : ActivityResultContract<Intent, Int>() {
    override fun createIntent(context: Context, input: Intent): Intent {
        return input ?: Intent(context, AddStoryActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Int {
        return resultCode
    }
}