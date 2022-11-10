package com.example.flashy

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.flashy.database.Card
import com.example.flashy.database.CardDao
import com.example.flashy.database.Deck
import com.example.flashy.database.DeckDao
import kotlinx.coroutines.launch

class FlashcardsViewModel(
    private val deckDao: DeckDao,
    private val cardDao: CardDao
) : ViewModel() {
    val allDecks: LiveData<List<Deck>> = deckDao.getAll().asLiveData()

    fun retrieveDeck(id: Int): LiveData<Deck> {
        return deckDao.getDeck(id).asLiveData()
    }

    private fun insertDeck(deck: Deck) {
        viewModelScope.launch {
            deckDao.insert((deck))
        }
    }

    private fun updateDeck(deck: Deck) {
        viewModelScope.launch {
            deckDao.update(deck)
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            deckDao.delete(deck)
        }
    }

    private fun getNewDeckEntry(deckName: String): Deck {
        return Deck(name = deckName)
    }

    fun addNewDeck(deckName: String) {
        val deck = getNewDeckEntry(deckName)
        insertDeck(deck)
    }

    private fun getUpdatedDeckEntry(
        deckId: Int,
        deckName: String): Deck {
        return Deck(
            id = deckId,
            name = deckName,
        )
    }

    fun updateExistingDeck(
        deckId: Int,
        deckName: String) {
        val updatedDeck = getUpdatedDeckEntry(deckId, deckName)
        updateDeck(updatedDeck)
    }

    fun retrieveCard(id: Int): LiveData<Card> {
        return cardDao.getCard(id).asLiveData()
    }

    private fun insertCard(card: Card) {
        viewModelScope.launch {
            cardDao.insert(card)
        }
    }

    private fun updateCard(card: Card) {
        viewModelScope.launch {
            cardDao.update(card)
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            cardDao.delete(card)
        }
    }

    fun retrieveCardsFromDeck(deckId: Int): LiveData<List<Card>> {
        return cardDao.getCardsFromDeck(deckId).asLiveData()
    }

    fun retrieveCardsFromDeckInTime(deckId: Int): LiveData<List<Card>> {
        val mediator: MediatorLiveData<List<Card>> = MediatorLiveData()
        val cards = cardDao.getCardsFromDeck(deckId).asLiveData()
        mediator.addSource(cards) { cardList ->
            if (cardList != null) {
                mediator.removeSource(cards)
                mediator.value = cardList
            }
        }
        return mediator
    }

    private fun getNewCardEntry(
        front: String,
        back: String,
        deckId: Int
    ): Card {
        return Card(
            frontContent = front,
            backContent = back,
            deck = deckId
        )
    }

    fun addNewCard(
        front: String,
        back: String,
        deckId: Int) {
        val card = getNewCardEntry(front, back, deckId)
        insertCard(card)
    }

    private fun getUpdatedCardEntry(
        cardId: Int,
        front: String,
        back: String,
        deckId: Int): Card {
        return Card(
            id = cardId,
            frontContent = front,
            backContent = back,
            deck = deckId)
    }

    fun updateExistingCard(
        cardId: Int,
        front: String,
        back: String,
        deckId: Int
    ) {
        val updatedCard = getUpdatedCardEntry(cardId, front, back, deckId)
        updateCard(updatedCard)
    }

    fun retrieveCardIdsFromDeck(deckId: Int): LiveData<List<Int>> {
        return cardDao.getCardIdsFromDeck(deckId).asLiveData()
    }

    fun isEntryValid(front: String, back: String): Boolean {
        if (front.isBlank() || back.isBlank()) {
            return false
        }
        return true
    }

    fun deleteCardsFromDeck(deck: Deck) {
        viewModelScope.launch {
            cardDao.deleteCardsFromDeck(deck.id)
        }
    }

    class FlashcardsViewModelFactory(
        private val deckDao: DeckDao,
        private val cardDao: CardDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(
                    FlashcardsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FlashcardsViewModel(deckDao, cardDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}