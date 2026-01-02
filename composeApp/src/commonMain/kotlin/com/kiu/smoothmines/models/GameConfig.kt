package com.kiu.smoothmines.models

data class GameConfig(
    val rows: Int,
    val cols: Int,
    val minesCount: Int,
    val difficultyName: String
)

val Difficulties = listOf(
    GameConfig(9, 9, 10, "Новичок"),
    GameConfig(16, 16, 40, "Любитель"),
    GameConfig(16, 30, 99, "Эксперт")
)