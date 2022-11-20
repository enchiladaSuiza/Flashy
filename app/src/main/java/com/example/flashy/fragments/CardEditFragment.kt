package com.example.flashy.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
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

    private var frontPhotoPath: String? = null
    private var backPhotoPath: String? = null

    private var usingImageView = 0 // 0 -> Front, 1 -> Back
    /*private var cameraResultLauncher: ActivityResultLauncher<Intent>? = null*/

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
        }
    }

    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.frontContent.text.toString(),
            binding.backContent.text.toString()
        )
    }

    private fun addNewCard() {
        /*if (isEntryValid()) {*/
            viewModel.addNewCard(
                binding.frontContent.text.toString(),
                binding.backContent.text.toString(),
                navigationArgs.deckId,
                frontPhotoPath,
                backPhotoPath
            )
        /*}*/
    }

    private fun updateCard() {
        /*if (isEntryValid()) {*/
            viewModel.updateExistingCard(
                card.id,
                binding.frontContent.text.toString(),
                binding.backContent.text.toString(),
                card.deck,
                frontPhotoPath,
                backPhotoPath
            )
        /*}*/
    }

    private fun deleteCard() {
        viewModel.deleteCard(card)
        findNavController().navigateUp()
    }

    private fun getPermission(): Boolean {
        val permission = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), cameraRQ)
        }

        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera() {
        if (!getPermission()) return
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("Flashy_${timeStamp}_", ".jpg", storageDir)
    }

    private fun openGallery() {
        if (!getPermission()) return
        val intent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, galleryRQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == cameraRQ) {
                try {
                    if (usingImageView == 0 && frontPhotoPath != null) {
                        binding.cardFrontImage.setImageURI(Uri.fromFile(File(frontPhotoPath)))
                    }
                    else if (usingImageView == 1 && backPhotoPath != null) {
                        binding.cardBackImage.setImageURI(Uri.fromFile(File(backPhotoPath)))
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
                    Log.e("debug", e.message.toString())
                }
            }
            else if (requestCode == galleryRQ) {
                try {
                    if (usingImageView == 0) {
                        frontPhotoPath = getPath(data?.data!!)
                        binding.cardFrontImage.setImageURI(data.data)
                    }
                    else if (usingImageView == 1) {
                        backPhotoPath = getPath(data?.data!!)
                        binding.cardBackImage.setImageURI(data.data)
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
                    Log.e("debug", e.message.toString())
                }
            }
        }
    }

    private fun getPath(uri: Uri): String? {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
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

    private fun removeFrontImage() {
        frontPhotoPath = null
        binding.cardFrontImage.setImageDrawable(null)
    }

    private fun removeBackImage() {
        backPhotoPath = null
        binding.cardBackImage.setImageDrawable(null)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}