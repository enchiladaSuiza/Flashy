package com.example.flashy.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.view.MenuItemCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flashy.*
import com.example.flashy.database.Deck
import com.example.flashy.databinding.FragmentDecksBinding
import com.example.flashy.recyclerview.DeckListAdapter
import com.example.flashy.recyclerview.GridItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
            if (ModeManager.getInstance().getMode() == ModeManager.Mode.FREE) {
                StudyManager.getInstance().prepareCards(this.viewLifecycleOwner, viewModel, it.id)
            }
            else {
                StudyManager.getInstance().prepareSRSCards(this.viewLifecycleOwner, viewModel, it.id)
            }
            val action = DecksFragmentDirections
                .actionDecksFragmentToStudyActivity(it.name)
            this.findNavController().navigate(action)
        }, {
            deck, textView ->
            run {
                viewModel.retrieveDueCardsCountFromDeck(deck.id, format.format(Date()))
                    .observe(this.viewLifecycleOwner) {
                    textView.text = it.toString()
                }
            }
        })

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(this.context, 2)
        binding.recyclerView.addItemDecoration(GridItemDecoration(22))

        if (ModeManager.getInstance().getMode() == ModeManager.Mode.FREE) {
            viewModel.allDecks.observe(viewLifecycleOwner) { decks ->
                decks.let {
                    adapter.submitList(it)
                }
            }
            binding.decksFab.setOnClickListener {
                showDeckEditDialog()
            }
        }
        else {
            val today = format.format(Date())
            viewModel.retrieveDecksForDay(today).observe(viewLifecycleOwner) { decks ->
                decks.let {
                    adapter.submitList(it)
                }
            }
            binding.decksFab.isVisible = false
        }

        provider = object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
                val switchItem = menu.findItem(R.id.switch_modes)
                setupBadge(switchItem)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                switchMode()
                return true
            }
        }

        requireActivity().addMenuProvider(provider)
        // requireActivity().actionBar?.setIcon(R.mipmap.ic_launcher_foreground)
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
            .setMessage(
                getString(R.string.delete_deck_confirmation_text) +
                        '\n' + getString(R.string.all_cards_will_be_deleted)
            )
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

    private fun switchMode() {
        ModeManager.getInstance().switchMode()
        requireActivity().recreate()
    }

    private fun setupBadge(item: MenuItem) {
        item.title = ModeManager.getInstance().getNextModeString()
        val button = item.actionView?.findViewById<Button>(R.id.mode_title)
        button?.text = item.title
        button?.setOnClickListener { switchMode() }

        val badgeView = item.actionView?.findViewById<TextView>(R.id.mode_badge)

        viewModel.retrieveDueDecksCount(format.format(Date())).observe(viewLifecycleOwner) {
            if (it == 0) {
                if (ModeManager.getInstance().getMode() == ModeManager.Mode.SRS) {
                    switchMode()
                }
                badgeView?.visibility = View.INVISIBLE
                button?.visibility = View.INVISIBLE
            }
            else {
                if (ModeManager.getInstance().getMode() == ModeManager.Mode.SRS) {
                    badgeView?.visibility = View.INVISIBLE
                }
                else {
                    badgeView?.visibility = View.VISIBLE
                    badgeView?.text = it.toString()
                }
            }
        }
    }
}