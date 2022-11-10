package com.example.flashy

import androidx.lifecycle.LiveData
import com.example.flashy.database.Card

class StudyManager(
    private val viewModel: FlashcardsViewModel,
    private val deckId: Int) {

    private var index = -1
    private val cards: LiveData<List<Card>> = viewModel.retrieveCardsFromDeck(deckId)

    fun currentCard(): Card {
        return cards.value?.get(index)!!
    }

    fun nextCard(): Card {
        index++
        return currentCard()
    }

    fun previousCard(): Card {
        index--
        return currentCard()
    }
}