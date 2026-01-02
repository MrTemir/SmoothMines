package com.kiu.smoothmines.models

import androidx.compose.runtime.Stable

@Stable
data class Cell(
    val x: Int,
    val y: Int,
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    var adjacentMines: Int = 0
)