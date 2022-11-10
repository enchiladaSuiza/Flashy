package com.example.flashy.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.flashy.FlashcardsApplication
import com.example.flashy.FlashcardsViewModel
import com.example.flashy.R
import com.example.flashy.StudyManager
import com.example.flashy.database.Card
import com.example.flashy.databinding.FragmentCardFrontBinding
import com.example.flashy.databinding.FragmentCardsBinding

class CardFrontFragment : Fragment() {
    private var _binding: FragmentCardFrontBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: CardFrontFragmentArgs by navArgs()

    private val viewModel: FlashcardsViewModel by activityViewModels {
        FlashcardsViewModel.FlashcardsViewModelFactory(
            (activity?.application as FlashcardsApplication).database.deckDao(),
            (activity?.application as FlashcardsApplication).database.cardDao()
        )
    }

    private fun bind(cardId: Int) {
        viewModel.retrieveCard(cardId)
            .observe(this.viewLifecycleOwner) { selectedCard ->
                binding.cardFrontText.text = selectedCard.frontContent
                binding.turnOverButton.setOnClickListener { goToCardBack(cardId) }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCardFrontBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var cardId = navigationArgs.cardId
        val deckId = navigationArgs.deckId

        if (deckId != -1) { // Estudio
            viewModel.retrieveCardsFromDeck(deckId).value
            val index = navigationArgs.index
            viewModel.retrieveCardsFromDeck(deckId).observe(this.viewLifecycleOwner) { cards ->
                cards.let {
                    if (index > cards.size - 1) {
                        this.findNavController().popBackStack(R.id.decksFragment, false)
                    } else {
                        cardId = cards[index].id
                        bind(cardId)
                    }
                }
            }
        }
        else {
            bind(cardId)
        }
    }

    private fun goToCardBack(currentCardId: Int) {
        val action = CardFrontFragmentDirections
            .actionCardFrontFragmentToCardBackFragment(
                cardId = currentCardId,
                deckId = navigationArgs.deckId,
                index = navigationArgs.index)
        this.findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}