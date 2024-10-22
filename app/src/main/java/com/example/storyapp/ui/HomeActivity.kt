package com.example.storyapp.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storyapp.R
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.databinding.ActivityHomeBinding
import com.example.storyapp.ui.widget.StoryAppWidget
import com.example.storyapp.util.showToast

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var doubleBackToExitPressedOnce = false
    private var token: String? = null

    private val SELECTED_FRAGMENT_KEY = "selected_fragment"
    private var selectedFragmentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includeToolbar.toolbar)

        token = UserPreferences(this).getToken()
        loadWidget()

        if (savedInstanceState != null) {
            selectedFragmentIndex = savedInstanceState.getInt(SELECTED_FRAGMENT_KEY, 0)
        } else {
            selectedFragmentIndex = 0
        }

        loadFragment(selectedFragmentIndex)

        binding.bottomBar.itemActiveIndex = selectedFragmentIndex
        
        binding.bottomBar.setOnItemSelectedListener { position ->
            if (position != selectedFragmentIndex) {
                selectedFragmentIndex = position
                loadFragment(selectedFragmentIndex)
            }
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }

                doubleBackToExitPressedOnce = true
                showToast(this@HomeActivity, getString(R.string.press_again_for_exit))
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return true
    }

    private fun loadFragment(index: Int) {
        val selectedFragment = when (index) {
            0 -> StoryWithoutMapFragment()
            1 -> StoryMapFragment()
            else -> StoryWithoutMapFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment, selectedFragment)
            .commit()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                UserPreferences(this).clearToken()
                val appWidgetManager = AppWidgetManager.getInstance(this)
                val componentName = ComponentName(this, StoryAppWidget::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                Log.d("HomeActivity", "Widget IDs: ${ids.joinToString()}")
                appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view)

                // Mengirim broadcast pembaruan widget
                val updateIntent = Intent(this, StoryAppWidget::class.java)
                updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                sendBroadcast(updateIntent)


                showToast(this, getString(R.string.logout_success))
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }, 1000)

                true
            }

            R.id.menu_setting -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun loadWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, StoryAppWidget::class.java)
        val ids = appWidgetManager.getAppWidgetIds(componentName)
        Log.d("HomeActivity", "Widget IDs: ${ids.joinToString()}")
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view)
        val updateIntent = Intent(this, StoryAppWidget::class.java)
        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(updateIntent)
    }


}