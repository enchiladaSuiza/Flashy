package com.example.flashy.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.flashy.*
import com.example.flashy.database.Card
import com.example.flashy.databinding.FragmentCardBackBinding
import java.io.File

class CardBackFragment : Fragment() {
    private var _binding: FragmentCardBackBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*(requireActivity() as AppCompatActivity).supportActionBar?.title =
            requireActivity().intent?.extras?.let { StudyActivityArgs.fromBundle(it).title }*/
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
            StudyManager.getInstance().size() - 1) {
            StudyManager.getInstance().discard()
            requireActivity().finish()
        }
        else {
            StudyManager.getInstance().nextCard()
            findNavController().navigate(CardBackFragmentDirections
                .actionCardBackFragmentToCardFrontFragment())
        }
    }

    private fun bindForStudy(card: Card) {
        binding.apply {
            remainingCardsBack.text = StudyManager.getInstance().remainingCards().toString()
            if (card.backImage != null) {
                backImageView.setImageURI(Uri.fromFile(File(card.backImage)))
            } else {
                backCardLayout.removeView(backImageView)
                cardBackText.updateLayoutParams<LinearLayout.LayoutParams> {
                    bottomMargin = 0
                }
            }
            if (card.backContent.isBlank()) {
                backCardLayout.removeView(cardBackText)
            } else {
                cardBackText.text = card.backContent
            }
            goodButton.setOnClickListener { goToNextFront(1) }
            againButton.setOnClickListener {
                StudyManager.getInstance().sendCurrentCardToBack()
                goToNextFront(0)
            }
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