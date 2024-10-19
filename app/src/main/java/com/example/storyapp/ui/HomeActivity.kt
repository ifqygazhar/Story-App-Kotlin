package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.data.Result
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.ActivityHomeBinding
import com.example.storyapp.ui.adapter.StoryAdapter
import com.example.storyapp.ui.viewmodel.HomeViewModel
import com.example.storyapp.ui.viewmodel.factory.HomeViewModelFactory
import com.example.storyapp.util.showToast

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var doubleBackToExitPressedOnce = false
    private lateinit var storyAdapter: StoryAdapter
    private var token: String? = null


    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includeToolbar.toolbar)


        setupRecyclerView()
        observeViewModel()


        token = UserPreferences(this).getToken()
        homeViewModel.getAllStories(token!!)

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
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

    override fun onResume() {
        super.onResume()
        homeViewModel.getAllStories(token!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                UserPreferences(this).clearToken()
                showToast(this, getString(R.string.logout_success))
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }, 1000)

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter(listOf()) { storyId ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("storyId", storyId)
            startActivity(intent)
        }
        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = storyAdapter
        }
    }


    private fun updateStories(stories: List<ListStoryItem>) {
        storyAdapter = StoryAdapter(stories) { storyId ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("STORY_ID", storyId)
            startActivity(intent)
        }
        binding.rvStory.adapter = storyAdapter
    }

    private fun observeViewModel() {

        homeViewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressBar()
            } else {
                hideProgressBar()
            }
        })

        homeViewModel.stories.observe(this, Observer { result ->
            when (result) {
                is Result.Success -> {
                    val stories = result.data
                    hideProgressBar()
                    if (stories.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                        updateStories(stories)
                    }

                }

                is Result.Loading -> {
                    showProgressBar()
                }

                is Result.Error -> {
                    hideProgressBar()
                    showToast(this, result.error)
                }
            }
        })
    }

    private fun showProgressBar() {
        binding.rvStory.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.rvStory.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.rvStory.visibility = View.GONE
        binding.imgEmpty.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.rvStory.visibility = View.VISIBLE
        binding.imgEmpty.visibility = View.GONE
    }


}