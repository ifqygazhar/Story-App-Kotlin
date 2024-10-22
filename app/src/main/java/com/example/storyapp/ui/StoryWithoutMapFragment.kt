package com.example.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.databinding.FragmentStoryWithoutMapBinding
import com.example.storyapp.ui.adapter.LoadingStateAdapter
import com.example.storyapp.ui.adapter.StoryAdapterPaging
import com.example.storyapp.ui.viewmodel.StoryWithoutMapViewModel
import com.example.storyapp.ui.viewmodel.factory.StoryWithoutMapViewModelFactory

class StoryWithoutMapFragment : Fragment() {

    private val storyWithoutMapViewModel: StoryWithoutMapViewModel by viewModels {
        StoryWithoutMapViewModelFactory.getInstance(requireActivity())
    }
    private lateinit var storyAdapter: StoryAdapterPaging
    private var _binding: FragmentStoryWithoutMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoryWithoutMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()


        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), AddStoryActivity::class.java)
            startActivity(intent)
        }


    }

    private fun setupRecyclerView() {
        binding.rvStory.layoutManager = LinearLayoutManager(requireContext())
        storyAdapter = StoryAdapterPaging()
        binding.rvStory.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                storyAdapter.retry()
            }
        )
        val token = UserPreferences(requireContext()).getToken()
        storyWithoutMapViewModel.stories(token = token!!)
            .observe(viewLifecycleOwner) { pagingData ->
                storyAdapter.submitData(lifecycle, pagingData)
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
