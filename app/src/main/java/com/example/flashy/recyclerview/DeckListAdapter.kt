package com.example.flashy.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashy.FlashcardsApplication
import com.example.flashy.FlashcardsViewModel
import com.example.flashy.ModeManager
import com.example.flashy.database.Deck
import com.example.flashy.databinding.ItemDeckBinding
import java.text.SimpleDateFormat
import java.util.*

class DeckListAdapter(
    val context: Context,
    private val onDeckClicked: (Deck) -> Unit,
    private val onDeckLongClicked: (Deck) -> Unit,
    private val onStudyClicked: (Deck) -> Unit,
    private val setDueCardsCount: (Deck, TextView) -> Unit)
    : ListAdapter<Deck, DeckListAdapter.DeckViewHolder>(DiffCallback) {

    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    inner class DeckViewHolder(private var binding: ItemDeckBinding)
        : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ClickableViewAccessibility")
        fun bind(deck: Deck) {
            binding.apply {
                deckName.text = deck.name
                itemDeckLayout.setOnClickListener { onStudyClicked(deck) }

                when (ModeManager.getInstance().getMode()) {
                    ModeManager.Mode.FREE -> {
                        cardViewButton.setOnClickListener { onDeckClicked(deck) }
                        deckEditButton.setOnClickListener { onDeckLongClicked(deck) }
                        itemDeckLayout.setOnLongClickListener {
                            onDeckLongClicked(deck)
                            true
                        }
                        dueBadge.visibility = View.INVISIBLE
                    }
                    ModeManager.Mode.SRS -> {
                        setDueCardsCount(deck, dueBadge)
                        cardViewButton.setImageBitmap(null)
                        deckEditButton.setImageBitmap(null)
                        cardViewButton.isEnabled = false
                        deckEditButton.isEnabled = false
                    }
                }

            }
        }
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<Deck>() {
            override fun areItemsTheSame(
                oldDeck: Deck, newDeck: Deck
            ): Boolean {
                return oldDeck === newDeck
            }

            override fun areContentsTheSame(
                oldDeck: Deck, newDeck: Deck
            ): Boolean {
                return oldDeck.name == newDeck.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        return DeckViewHolder(
            ItemDeckBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }
}