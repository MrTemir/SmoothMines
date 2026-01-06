package com.kiu.smoothmines.logic

import com.kiu.smoothmines.models.Cell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MinesweeperEngine(val rows: Int, val cols: Int, val minesCount: Int) {

    fun generateBoard(): List<Cell> {
        return List(rows * cols) { index ->
            Cell(
                id = index,
                x = index % cols,
                y = index / cols,
                isMine = false,
                isRevealed = false,
                isFlagged = false,
            )
        }
    }

    // РАССТАНОВКА МИН И РАСЧЕТ ЦИФР
    suspend fun placeMines(cells: MutableList<Cell>, startCell: Cell, safeRadius: Int = 2) = withContext(Dispatchers.Default) {
        val totalCells = rows * cols

        // Проверка на безумные настройки (оставляем место для первого клика)
        val effectiveMinesCount = minesCount.coerceAtMost(totalCells - (safeRadius * 2 + 1).let { it * it })

        val safeIndices = mutableSetOf<Int>()
        // Определяем "безопасную зону"
        for (dx in -safeRadius..safeRadius) {
            for (dy in -safeRadius..safeRadius) {
                val nx = startCell.x + dx
                val ny = startCell.y + dy
                if (nx in 0 until cols && ny in 0 until rows) {
                    safeIndices.add(ny * cols + nx)
                }
            }
        }

        var minesPlaced = 0
        while (minesPlaced < effectiveMinesCount) {
            val randomIndex = Random.nextInt(totalCells)
            if (!safeIndices.contains(randomIndex) && !cells[randomIndex].isMine) {
                cells[randomIndex] = cells[randomIndex].copy(isMine = true)
                minesPlaced++
            }
        }

        // РАССЧИТЫВАЕМ ЦИФРЫ
        for (i in cells.indices) {
            if (!cells[i].isMine) {
                val count = countNeighborMines(i, cells)
                cells[i] = cells[i].copy(adjacentMines = count)
            }
        }
    }

    // Оптимизированный подсчет соседей без создания лишних списков
    private fun countNeighborMines(index: Int, cells: List<Cell>): Int {
        var count = 0
        val x = index % cols
        val y = index / cols
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nx = x + dx
                val ny = y + dy
                if (nx in 0 until cols && ny in 0 until rows) {
                    if (cells[ny * cols + nx].isMine) count++
                }
            }
        }
        return count
    }

    // ИТЕРАТИВНОЕ РАСКРЫТИЕ (BFS)
    suspend fun revealEmptyCells(startCell: Cell, cells: MutableList<Cell>, onReveal: () -> Unit) = withContext(Dispatchers.Default) {
        val queue = ArrayDeque<Int>()
        queue.add(startCell.id)
        val visited = mutableSetOf<Int>()

        while (queue.isNotEmpty()) {
            val currentIndex = queue.removeFirst()
            // visited.add возвращает false, если элемент уже там был
            if (!visited.add(currentIndex)) continue

            val currentCell = cells[currentIndex]
            // Используем твои новые свойства isSticky для лаконичности,
            // но тут важна проверка на флаг и уже открытость
            if (currentCell.isFlagged || currentCell.isRevealed) continue

            // Раскрываем ячейку
            cells[currentIndex] = currentCell.copy(isRevealed = true)

            // Если нашли пустую ячейку, идем к соседям
            if (cells[currentIndex].adjacentMines == 0 && !cells[currentIndex].isMine) {
                getNeighborIndices(currentIndex).forEach { neighborIdx ->
                    if (!cells[neighborIdx].isRevealed && !cells[neighborIdx].isFlagged) {
                        queue.add(neighborIdx)
                    }
                }
            }
        }
    }

    private fun getNeighborIndices(index: Int): List<Int> {
        val neighbors = mutableListOf<Int>()
        val x = index % cols
        val y = index / cols
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nx = x + dx
                val ny = y + dy
                if (nx in 0 until cols && ny in 0 until rows) {
                    neighbors.add(ny * cols + nx)
                }
            }
        }
        return neighbors
    }
    fun getAdjacentIndexes(index: Int): List<Int> {
        val r = index / cols
        val c = index % cols
        val adjacent = mutableListOf<Int>()

        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols) {
                    adjacent.add(nr * cols + nc)
                }
            }
        }
        return adjacent
    }
}