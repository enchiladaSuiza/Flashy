package com.example.flashy

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.flashy.database.Card
import com.example.flashy.database.CardDao
import com.example.flashy.database.Deck
import com.example.flashy.database.DeckDao
import kotlinx.coroutines.launch
import java.util.Date

class FlashcardsViewModel(
    private val deckDao: DeckDao,
    private val cardDao: CardDao
) : ViewModel() {
    val allDecks: LiveData<List<Deck>> = deckDao.getAll().asLiveData()

    fun retrieveDeck(id: Int): LiveData<Deck> {
        return deckDao.getDeck(id).asLiveData()
    }

    fun retrieveDecksForDay(date: String): LiveData<List<Deck>> {
        return deckDao.getAllDecksForDay(date).asLiveData()
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

    fun retrieveCardsFromDeckDue(deckId: Int, dueDate: String): LiveData<List<Card>> {
        return cardDao.getCardsFromDeckDue(deckId, dueDate).asLiveData()
    }

    private fun getNewCardEntry(
        front: String,
        back: String,
        deckId: Int,
        ivl: Float,
        due: String,
        fImage: String? = null,
        bImage: String? = null,
        fAudio: String? = null,
        bAudio: String? = null
    ): Card {
        return Card(
            frontContent = front,
            backContent = back,
            deck = deckId,
            frontImage = fImage,
            backImage = bImage,
            frontAudio = fAudio,
            backAudio = bAudio,
            interval = ivl,
            dueDate = due
        )
    }

    fun addNewCard(
        front: String,
        back: String,
        deckId: Int,
        ivl: Float,
        due: String,
        fImage: String? = null,
        bImage: String? = null,
        fAudio: String? = null,
        bAudio: String? = null,
        ) {
        val card = getNewCardEntry(
            front, back, deckId, ivl, due, fImage, bImage, fAudio, bAudio)
        insertCard(card)
    }

    private fun getUpdatedCardEntry(
        cardId: Int,
        front: String,
        back: String,
        deckId: Int,
        ivl: Float,
        due: String,
        fImage: String? = null,
        bImage: String? = null,
        fAudio: String? = null,
        bAudio: String? = null): Card {
        return Card(
            id = cardId,
            frontContent = front,
            backContent = back,
            deck = deckId,
            frontImage = fImage,
            backImage = bImage,
            frontAudio = fAudio,
            backAudio = bAudio,
            interval = ivl,
            dueDate = due)
    }

    fun updateExistingCard(
        cardId: Int,
        front: String,
        back: String,
        deckId: Int,
        ivl: Float,
        due: String,
        fImage: String? = null,
        bImage: String? = null,
        fAudio: String? = null,
        bAudio: String? = null
    ) {
        val updatedCard = getUpdatedCardEntry(
            cardId, front, back, deckId, ivl, due, fImage, bImage, fAudio, bAudio)
        updateCard(updatedCard)
    }

    fun retrieveCardIdsFromDeck(deckId: Int): LiveData<List<Int>> {
        return cardDao.getCardIdsFromDeck(deckId).asLiveData()
    }

    fun deleteCardsFromDeck(deck: Deck) {
        viewModelScope.launch {
            cardDao.deleteCardsFromDeck(deck.id)
        }
    }

    fun retrieveDecksCount(): LiveData<Int> {
        return deckDao.getDecksCount().asLiveData()
    }

    fun retrieveDueDecksCount(date: String): LiveData<Int> {
        return deckDao.getDueDecksCount(date).asLiveData()
    }

    fun retrieveDueCardsCountFromDeck(deckId: Int, date: String): LiveData<Int> {
        return deckDao.getDueCardsCountFromDeck(deckId, date).asLiveData()
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