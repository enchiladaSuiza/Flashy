package com.example.flashy

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

class ModeManager private constructor() {
    companion object {
        @Volatile
        private var instance: ModeManager? = null

        fun getInstance(): ModeManager =
            instance ?: synchronized(this) {
                instance ?: ModeManager().also { instance = it }
            }
    }

    enum class Mode {
        FREE {
            override fun nextMode() = SRS
            override fun toString() = "Libre"
        },
        SRS {
            override fun nextMode() = FREE
        };

        abstract fun nextMode(): Mode
    }

    private var mode : Mode = Mode.FREE
    private var light : Boolean = true

    fun getMode(): Mode {
        return mode
    }

    fun getModeString(): String {
        return mode.toString()
    }

    fun getNextModeString(): String {
        return mode.nextMode().toString()
    }

    fun switchMode() {
        mode = mode.nextMode()
    }

    fun isSRS(): Boolean {
        return mode == Mode.SRS
    }

    fun isFree(): Boolean {
        return mode == Mode.FREE
    }

    fun setTheme(context: Context) {
        when (mode) {
            Mode.FREE -> context.setTheme(R.style.Theme_Flashy)
            Mode.SRS -> context.setTheme(R.style.Theme_FlashyYellow)
        }
    }
}