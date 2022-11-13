package com.example.flashy.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
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
    // private val navigationArgs: CardFrontFragmentArgs by navArgs()

    private val viewModel: FlashcardsViewModel by activityViewModels {
        FlashcardsViewModel.FlashcardsViewModelFactory(
            (activity?.application as FlashcardsApplication).database.deckDao(),
            (activity?.application as FlashcardsApplication).database.cardDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (StudyManager.getInstance().index() == 0) {
                requireActivity().finish()
            } else {
                StudyManager.getInstance().previousCard()
                findNavController().navigate(CardFrontFragmentDirections
                    .actionCardFrontFragmentToCardBackFragment())
            }
        }
        callback.isEnabled = true
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
        bindForStudy(StudyManager.getInstance().currentCard())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindForPreview(cardId: Int) {
        viewModel.retrieveCard(cardId)
            .observe(this.viewLifecycleOwner) { selectedCard ->
                binding.cardFrontText.text = selectedCard.frontContent
                binding.turnOverButton.setOnClickListener { goToCardBack(cardId) }
            }
    }

    private fun bindForStudy(card: Card?) {
        binding.apply {
            cardFrontText.text = card?.frontContent ?: ""
            binding.turnOverButton.setOnClickListener { goToCardBack() }
        }
    }

    private fun goToCardBack(currentCardId: Int = -1) {
        val action = CardFrontFragmentDirections
            .actionCardFrontFragmentToCardBackFragment()
        this.findNavController().navigate(action)
    }
}