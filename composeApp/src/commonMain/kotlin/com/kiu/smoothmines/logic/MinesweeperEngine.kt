package com.kiu.smoothmines.logic

import androidx.annotation.RequiresApi
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.kiu.smoothmines.models.Cell
import kotlinx.coroutines.delay

class MinesweeperEngine(val rows: Int, val cols: Int, val minesCount: Int) {

    // Создаем пустое поле
    fun generateBoard(): List<Cell> {
        val board = List(rows * cols) { index ->
            Cell(x = index / cols, y = index % cols)
        }
        return board
    }

    suspend fun revealEmptyCells(
        startCell: Cell,
        cells: MutableList<Cell>, // Убедись, что тут MutableList
        onCellRevealed: (Cell) -> Unit
    ) {
        val queue = ArrayDeque<Pair<Cell, Int>>() // Пара: Ячейка и её дистанция
        val visited = mutableSetOf<Pair<Int, Int>>()

        queue.add(startCell to 0)
        var currentDistance = 0

        while (queue.isNotEmpty()) {
            val (current, distance) = queue.removeFirst()
            val pos = current.x to current.y

            if (pos in visited) continue
            visited.add(pos)

            // Если мы перешли на новый уровень дистанции, делаем небольшую паузу
            if (distance > currentDistance) {
                delay(25L) // Время между "волнами"
                currentDistance = distance
            }

            val index = cells.indexOfFirst { it.x == current.x && it.y == current.y }
            if (index != -1 && !cells[index].isRevealed) {
                // Обновляем состояние
                cells[index] = cells[index].copy(isRevealed = true)
                onCellRevealed(cells[index])

                // Если рядом нет мин, добавляем соседей
                if (cells[index].adjacentMines == 0) {
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            if (dx == 0 && dy == 0) continue
                            val nx = current.x + dx
                            val ny = current.y + dy

                            val neighbor = cells.find { it.x == nx && it.y == ny }
                            if (neighbor != null && !neighbor.isRevealed && !neighbor.isMine) {
                                queue.add(neighbor to distance + 1)
                            }
                        }
                    }
                }
            }
        }
    }

    // Update placeMines function to prevent crashes
    fun placeMines(cells: SnapshotStateList<Cell>, safeCell: Cell) {
        val totalCells = rows * cols
        if (minesCount >= totalCells - 1) {
            // Don't allow more mines than cells - 1 (need at least one safe cell)
            return
        }

        var minesPlaced = 0
        val random = java.util.Random()

        // 1. Place mines
        while (minesPlaced < minesCount) {
            val randomIndex = random.nextInt(totalCells)
            val targetCell = cells[randomIndex]

            if (!targetCell.isMine &&
                targetCell.x != safeCell.x &&
                targetCell.y != safeCell.y) {
                cells[randomIndex] = targetCell.copy(isMine = true)
                minesPlaced++
            }
        }

        // 2. Calculate adjacent mines
        for (i in cells.indices) {
            val cell = cells[i]
            if (cell.isMine) continue

            var count = 0
            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue

                    val nx = cell.x + dx
                    val ny = cell.y + dy

                    if (nx in 0 until rows && ny in 0 until cols) {
                        val neighbor = cells.find { it.x == nx && it.y == ny }
                        if (neighbor?.isMine == true) {
                            count++
                        }
                    }
                }
            }
            cells[i] = cell.copy(adjacentMines = count)
        }

        // 3. Ensure there's at least one reachable empty cell
        if (cells.none { !it.isMine && it.adjacentMines == 0 }) {
            // If no empty cells, retry mine placement
            cells.replaceAll { it.copy(isMine = false, adjacentMines = 0) }
            placeMines(cells, safeCell)
        }
    }

    // Считаем цифры вокруг мин (оптимизированная версия)
    private fun calculateNumbers(board: List<Cell>) {
        // Создаем карту для быстрого доступа к клеткам по координатам
        val cellMap = board.associateBy { it.x to it.y }
        
        for (cell in board) {
            if (cell.isMine) continue
            
            var count = 0
            // Проверяем только существующие клетки вокруг
            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    
                    val nx = cell.x + dx
                    val ny = cell.y + dy
                    
                    // Проверяем границы
                    if (nx in 0 until rows && ny in 0 until cols) {
                        val neighbor = cellMap[nx to ny]
                        if (neighbor?.isMine == true) {
                            count++
                        }
                    }
                }
            }
            
            // Обновляем клетку
            val index = board.indexOf(cell)
            if (index != -1) {
                (board as? SnapshotStateList<Cell>)?.set(index, cell.copy(adjacentMines = count))
            }
        }
    }
}