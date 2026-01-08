package com.example.pulsepath.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MoodEntry(
    val id: String,
    val emoji: String,
    val mood: String,
    val note: String,
    val timestamp: Long
) : Parcelable