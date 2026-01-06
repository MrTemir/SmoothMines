package com.kiu.smoothmines.models // Проверь свой пакет!

data class Cell(
    val id: Int,
    val x: Int,
    val y: Int,
    val isMine: Boolean = false,
    val adjacentMines: Int = 0,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false
) {
    // Эти свойства должны быть ЗДЕСЬ, внутри фигурных скобок
    val isSticky: Boolean get() = !isRevealed && !isFlagged
    val shouldShowContent: Boolean get() = isRevealed

}