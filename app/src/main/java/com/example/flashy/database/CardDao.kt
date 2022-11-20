package com.example.flashy.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM card WHERE card.deck = :deckId")
    fun getCardsFromDeck(deckId: Int): Flow<List<Card>>

    @Query("SELECT id FROM card WHERE deck = :deckId")
    fun getCardIdsFromDeck(deckId: Int): Flow<List<Int>>

    @Query("DELETE FROM card WHERE card.deck = :deckId")
    suspend fun deleteCardsFromDeck(deckId: Int)
}