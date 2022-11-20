package com.example.flashy.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.flashy.database.Deck

@Entity
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "front_content")
    val frontContent: String,

    @ColumnInfo(name = "front_image")
    val frontImage: String?,

    @ColumnInfo(name = "back_content")
    val backContent: String,

    @ColumnInfo(name = "back_image")
    val backImage: String?,

    @ColumnInfo(name = "deck", index = true)
    val deck: Int
)
