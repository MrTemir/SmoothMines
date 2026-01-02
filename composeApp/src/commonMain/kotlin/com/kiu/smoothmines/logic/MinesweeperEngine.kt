package com.kiu.smoothmines.logic

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
        cells: MutableList<Cell>,
        onCellRevealed: (Cell) -> Unit
    ) {
        val rows = 9 // Подставь переменную своего конфига
        val cols = 9
        val queue = ArrayDeque<Pair<Cell, Int>>()
        val visited = mutableSetOf<Int>()

        // Быстрый поиск индекса через координаты
        fun getIndex(x: Int, y: Int) = x * cols + y

        val startIndex = getIndex(startCell.x, startCell.y)
        queue.add(startCell to 0)
        visited.add(startIndex)

        var currentWaveDist = 0

        while (queue.isNotEmpty()) {
            val (current, dist) = queue.removeFirst()

            if (dist > currentWaveDist) {
                delay(25L) // Та самая плавная волна
                currentWaveDist = dist
            }

            val idx = getIndex(current.x, current.y)
            if (idx in cells.indices && !cells[idx].isMine) {
                val updated = cells[idx].copy(isRevealed = true)
                cells[idx] = updated
                onCellRevealed(updated)

                // Если вокруг нет мин, добавляем соседей
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