package com.example.storyapp.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.storyapp.R
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.ui.viewmodel.DetailViewModel
import com.example.storyapp.ui.viewmodel.factory.DetailViewModelFactory
import com.example.storyapp.util.LoadImage
import com.example.storyapp.util.formatDate
import com.example.storyapp.util.showToast

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val detailViewModel: DetailViewModel by viewModels {
        DetailViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includeToolbar.toolbar)
        supportActionBar?.title = getString(R.string.detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val storyId = intent.getStringExtra("STORY_ID")
        val token = UserPreferences(this).getToken()

        if (storyId != null && token != null) {
            detailViewModel.getStory(token, storyId)
        } else {
            showToast(this, getString(R.string.invalid_story_id_or_token))
            finish()
        }

        observeViewModel()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun observeViewModel() {

        detailViewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressBar()
            } else {
                hideProgressBar()
            }
        })

        detailViewModel.story.observe(this, Observer { storyDetail ->
            if (storyDetail != null) {
                binding.apply {
                    LoadImage.load(
                        this@DetailActivity,
                        ivPicture,
                        storyDetail.story.photoUrl,
                        R.color.placeholder
                    )
                    binding.includeRectangle.tvDate.text = formatDate(storyDetail.story.createdAt)
                    binding.includeRectangle.tvName.text = storyDetail.story.name
                    tvDescription.text = storyDetail.story.description
                }
            }
        })
    }

    private fun showProgressBar() {
        binding.ivPicture.visibility = View.GONE
        binding.includeRectangle.cardDate.visibility = View.GONE
        binding.includeRectangle.cardName.visibility = View.GONE
        binding.includeRectangle.tvDate.visibility = View.GONE
        binding.includeRectangle.tvName.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.ivPicture.visibility = View.VISIBLE
        binding.includeRectangle.cardDate.visibility = View.VISIBLE
        binding.includeRectangle.cardName.visibility = View.VISIBLE
        binding.includeRectangle.tvDate.visibility = View.VISIBLE
        binding.includeRectangle.tvName.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }
}