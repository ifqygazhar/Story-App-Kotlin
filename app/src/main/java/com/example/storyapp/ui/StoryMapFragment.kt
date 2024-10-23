package com.example.storyapp.ui

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.storyapp.R
import com.example.storyapp.data.Result
import com.example.storyapp.data.pref.UserPreferences
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.FragmentStoryMapBinding
import com.example.storyapp.ui.viewmodel.StoryMapViewModel
import com.example.storyapp.ui.viewmodel.factory.StoryMapViewModelFactory
import com.example.storyapp.util.showToast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class StoryMapFragment : Fragment(), OnMapReadyCallback {

    private val storyMapViewModel: StoryMapViewModel by viewModels {
        StoryMapViewModelFactory.getInstance(requireActivity())
    }
    private lateinit var mMap: GoogleMap

    private var _binding: FragmentStoryMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStoryMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupObservers()

        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), AddStoryActivity::class.java)
            startActivity(intent)
        }

        val token = UserPreferences(requireContext()).getToken()
        token?.let {
            storyMapViewModel.getAllStoriesWithMap(it)
        } ?: run {
            showToast(requireContext(), "Token tidak tersedia")
        }
    }

    private fun setupObservers() {
        storyMapViewModel.stories.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val stories = result.data
                    if (stories.isNotEmpty()) {
                        addManyMarker(stories)
                    } else {
                        Toast.makeText(context, "Tidak ada data cerita", Toast.LENGTH_SHORT).show()
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Opsional: Atur pengaturan peta seperti jenis peta, zoom level awal, dll.
        mMap.uiSettings.isZoomControlsEnabled = true
    }

    private fun addManyMarker(stories: List<ListStoryItem>) {
        if (::mMap.isInitialized) {
            val boundsBuilder = LatLngBounds.Builder()

            stories.forEach { story ->
                val lat = story.lat
                val lon = story.lon
                if (lat != null && lon != null) {
                    val latLng = LatLng(lat as Double, lon as Double)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(story.name)
                            .snippet(story.description)
                    )
                    boundsBuilder.include(latLng)
                }
            }


            val bounds = boundsBuilder.build()
            setMapStyle()
            val padding = 100
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        } else {
            Toast.makeText(context, "Map belum siap", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                    )
                )
            if (!success) {
                Log.e("StoryMapFragment", "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e("StoryMapFragment", "Can't find style. Error: ", exception)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
