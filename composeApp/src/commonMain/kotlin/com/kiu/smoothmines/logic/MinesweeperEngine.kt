package com.kiu.smoothmines.logic

import com.kiu.smoothmines.models.Cell
import kotlinx.coroutines.delay
import kotlin.math.abs

class MinesweeperEngine(val rows: Int, val cols: Int, val minesCount: Int) {

    fun generateBoard(): List<Cell> {
        return List(rows * cols) { index ->
            Cell(x = index / cols, y = index % cols)
        }
    }

    suspend fun revealEmptyCells(
        startCell: Cell,
        cells: MutableList<Cell>,
        onCellRevealed: (Cell) -> Unit
    ) {
        val queue = ArrayDeque<Pair<Cell, Int>>()
        val visited = mutableSetOf<Int>()

        // Используем параметры класса (rows, cols), а не жесткие числа!
        fun getIndex(x: Int, y: Int) = x * cols + y

        val startIndex = getIndex(startCell.x, startCell.y)
        queue.add(startCell to 0)
        visited.add(startIndex)

        var currentWaveDist = 0

        while (queue.isNotEmpty()) {
            val (current, dist) = queue.removeFirst()

            if (dist > currentWaveDist) {
                delay(20L) // Плавная волна открытия
                currentWaveDist = dist
            }

            val idx = getIndex(current.x, current.y)
            if (idx in cells.indices && !cells[idx].isMine) {
                val updated = cells[idx].copy(isRevealed = true)
                cells[idx] = updated
                onCellRevealed(updated)

                if (updated.adjacentMines == 0) {
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            val nx = current.x + dx
                            val ny = current.y + dy
                            val nIdx = getIndex(nx, ny)

                            if (nx in 0 until rows && ny in 0 until cols &&
                                nIdx !in visited && !cells[nIdx].isRevealed) {
                                visited.add(nIdx)
                                queue.add(cells[nIdx] to dist + 1)
                            }
                        }
                    }
                }
            }
        }
    }

    fun placeMines(cells: MutableList<Cell>, startCell: Cell, safeRadius: Int = 2) {
        cells.forEachIndexed { i, cell ->
            cells[i] = cell.copy(isMine = false, adjacentMines = 0)
        }

        val availableIndices = cells.indices.filter { i ->
            val c = cells[i]
            abs(c.x - startCell.x) > safeRadius || abs(c.y - startCell.y) > safeRadius
        }.shuffled()

        val minesToPlace = minOf(minesCount, availableIndices.size)
        for (i in 0 until minesToPlace) {
            val mineIdx = availableIndices[i]
            cells[mineIdx] = cells[mineIdx].copy(isMine = true)
        }

        recalculateNumbers(cells)
    }

    private fun recalculateNumbers(cells: MutableList<Cell>) {
        cells.forEachIndexed { i, cell ->
            if (!cell.isMine) {
                cells[i] = cell.copy(adjacentMines = countAdjacentMines(cell, cells))
            }
        }
    }

    // Тот самый метод, которого не хватало
    private fun countAdjacentMines(cell: Cell, cells: List<Cell>): Int {
        var count = 0
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nx = cell.x + dx
                val ny = cell.y + dy
                if (nx in 0 until rows && ny in 0 until cols) {
                    if (cells[nx * cols + ny].isMine) count++
                }
            }
        }
        return count
    }
}