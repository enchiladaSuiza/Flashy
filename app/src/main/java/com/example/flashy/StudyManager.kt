package com.example.flashy

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.flashy.database.Card
import com.example.flashy.database.Deck

class StudyManager private constructor() {
    private var index = 0
    private lateinit var cards: List<Card>
    private lateinit var againCards: MutableList<Card>

    private var viewModel: FlashcardsViewModel? = null
    private var owner: LifecycleOwner? = null
    private var deckId = 0
    private var cardId = 0

    private var observeDeck: Observer<List<Card>> = Observer { list ->
        cards = list.shuffled()
    }

    private var observeCard: Observer<Card> = Observer { card ->
        cards = listOf(card)
    }

    fun prepareCards(owner: LifecycleOwner, viewModel: FlashcardsViewModel, deckId: Int) {
        this.viewModel = viewModel
        this.owner = owner
        this.deckId = deckId
        viewModel.retrieveCardsFromDeck(deckId).observe(owner, observeDeck)
        againCards = mutableListOf()
        index = 0
    }

    fun preparePreview(owner: LifecycleOwner, viewModel: FlashcardsViewModel, cardId: Int) {
        this.viewModel = viewModel
        this.owner = owner
        this.cardId = cardId
        viewModel.retrieveCard(cardId).observe(owner, observeCard)
        againCards = mutableListOf()
        index = 0
    }

    fun discard() {
        owner?.let { viewModel?.retrieveCardsFromDeck(deckId)?.removeObservers(it) }
        owner?.let { viewModel?.retrieveCard(cardId)?.removeObservers(it) }
        deckId = 0
        cardId = 0
        cards = listOf()
        againCards = mutableListOf()
    }

    fun currentCard(): Card {
        return try {
            cards[index]
        } catch (e: Exception) {
            againCards[size() - index - 1]
        }
    }

    fun nextCard(): Card {
        index++
        return currentCard()
    }

    fun previousCard(): Card {
        index--
        return currentCard()
    }

    fun size(): Int {
        return cards.size + againCards.size
    }

    fun index(): Int {
        return index
    }

    fun remainingCards(): Int {
        return size() - index
    }

    fun sendCurrentCardToBack() {
        againCards.add(currentCard())
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