package com.example.flashy.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashy.database.Card
import com.example.flashy.databinding.ItemCardBinding

class CardListAdapter(
    private val onCardClicked: (Card) -> Unit,
    private val onPreviewClicked: (Card) -> Unit)
    : ListAdapter<Card, CardListAdapter.CardViewHolder>(DiffCallback) {

    inner class CardViewHolder(private var binding: ItemCardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: Card) {
            binding.apply {
                cardFront.text = card.frontContent
                itemCardLayout.setOnClickListener { onCardClicked(card) }
                previewCard.setOnClickListener { onPreviewClicked(card) }
            }
        }
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<Card>() {
                override fun areItemsTheSame(
                    oldCard: Card, newCard: Card
                ): Boolean {
                    return oldCard === newCard
                }

                override fun areContentsTheSame(
                    oldCard: Card, newCard: Card
                ): Boolean {
                    return oldCard.frontContent == newCard.frontContent &&
                            oldCard.backContent == newCard.backContent &&
                            oldCard.deck == newCard.deck
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(
            ItemCardBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }
}