package com.example.flashy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.flashy.FlashcardsApplication
import com.example.flashy.FlashcardsViewModel
import com.example.flashy.R
import com.example.flashy.StudyManager
import com.example.flashy.database.Card
import com.example.flashy.databinding.FragmentCardBackBinding

class CardBackFragment : Fragment() {
    private var _binding: FragmentCardBackBinding? = null
    private val binding get() = _binding!!
    /*private val navigationArgs: CardBackFragmentArgs by navArgs()*/

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
        _binding = FragmentCardBackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindForStudy(StudyManager.getInstance().currentCard())
    }

    private fun goToNextFront(currentRating: Int) {
        if (StudyManager.getInstance().index() ==
            StudyManager.getInstance().size()?.minus(1)) {
            requireActivity().finish()
        }
        else {
            StudyManager.getInstance().nextCard()
            findNavController().navigate(CardBackFragmentDirections
                .actionCardBackFragmentToCardFrontFragment())
        }
    }

    private fun bindForPreview(cardId: Int) {
        viewModel.retrieveCard(cardId)
            .observe(this.viewLifecycleOwner) { selectedCard ->
                binding.cardBackText.text = selectedCard.backContent
                binding.goodButton.setOnClickListener { goToCardsFragment() }
                binding.againButton.setOnClickListener { goToCardsFragment() }
            }
    }

    private fun bindForStudy(card: Card?) {
        binding.apply {
            cardBackText.text = card?.backContent ?: ""
            goodButton.setOnClickListener { goToNextFront(1) }
            againButton.setOnClickListener { goToNextFront(0) }
        }
    }

    private fun goToCardsFragment() {
        this.findNavController().navigateUp()
        this.findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}