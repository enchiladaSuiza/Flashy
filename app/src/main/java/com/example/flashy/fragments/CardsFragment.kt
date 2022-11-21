package com.example.flashy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashy.*
import com.example.flashy.databinding.FragmentCardsBinding
import com.example.flashy.recyclerview.CardListAdapter
import com.example.flashy.recyclerview.ListItemDecoration

class CardsFragment : Fragment() {

    private var _binding: FragmentCardsBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: CardsFragmentArgs by navArgs()

    private val viewModel: FlashcardsViewModel by activityViewModels {
        FlashcardsViewModel.FlashcardsViewModelFactory(
            (activity?.application as FlashcardsApplication).database.deckDao(),
            (activity?.application as FlashcardsApplication).database.cardDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CardListAdapter ({
            val action = CardsFragmentDirections
                .actionCardsFragmentToCardEditFragment(getString(
                    R.string.edit_card), it.id, it.deck)
            this.findNavController().navigate(action)
        }, {
            StudyManager.getInstance().preparePreview(this.viewLifecycleOwner, viewModel, it.id)
            viewModel.retrieveDeck(it.deck).observe(this.viewLifecycleOwner) { deck ->
                val action = CardsFragmentDirections
                    .actionCardsFragmentToStudyActivity(deck.name)
                this.findNavController().navigate(action)
            }
        })
        binding.recyclerView.adapter = adapter

        val deckId = navigationArgs.deckId
        viewModel.retrieveCardsFromDeck(deckId).observe(this.viewLifecycleOwner) { cards ->
            cards.let {
                adapter.submitList(it)
            }
        }
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.addItemDecoration(ListItemDecoration(12))
        binding.cardsFab.setOnClickListener {
            val action = CardsFragmentDirections
                .actionCardsFragmentToCardEditFragment(
                    title = getString(R.string.create_new_card),
                    deckId = deckId)
            this.findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}