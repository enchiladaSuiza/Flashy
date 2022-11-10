package com.example.flashy

import android.app.Application
import com.example.flashy.database.CardsDatabase

class FlashcardsApplication : Application() {
    val database: CardsDatabase by lazy {
        CardsDatabase.getDatabase(this)
    }
}
