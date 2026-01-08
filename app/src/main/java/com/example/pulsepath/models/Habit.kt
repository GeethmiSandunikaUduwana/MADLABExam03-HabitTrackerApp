package com.example.pulsepath.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Habit(
    val id: String = "",
    var name: String,
    var category: String,
    var target: Int,
    var progress: Int = 0
) : Parcelable {
    val completionPercentage: Int
        get() = if (target > 0) (progress * 100) / target else 0

    val isCompleted: Boolean
        get() = progress >= target
}