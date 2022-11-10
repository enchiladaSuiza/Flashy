package com.example.flashy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flashy.*
import com.example.flashy.database.Deck
import com.example.flashy.databinding.FragmentDecksBinding
import com.example.flashy.recyclerview.DeckListAdapter
import com.example.flashy.recyclerview.GridItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Arrays

class DecksFragment : Fragment() {

    private var _binding: FragmentDecksBinding? = null

    private val binding get() = _binding!!

    private val viewModel: FlashcardsViewModel by activityViewModels {
        FlashcardsViewModel.FlashcardsViewModelFactory(
            (activity?.application as FlashcardsApplication).database.deckDao(),
            (activity?.application as FlashcardsApplication).database.cardDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDecksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DeckListAdapter (requireContext(), {
            val action = DecksFragmentDirections
                .actionDecksFragmentToCardsFragment(it.id)
            this.findNavController().navigate(action)
        }, {
            showDeckOptionsDialog(it)
        }, {
            val action = DecksFragmentDirections
                .actionDecksFragmentToCardFrontFragment(deckId = it.id)
            this.findNavController().navigate(action)
        })
        binding.recyclerView.adapter = adapter
        viewModel.allDecks.observe(this.viewLifecycleOwner) { decks ->
            decks.let {
                adapter.submitList(it)
            }
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this.context, 2)
        binding.recyclerView.addItemDecoration(GridItemDecoration(20))
        binding.decksFab.setOnClickListener {
            showDeckEditDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDeckEditDialog() {
        DeckEditDialogFragment(DeckEditDialogFragment.CREATE).show(
            childFragmentManager,
            DeckEditDialogFragment.TAG)
    }

    private fun showDeckOptionsDialog(deck: Deck) {
        MaterialAlertDialogBuilder(requireContext())
            .setItems(R.array.deck_options) { dialog, which ->
                when (which) {
                    0 -> {
                        DeckEditDialogFragment(DeckEditDialogFragment.EDIT, deck.id).show(
                            childFragmentManager,
                            DeckEditDialogFragment.TAG
                        )
                    }
                    1 -> showConfirmationDialog(deck)
                }
            }
            .show()
    }

    private fun showConfirmationDialog(deck: Deck) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirmation))
            .setMessage(getString(R.string.delete_deck_confirmation_text) +
                    '\n' + getString(R.string.all_cards_will_be_deleted))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteDeck(deck)
            }
            .show()
    }

    private fun deleteDeck(deck: Deck) {
        viewModel.deleteDeck(deck)
        viewModel.deleteCardsFromDeck(deck)
    }
}