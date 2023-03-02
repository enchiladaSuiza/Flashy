package com.example.flashy.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(card: Card)

    @Update
    suspend fun update(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Query("SELECT * FROM card WHERE id = :id")
    fun getCard(id: Int): Flow<Card>

    @Query("SELECT * FROM card ORDER BY front_content ASC")
    fun getAll(): Flow<List<Card>>

    @Query("SELECT deck FROM card WHERE card.id = :id")
    fun getDeckId(id: Int): Flow<Int>

    @Query("SELECT front_image FROM card WHERE card.id = :id")
    fun getFrontImage(id: Int): Flow<String>

    @Query("SELECT back_image FROM card WHERE card.id = :id")
    fun getBackImage(id: Int): Flow<String>

    @Query("SELECT front_audio FROM card WHERE card.id = :id")
    fun getFrontAudio(id: Int): Flow<String>

    @Query("SELECT back_audio FROM card WHERE card.id = :id")
    fun getBackAudio(id: Int): Flow<String>

    @Query("SELECT * FROM card WHERE card.deck = :deckId ORDER BY front_content ASC")
    fun getCardsFromDeck(deckId: Int): Flow<List<Card>>

    @Query("SELECT id FROM card WHERE deck = :deckId")
    fun getCardIdsFromDeck(deckId: Int): Flow<List<Int>>

    @Query("SELECT * FROM card WHERE card.deck = :deckId AND card.due_date <= :dueDate")
    fun getCardsFromDeckDue(deckId: Int, dueDate: String): Flow<List<Card>>

    @Query("DELETE FROM card WHERE card.deck = :deckId")
    suspend fun deleteCardsFromDeck(deckId: Int)
    @Query("SELECT interval FROM card WHERE card.id = :id")
    fun getInterval(id: Int): Float

    @Query("SELECT due_date FROM card WHERE card.id = :id")
    fun getDueDate(id: Int): String
}