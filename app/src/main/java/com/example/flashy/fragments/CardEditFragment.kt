package com.example.flashy.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.flashy.FlashcardsApplication
import com.example.flashy.FlashcardsViewModel
import com.example.flashy.R
import com.example.flashy.database.Card
import com.example.flashy.databinding.FragmentCardEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CardEditFragment : Fragment() {

    private var _binding: FragmentCardEditBinding? = null

    private val binding get() = _binding!!
    private val navigationArgs: CardEditFragmentArgs by navArgs()

    lateinit var card: Card

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
        _binding = FragmentCardEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun bindOnEdit(card: Card) {
        binding.apply {
            frontContent.setText(card.frontContent, TextView.BufferType.SPANNABLE)
            backContent.setText(card.backContent, TextView.BufferType.SPANNABLE)
            saveCardButton.setOnClickListener {
                updateCard()
                returnToCardsFragment()
            }
            deleteCardButton.setOnClickListener {
                showConfirmationDialog()
            }
        }
    }
    
    private fun bindOnCreate() {
        binding.apply {
            root.removeView(binding.deleteCardButton)
            saveCardButton.setOnClickListener {
                addNewCard()
                returnToCardsFragment()
            }
            saveCardButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = 0
            }
        }
    }

    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.frontContent.text.toString(),
            binding.backContent.text.toString()
        )
    }

    private fun addNewCard() {
        if (isEntryValid()) {
            viewModel.addNewCard(
                binding.frontContent.text.toString(),
                binding.backContent.text.toString(),
                navigationArgs.deckId
            )
        }
    }

    private fun updateCard() {
        if (isEntryValid()) {
            viewModel.updateExistingCard(
                card.id,
                binding.frontContent.text.toString(),
                binding.backContent.text.toString(),
                card.deck
            )
        }
    }

    private fun deleteCard() {
        viewModel.deleteCard(card)
        findNavController().navigateUp()
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirmation))
            .setMessage(getString(R.string.delete_card_confirmation_text))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteCard()
            }
            .show()
    }

    private fun returnToCardsFragment() {
        this.findNavController().navigateUp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cardId = navigationArgs.cardId
        if (cardId > 0) { // Llegamos de "Editar Carta".
            viewModel.retrieveCard(cardId)
                .observe(this.viewLifecycleOwner) { selectedCard ->
                    card = selectedCard
                    bindOnEdit(card)
                }
        }
        else { // LLegamos de "Crear Carta".
            bindOnCreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}