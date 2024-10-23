package com.example.storyapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.databinding.FragmentStoryWithoutMapBinding
import com.example.storyapp.ui.adapter.LoadingStateAdapter
import com.example.storyapp.ui.adapter.StoryAdapterPaging
import com.example.storyapp.ui.viewmodel.StoryWithoutMapViewModel
import com.example.storyapp.ui.viewmodel.factory.StoryWithoutMapViewModelFactory
import com.example.storyapp.util.showToast

class StoryWithoutMapFragment : Fragment() {

    private val storyWithoutMapViewModel: StoryWithoutMapViewModel by viewModels {
        StoryWithoutMapViewModelFactory.getInstance(requireActivity())
    }
    private lateinit var storyAdapter: StoryAdapterPaging
    private var _binding: FragmentStoryWithoutMapBinding? = null
    private val binding get() = _binding!!

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            storyAdapter.refresh()
            storyAdapter.addLoadStateListener { loadState ->
                if (loadState.source.refresh is LoadState.NotLoading) {
                    binding.rvStory.smoothScrollToPosition(0)
                }
            }
        }
    }

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
            addStoryLauncher.launch(intent)
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
        storyAdapter.addLoadStateListener { loadState ->
            binding.progressBar.visibility =
                if (loadState.source.refresh is LoadState.Loading) View.VISIBLE else View.GONE

            binding.imgEmpty.visibility =
                if (loadState.source.refresh is LoadState.NotLoading && storyAdapter.itemCount == 0) View.VISIBLE else View.GONE


            val errorState = loadState.source.refresh as? LoadState.Error
                ?: loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error

            errorState?.let {
                it.error.localizedMessage?.let { it1 -> showToast(requireContext(), it1) }
            }
        }


        observeViewModel()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        val token = UserPreferences(requireContext()).getToken()
        storyWithoutMapViewModel.stories(token = token!!)
            .observe(viewLifecycleOwner) { pagingData ->
                storyAdapter.submitData(lifecycle, pagingData)
            }
    }
}


