package com.example.flashy

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.flashy.database.Card
import com.example.flashy.database.Deck

class StudyManager private constructor() {

    private var index = 0
    private var cards: List<Card>? = null

    fun prepareCards(viewModel: FlashcardsViewModel, deckId: Int) {
        viewModel.retrieveCardsFromDeck(deckId).observeForever { list ->
            cards = list
        }
        index = 0
    }

    fun preparePreview(viewModel: FlashcardsViewModel, cardId: Int) {
        viewModel.retrieveCard(cardId).observeForever { card ->
            cards = listOf(card)
        }
        index = 0
    }

    fun currentCard(): Card? {
        return cards?.get(index)
    }

    fun nextCard(): Card? {
        index++
        return currentCard()
    }

    fun previousCard(): Card? {
        index--
        return currentCard()
    }

    fun size(): Int? {
        return cards?.size
    }

    fun index(): Int {
        return index
    }

    companion object {
        @Volatile
        private var instance: StudyManager? = null

        fun getInstance(): StudyManager =
            instance ?: synchronized(this) {
                instance ?: StudyManager().also { instance = it }
            }
    }
}