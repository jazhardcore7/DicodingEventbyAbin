package com.event.abin.ui.upcoming

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.event.abin.databinding.FragmentUpcomingBinding
import com.event.abin.ui.EventAdapter
import com.event.abin.ui.EventItem
import com.event.abin.ui.EventViewModel
import com.event.abin.viewmodel.ViewModelFactory
class UpcomingFragment : Fragment() {
    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventViewModel by viewModels { ViewModelFactory.getInstance(requireContext()) }
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUpcomingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvUpcoming.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcoming.setHasFixedSize(true)

        eventAdapter = EventAdapter { eventItem ->
            when (eventItem) {
                is EventItem.Regular -> {
                    val detailEvent = UpcomingFragmentDirections.actionNavigationUpcomingToDetailFragment(eventItem.event.id)
                    findNavController().navigate(detailEvent)
                }
                is EventItem.Favorite -> {
                    val detailEvent = UpcomingFragmentDirections.actionNavigationUpcomingToDetailFragment(eventItem.event.id)
                    findNavController().navigate(detailEvent)
                }
            }
        }
        binding.rvUpcoming.adapter = eventAdapter

        viewModel.getUpcomingEvent()
        observeData()
    }

    private fun observeData() {
        viewModel.upcomingEvent.observe(viewLifecycleOwner) { eventList ->
            eventList?.let {
                eventAdapter.submitList(it.map { event -> EventItem.Regular(event) })
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
