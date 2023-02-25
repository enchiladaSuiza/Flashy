package com.example.flashy.fragments

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.flashy.*
import com.example.flashy.database.Card
import com.example.flashy.databinding.FragmentCardFrontBinding
import java.io.File
import java.io.IOException

class CardFrontFragment : Fragment() {
    private var _binding: FragmentCardFrontBinding? = null
    private val binding get() = _binding!!

    private var playing: Boolean = true
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (StudyManager.getInstance().index() == 0) {
                StudyManager.getInstance().discard()
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
        try {
            bindForStudy(StudyManager.getInstance().currentCard())
        } catch (e: Exception) {
            Toast.makeText(
                this.context,
                "No hay cartas en este mazo.",
                Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindForStudy(card: Card) {
        binding.apply {
            val bundle = requireActivity().intent?.extras
            if (bundle != null) {
                val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
                actionBar?.title = StudyActivityArgs.fromBundle(bundle).title + " (" +
                        StudyManager.getInstance().remainingCards().toString() + ")"
            }
            if (card.frontContent.isBlank()) {
                frontCardLayout.removeView(cardFrontText)
            } else {
                cardFrontText.text = card.frontContent
            }
            if (card.frontImage != null) {
                frontImageView.setImageURI(Uri.fromFile(File(card.frontImage)))
            } else {
                frontCardLayout.removeView(frontImageView)
            }
            if (card.frontAudio != null) {
                frontAudioPlay.setOnClickListener { onPlay(card.frontAudio) }
            } else {
                frontCardLayout.removeView(frontAudioPlay)
            }
            binding.turnOverButton.setOnClickListener { goToCardBack() }
        }
    }

    private fun goToCardBack(currentCardId: Int = -1) {
        val action = CardFrontFragmentDirections
            .actionCardFrontFragmentToCardBackFragment()
        this.findNavController().navigate(action)
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
                binding.frontAudioPlay
                    .setImageResource(R.drawable.ic_baseline_stop_circle_48)
                setOnCompletionListener {
                    binding.frontAudioPlay
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
        binding.frontAudioPlay
            .setImageResource(R.drawable.ic_baseline_play_circle_48)
        binding.frontAudioPlay
            .setImageResource(R.drawable.ic_baseline_play_circle_48)
    }
}