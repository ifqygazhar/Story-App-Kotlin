package com.example.storyapp.ui.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.ItemStoryBinding
import com.example.storyapp.ui.DetailActivity
import com.example.storyapp.util.LoadImage
import com.example.storyapp.util.formatDate

class StoryAdapter(
    private val context: Context,
    private val storyList: List<ListStoryItem>,

    ) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(
        private val context: Context,
        private val binding: ItemStoryBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.tvName.text = story.name
            binding.tvDescription.text = story.description
            binding.tvDate.text = formatDate(story.createdAt)
            LoadImage.load(
                binding.root.context,
                binding.ivPicture,
                story.photoUrl,
                R.color.placeholder,
            )
            binding.root.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("STORY_ID", story.id)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        binding.root.context as Activity,
                        Pair(binding.ivPicture, "profile"),
                        Pair(binding.tvName, "name"),
                        Pair(binding.tvDescription, "description"),
                    )
                context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(context, binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]
        holder.bind(story)
    }

    override fun getItemCount(): Int {
        return storyList.size
    }
}
