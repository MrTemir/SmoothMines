package com.kiu.smoothmines.models

data class SaveData(
    val slotIndex: Int,
    val config: GameConfig,
    val cells: List<Cell>,
    val date: String // Чтобы выводить "Сохранено: 14:20"
)