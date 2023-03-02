package com.example.flashy.fragments

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
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
import java.io.IOException

class CardBackFragment : Fragment() {
    private var _binding: FragmentCardBackBinding? = null
    private val binding get() = _binding!!

    private var playing: Boolean = true
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    private fun goToNextFront(currentRating: Int) {
        if (ModeManager.getInstance().getMode() == ModeManager.Mode.SRS) {
            StudyManager.getInstance().rateCard(currentRating)
        }
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
            val bundle = requireActivity().intent?.extras
            if (bundle != null) {
                val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
                actionBar?.title = StudyActivityArgs.fromBundle(bundle).title + " (" +
                        StudyManager.getInstance().remainingCards().toString() + ")"
            }
            if (card.backContent.isBlank()) {
                backCardLayout.removeView(cardBackText)
            } else {
                cardBackText.text = card.backContent
            }
            if (card.backImage != null) {
                backImageView.setImageURI(Uri.fromFile(File(card.backImage)))
            } else {
                backCardLayout.removeView(backImageView)
            }
            if (card.backAudio != null) {
                backAudioPlay.setOnClickListener { onPlay(card.backAudio) }
            } else {
                backCardLayout.removeView(backAudioPlay)
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

    private fun onPlay(file: String) {
        playing = if (playing) {
            startPlaying(file)
            false
        } else {
            stopPlaying()
            true
        }
    }

    private fun startPlaying(file: String) {
        player = MediaPlayer().apply {
            try {
                setDataSource(file)
                binding.backAudioPlay
                    .setImageResource(R.drawable.ic_baseline_stop_circle_48)
                setOnCompletionListener {
                    binding.backAudioPlay
                        .setImageResource(R.drawable.ic_baseline_play_circle_48)
                }
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("ioexception", e.toString())
                Toast.makeText(
                    requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
        binding.backAudioPlay
            .setImageResource(R.drawable.ic_baseline_play_circle_48)
        binding.backAudioPlay
            .setImageResource(R.drawable.ic_baseline_play_circle_48)
    }
}