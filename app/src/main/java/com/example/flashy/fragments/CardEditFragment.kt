package com.example.flashy.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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

    private val cameraRQ = 1
    private val galleryRQ = 2
    private val micRQ = 3
    private val audioRQ = 4

    private var frontPhotoPath: String? = null
    private var backPhotoPath: String? = null

    private var usingImageView = 0 // 0 -> Front, 1 -> Back

    private var startRecording: Boolean = true
    private var startPlaying: Boolean = true

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var frontAudioPath: String? = null
    private var backAudioPath: String? = null

    private var interval: Float = 0f
    private var dueDate: String = ""

    private var usingAudioSide = 0 // 0 -> Front, 1 -> Back

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCardEditBinding.inflate(inflater, container, false)
        return binding.root
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
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dueDate = format.format(Date())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                cameraRQ -> {
                    try {
                        if (usingImageView == 0 && frontPhotoPath != null) {
                            binding.cardFrontImage.setImageURI(Uri.fromFile(File(frontPhotoPath)))
                        } else if (usingImageView == 1 && backPhotoPath != null) {
                            binding.cardBackImage.setImageURI(Uri.fromFile(File(backPhotoPath)))
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
                        Log.e("debug", e.message.toString())
                    }
                }
                galleryRQ -> {
                    try {
                        if (usingImageView == 0) {
                            frontPhotoPath = getPath(data?.data!!, 0)
                            binding.cardFrontImage.setImageURI(data.data)
                        } else if (usingImageView == 1) {
                            backPhotoPath = getPath(data?.data!!, 0)
                            binding.cardBackImage.setImageURI(data.data)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
                        Log.e("debug", e.message.toString())
                    }
                }
                audioRQ -> {
                    try {
                        if (usingAudioSide == 0) {
                            frontAudioPath = getPath(data?.data!!, 1)
                            binding.playFrontAudio.visibility = View.VISIBLE
                        } else if (usingAudioSide == 1) {
                            backAudioPath = getPath(data?.data!!, 1)
                            binding.playBackAudio.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
                        Log.e("debug", e.message.toString())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
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
            if (card.frontImage != null) {
                frontPhotoPath = card.frontImage
                cardFrontImage.setImageURI(Uri.fromFile(File(card.frontImage)))
            }
            if (card.backImage != null) {
                backPhotoPath = card.backImage
                cardBackImage.setImageURI(Uri.fromFile(File(card.backImage)))
            }

            if (card.frontAudio != null) {
                frontAudioPath = card.frontAudio
                playFrontAudio.visibility = View.VISIBLE
            }
            if (card.backAudio != null) {
                backAudioPath = card.backAudio
                playBackAudio.visibility = View.VISIBLE
            }
            dueDate = card.dueDate
            interval = card.interval
            cardDueDate.text = resources.getString(R.string.due_date) + ": " + card.dueDate
            cardInterval.text = resources.getString(R.string.interval) + ": " + card.interval.toString()
        }
        bindImages()
    }
    
    private fun bindOnCreate() {
        binding.apply {
            root.removeView(deleteCardButton)
            saveCardButton.setOnClickListener {
                addNewCard()
                returnToCardsFragment()
            }
            saveCardButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            }
            cardDueDate.text = ""
            cardInterval.text = ""
        }
        bindImages()
    }

    private fun bindImages() {
        binding.apply {
            if (requireContext().packageManager
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                cardFrontImageCapture.setOnClickListener {
                    usingImageView = 0
                    openCamera()
                }
                cardBackImageCapture.setOnClickListener {
                    usingImageView = 1
                    openCamera()
                }
            }
            cardFrontImagePick.setOnClickListener {
                usingImageView = 0
                openGallery()
            }
            cardBackImagePick.setOnClickListener {
                usingImageView = 1
                openGallery()
            }
            cardFrontImageRemove.setOnClickListener { removeFrontImage() }
            cardBackImageRemove.setOnClickListener { removeBackImage() }

            if (requireContext().packageManager
                    .hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                cardFrontRecordAudio.setOnClickListener {
                    usingAudioSide = 0
                    onRecord()
                }
                cardBackRecordAudio.setOnClickListener {
                    usingAudioSide = 1
                    onRecord()
                }
            }
            cardFrontPickAudio.setOnClickListener {
                usingAudioSide = 0
                openDocuments()
            }
            cardBackPickAudio.setOnClickListener {
                usingAudioSide = 1
                openDocuments()
            }
            playFrontAudio.setOnClickListener {
                usingAudioSide = 0
                onPlay()
            }
            playBackAudio.setOnClickListener {
                usingAudioSide = 1
                onPlay()
            }
            cardFrontAudioRemove.setOnClickListener { removeFrontAudio() }
            cardBackAudioRemove.setOnClickListener { removeBackAudio() }
        }
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
        requireActivity().currentFocus?.let { view ->
            val imm = requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
        this.findNavController().navigateUp()
    }

    private fun addNewCard() {
        viewModel.addNewCard(
            binding.frontContent.text.toString(),
            binding.backContent.text.toString(),
            navigationArgs.deckId,
            interval,
            dueDate,
            frontPhotoPath,
            backPhotoPath,
            frontAudioPath,
            backAudioPath
        )
    }

    private fun updateCard() {
        viewModel.updateExistingCard(
            card.id,
            binding.frontContent.text.toString(),
            binding.backContent.text.toString(),
            card.deck,
            interval,
            dueDate,
            frontPhotoPath,
            backPhotoPath,
            frontAudioPath,
            backAudioPath
        )
    }

    private fun deleteCard() {
        viewModel.deleteCard(card)
        findNavController().navigateUp()
    }

    private fun getStoragePermission(): Boolean {
        val permission = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), cameraRQ)
        }

        return permission == PackageManager.PERMISSION_GRANTED
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("Flashy_${timeStamp}_", ".jpg", storageDir)
    }

    // type = 0 -> Image, type = 1 -> Audio
    private fun getPath(uri: Uri, type: Int): String? {
        var result: String? = null
        val proj = if (type == 0) {
            arrayOf(MediaStore.Images.Media.DATA)
        } else {
            arrayOf(MediaStore.Audio.Media.DATA)
        }
        val cursor = requireContext()
            .contentResolver.query(uri, proj, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(proj[0])
                result = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return result
    }

    private fun openCamera() {
        if (!getStoragePermission()) return
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (e: IOException) {
                    Toast.makeText(
                        requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
                    null
                }
                photoFile?.also {
                    if (usingImageView == 0) {
                        frontPhotoPath = photoFile.absolutePath
                    } else {
                        backPhotoPath = photoFile.absolutePath
                    }
                    Log.println(Log.INFO, "uris", photoFile.absolutePath)
                    val photoUri: Uri = FileProvider.getUriForFile(
                        requireContext(), "com.example.android.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, cameraRQ)
                }
            }
        }
    }

    private fun openGallery() {
        if (!getStoragePermission()) return
        val intent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, galleryRQ)
    }

    private fun openDocuments() {
        if (!getStoragePermission()) return
        val intent = Intent(
            Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, audioRQ)
    }

    private fun removeFrontImage() {
        frontPhotoPath = null
        binding.cardFrontImage.setImageDrawable(null)
    }

    private fun removeBackImage() {
        backPhotoPath = null
        binding.cardBackImage.setImageDrawable(null)
    }

    private fun getMicPermission(): Boolean {
        val permission = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.RECORD_AUDIO)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO), micRQ)
        }

        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun createAudioFile(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM)
        return storageDir?.absolutePath + timeStamp + ".3gp"
    }

    private fun onRecord() {
        startRecording = if (startRecording) {
            startRecording()
            false
        } else {
            stopRecording()
            true
        }
    }

    private fun startRecording() {
        if (!getMicPermission()) return
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            if (usingAudioSide == 0) {
                frontAudioPath = createAudioFile()
                setOutputFile(frontAudioPath)
                binding.cardFrontRecordAudio
                    .setImageResource(R.drawable.ic_baseline_stop_24)
            } else {
                backAudioPath = createAudioFile()
                setOutputFile(backAudioPath)
                binding.cardBackRecordAudio
                    .setImageResource(R.drawable.ic_baseline_stop_24)
            }
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("ioexception", e.toString())
                Toast.makeText(
                    requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
            }
            start()
        }
    }

    private fun stopRecording() {
        if (!getMicPermission()) return
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        if (usingAudioSide == 0) {
            binding.cardFrontRecordAudio
                .setImageResource(R.drawable.ic_baseline_mic_24)
            binding.playFrontAudio.visibility = View.VISIBLE
            binding.playFrontAudio
                .setImageResource(R.drawable.ic_baseline_play_circle_48)
        } else {
            binding.cardBackRecordAudio
                .setImageResource(R.drawable.ic_baseline_mic_24)
            binding.playBackAudio.visibility = View.VISIBLE
            binding.playBackAudio
                .setImageResource(R.drawable.ic_baseline_play_circle_48)
        }
    }

    private fun onPlay() {
        startPlaying = if (startPlaying) {
            startPlaying()
            false
        } else {
            stopPlaying()
            // pausePlaying()
            true
        }
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                if (usingAudioSide == 0) {
                    setDataSource(frontAudioPath)
                    binding.playFrontAudio
                        .setImageResource(R.drawable.ic_baseline_stop_circle_48)
                    setOnCompletionListener {
                        binding.playFrontAudio
                            .setImageResource(R.drawable.ic_baseline_play_circle_48)
                    }
                }
                else {
                    setDataSource(backAudioPath)
                    binding.playBackAudio
                        .setImageResource(R.drawable.ic_baseline_stop_circle_48)
                    setOnCompletionListener {
                        binding.playBackAudio
                            .setImageResource(R.drawable.ic_baseline_play_circle_48)
                    }
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

    private fun pausePlaying() {
        player?.pause()
        if (usingAudioSide == 0) {
            binding.playFrontAudio
                .setImageResource(R.drawable.ic_baseline_play_circle_48)
        } else {
            binding.playBackAudio
                .setImageResource(R.drawable.ic_baseline_play_circle_48)
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
        if (usingAudioSide == 0) {
            binding.playFrontAudio
                .setImageResource(R.drawable.ic_baseline_play_circle_48)
        } else {
            binding.playBackAudio
                .setImageResource(R.drawable.ic_baseline_play_circle_48)
        }
    }

    private fun removeFrontAudio() {
        binding.playFrontAudio.visibility = View.GONE
        frontAudioPath = null
    }

    private fun removeBackAudio() {
        binding.playBackAudio.visibility = View.GONE
        backAudioPath = null
    }
}