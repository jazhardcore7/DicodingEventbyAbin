package com.event.abin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.event.abin.databinding.FragmentHomeBinding
import com.event.abin.ui.EventAdapter
import com.event.abin.ui.EventItem
import com.event.abin.ui.EventViewModel
import com.event.abin.viewmodel.ViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventViewModel by viewModels { ViewModelFactory.getInstance(requireContext()) }

    private lateinit var upcomingEventAdapter: EventAdapter
    private lateinit var completedEventAdapter: EventAdapter
    private lateinit var searchResultsAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupRecyclerView()
        setupSearch()
        observeData()

        viewModel.getUpcomingEvent()
        viewModel.getCompletedEvent()
    }

    private fun setupAdapters() {
        upcomingEventAdapter = EventAdapter { eventItem -> navigateToDetail(eventItem) }
        completedEventAdapter = EventAdapter { eventItem -> navigateToDetail(eventItem) }
        searchResultsAdapter = EventAdapter { eventItem -> navigateToDetail(eventItem) }
    }

    private fun setupRecyclerView() {
        binding.rvUpcoming.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvUpcoming.adapter = upcomingEventAdapter

        binding.rvCompleted.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvCompleted.adapter = completedEventAdapter

        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvSearchResults.adapter = searchResultsAdapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchEvent(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.rvSearchResults.visibility = View.GONE
                    binding.rvUpcoming.visibility = View.VISIBLE
                    binding.rvCompleted.visibility = View.VISIBLE
                    binding.upcomingTitle.visibility = View.VISIBLE
                    binding.completedTitle.visibility = View.VISIBLE
                } else {
                    viewModel.searchEvent(newText)
                }
                return true
            }
        })
    }

    private fun observeData() {
        viewModel.upcomingEvent.observe(viewLifecycleOwner) { eventList ->
            eventList?.let {
                val upcomingItems = it.take(5).map { event -> EventItem.Regular(event) }
                upcomingEventAdapter.submitList(upcomingItems)
            }
        }

        viewModel.completedEvent.observe(viewLifecycleOwner) { eventList ->
            eventList?.let {
                val completedItems = it.take(5).map { event -> EventItem.Regular(event) }
                completedEventAdapter.submitList(completedItems)
            }
        }

        viewModel.searchEvent.observe(viewLifecycleOwner) { eventList ->
            if (eventList.isNullOrEmpty()) {
                binding.rvSearchResults.visibility = View.GONE
                binding.rvUpcoming.visibility = View.VISIBLE
                binding.rvCompleted.visibility = View.VISIBLE
                binding.upcomingTitle.visibility = View.VISIBLE
                binding.completedTitle.visibility = View.VISIBLE
            } else {
                val eventItems = eventList.map { EventItem.Regular(it) }
                searchResultsAdapter.submitList(eventItems)

                binding.rvSearchResults.visibility = View.VISIBLE
                binding.rvUpcoming.visibility = View.GONE
                binding.rvCompleted.visibility = View.GONE
                binding.upcomingTitle.visibility = View.GONE
                binding.completedTitle.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        //note reviewer: membuat 2 indikator loading
        viewModel.isLoadingUpcomingEvent.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarUpcoming.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isLoadingCompletedEvent.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarCompleted.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDetail(eventItem: EventItem) {
        when (eventItem) {
            is EventItem.Regular -> {
                val detailEvent = HomeFragmentDirections.actionNavigationHomeToDetailFragment(eventItem.event.id)
                findNavController().navigate(detailEvent)
            }
            is EventItem.Favorite -> {
                val detailEvent = HomeFragmentDirections.actionNavigationHomeToDetailFragment(eventItem.event.id)
                findNavController().navigate(detailEvent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}