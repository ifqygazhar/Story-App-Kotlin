package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.data.Result
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.databinding.FragmentStoryMapBinding
import com.example.storyapp.ui.adapter.StoryAdapter
import com.example.storyapp.ui.viewmodel.StoryMapViewModel
import com.example.storyapp.ui.viewmodel.factory.StoryMapViewModelFactory
import com.example.storyapp.util.showToast

class StoryMapFragment : Fragment() {

    private val storyMapViewModel: StoryMapViewModel by viewModels {
        StoryMapViewModelFactory.getInstance(requireActivity())
    }
    private lateinit var storyAdapter: StoryAdapter
    private var _binding: FragmentStoryMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoryMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), AddStoryActivity::class.java)
            startActivity(intent)
        }


        val token = UserPreferences(requireContext()).getToken()
        storyMapViewModel.getAllStoriesWithMap(token!!)
    }

    private fun setupRecyclerView() {
        binding.rvStory.layoutManager = LinearLayoutManager(requireContext())
        storyAdapter = StoryAdapter(requireContext(), emptyList())
        binding.rvStory.adapter = storyAdapter
    }

    private fun observeViewModel() {
        storyMapViewModel.stories.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val stories = result.data
                    if (stories.isNotEmpty()) {
                        storyAdapter = StoryAdapter(requireContext(), stories)
                        binding.rvStory.adapter = storyAdapter
                        binding.rvStory.visibility = View.VISIBLE
                        binding.imgEmpty.visibility = View.GONE
                    } else {
                        binding.rvStory.visibility = View.GONE
                        binding.imgEmpty.visibility = View.VISIBLE
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showToast(requireContext(), result.error)
                }
            }
        }


        storyMapViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}