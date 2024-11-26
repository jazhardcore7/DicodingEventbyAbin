package com.event.abin.ui.favorite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.event.abin.data.local.entity.EventEntity
import com.event.abin.databinding.FragmentFavoriteBinding
import com.event.abin.ui.EventAdapter
import com.event.abin.ui.EventItem
import com.event.abin.ui.EventViewModel
import com.event.abin.ui.favorite.FavoriteFragmentDirections
import com.event.abin.viewmodel.ViewModelFactory
class FavoriteFragment : Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventViewModel by viewModels { ViewModelFactory.getInstance(requireContext()) }
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvFavorite.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorite.setHasFixedSize(true)

        eventAdapter = EventAdapter { eventItem ->
            when (eventItem) {
                is EventItem.Regular -> {
                    val detailEvent = FavoriteFragmentDirections.actionFavoriteFragmentToDetailFragment(eventItem.event.id)
                    findNavController().navigate(detailEvent)
                }
                is EventItem.Favorite -> {
                    val detailEvent = FavoriteFragmentDirections.actionFavoriteFragmentToDetailFragment(eventItem.event.id)
                    findNavController().navigate(detailEvent)
                }
            }
        }
        binding.rvFavorite.adapter = eventAdapter

        viewModel.getAllEvent()
        observeData()
    }

    private fun observeData() {
        binding.apply {
            viewModel.allEvent.observe(viewLifecycleOwner) { listItems ->
                Log.d("FavoriteFragment", "Observed Favorite Events: $listItems")
                setFavoriteEvent(listItems)
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
    }

    private fun setFavoriteEvent(listEvent: List<EventEntity>) {
        val items = listEvent.map { EventItem.Favorite(it) }
        eventAdapter.submitList(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
