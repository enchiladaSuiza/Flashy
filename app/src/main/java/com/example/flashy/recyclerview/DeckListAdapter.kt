package com.example.flashy.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashy.database.Deck
import com.example.flashy.databinding.ItemDeckBinding

class DeckListAdapter(
    val context: Context,
    private val onDeckClicked: (Deck) -> Unit,
    private val onDeckLongClicked: (Deck) -> Unit,
    private val onStudyClicked: (Deck) -> Unit)
    : ListAdapter<Deck, DeckListAdapter.DeckViewHolder>(DiffCallback) {

    inner class DeckViewHolder(private var binding: ItemDeckBinding)
        : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ClickableViewAccessibility")
        fun bind(deck: Deck) {
            binding.apply {
                deckName.text = deck.name
                itemDeckLayout.setOnClickListener { onDeckClicked(deck) }
                itemDeckLayout.setOnLongClickListener {
                    onDeckLongClicked(deck)
                    true
                }
                studyDeck.setOnClickListener { onStudyClicked(deck) }
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