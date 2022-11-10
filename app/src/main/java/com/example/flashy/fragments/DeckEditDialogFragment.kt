package com.example.flashy.fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.flashy.FlashcardsApplication
import com.example.flashy.FlashcardsViewModel
import com.example.flashy.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeckEditDialogFragment(val type: Int, val deckId: Int = -1) : DialogFragment() {
    private val viewModel: FlashcardsViewModel by activityViewModels {
        FlashcardsViewModel.FlashcardsViewModelFactory(
            (activity?.application as FlashcardsApplication).database.deckDao(),
            (activity?.application as FlashcardsApplication).database.cardDao()
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogWindow = MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.dialog_deck_edit)
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }

        when (type) {
            CREATE -> {
                dialogWindow.setTitle(getString(R.string.create_new_deck))
                dialogWindow.setPositiveButton(getString(R.string.create)) { _, _ ->
                    addDeck(requireDialog())
                }
            }
            EDIT -> {
                dialogWindow.setTitle(getString(R.string.rename_deck))
                dialogWindow.setPositiveButton(getString(R.string.rename)) { _, _ ->
                    editDeck(requireDialog())
                }
            }
        }
        return dialogWindow.create()
    }

    private fun addDeck(dialog: Dialog) {
        viewModel.addNewDeck(
            (dialog.findViewById(R.id.deck_name_edit) as EditText)
                .text.toString())
    }

    private fun editDeck(dialog: Dialog) {
        viewModel.updateExistingDeck(
            deckId,
            (dialog?.findViewById(R.id.deck_name_edit) as EditText)
                .text.toString())
    }

    companion object {
        const val TAG = "DeckEditDialog"
        const val CREATE = 0
        const val EDIT = 1
    }
}