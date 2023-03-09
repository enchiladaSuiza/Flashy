package com.example.flashy.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DeckDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(deck: Deck)

    @Update
    suspend fun update(deck: Deck)

    @Delete
    suspend fun delete(deck: Deck)

    @Query("SELECT * FROM deck WHERE id = :id")
    fun getDeck(id: Int): Flow<Deck>

    @Query("SELECT * FROM deck WHERE name = :name")
    fun getDeckByName(name: String): Flow<Deck>

    @Query("SELECT id FROM deck WHERE name = :name")
    fun getIdByName(name: String): Flow<Int>

    @Query("SELECT * FROM deck ORDER BY name ASC")
    fun getAll(): Flow<List<Deck>>

    @Query("SELECT * FROM deck JOIN card ON card.deck = deck.id")
    fun getDecksWithCards(): Map<Deck, List<Card>>

    @Query("SELECT DISTINCT deck.* FROM deck, card ON deck.id = card.deck WHERE card.due_date <= :date")
    fun getAllDecksForDay(date: String): Flow<List<Deck>>

    @Query("SELECT COUNT(*) FROM deck")
    fun getDecksCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT deck.id) FROM deck, card ON deck.id = card.deck WHERE card.due_date <= :date")
    fun getDueDecksCount(date: String): Flow<Int>

    @Query("SELECT COUNT(card.id) FROM deck, card ON deck.id = card.deck WHERE deck.id = :deckId AND card.due_date <= :date")
    fun getDueCardsCountFromDeck(deckId: Int, date: String): Flow<Int>
}