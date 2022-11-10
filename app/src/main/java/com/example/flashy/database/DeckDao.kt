package com.example.flashy.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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
}