package com.example.flashy.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flashy.*
import com.example.flashy.database.Deck
import com.example.flashy.databinding.FragmentDecksBinding
import com.example.flashy.recyclerview.DeckListAdapter
import com.example.flashy.recyclerview.GridItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DecksFragment : Fragment() {

    private var _binding: FragmentDecksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FlashcardsViewModel by activityViewModels {
        FlashcardsViewModel.FlashcardsViewModelFactory(
            (activity?.application as FlashcardsApplication).database.deckDao(),
            (activity?.application as FlashcardsApplication).database.cardDao()
        )
    }

    private lateinit var provider: MenuProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DeckListAdapter (requireContext(), {
            val action = DecksFragmentDirections
                .actionDecksFragmentToCardsFragment(deckId = it.id, deckName = it.name)
            this.findNavController().navigate(action)
        }, {
            showDeckOptionsDialog(it)
        }, {
            StudyManager.getInstance().prepareCards(this.viewLifecycleOwner, viewModel, it.id)
            val action = DecksFragmentDirections
                .actionDecksFragmentToStudyActivity(it.name)
            this.findNavController().navigate(action)
        })

        binding.recyclerView.adapter = adapter
        viewModel.allDecks.observe(this.viewLifecycleOwner) { decks ->
            decks.let {
                adapter.submitList(it)
            }
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this.context, 2)
        binding.recyclerView.addItemDecoration(GridItemDecoration(22))

        when (ModeManager.getInstance().getMode()) {
            ModeManager.Mode.FREE -> {
                binding.decksFab.setOnClickListener {
                    showDeckEditDialog()
                }
            }
            ModeManager.Mode.SRS -> {
                binding.decksFab.isVisible = false
            }
        }

        provider = object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
                menu.getItem(0).title = ModeManager.getInstance().getModeString()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                ModeManager.getInstance().switchMode()
                requireActivity().recreate()
                return true
            }
        }

        requireActivity().addMenuProvider(provider)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(provider)
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