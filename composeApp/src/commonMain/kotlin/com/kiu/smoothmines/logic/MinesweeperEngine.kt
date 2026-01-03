package com.kiu.smoothmines.logic

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.kiu.smoothmines.models.Cell
import kotlinx.coroutines.delay
import kotlin.math.abs

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
        // Рекомендую ограничить количество мин, чтобы не уйти в бесконечную рекурсию
        // Для зоны 3х3 нужно минимум 9 свободных клеток.
        val maxMines = totalCells - 9
        val currentMinesCount = if (minesCount > maxMines) maxMines else minesCount

        // Очищаем поле перед расстановкой (на случай ретрая)
        for (i in cells.indices) {
            cells[i] = cells[i].copy(isMine = false, adjacentMines = 0)
        }

        var minesPlaced = 0
        val random = java.util.Random()

        // 1. Расстановка мин
        while (minesPlaced < currentMinesCount) {
            val randomIndex = random.nextInt(totalCells)
            val targetCell = cells[randomIndex]

            // ПРОВЕРКА ЗОНЫ 3х3:
            // Если расстояние по X И по Y меньше или равно 1 — это зона 3х3 вокруг клика.
            val isInsideSafeZone = abs(targetCell.x - safeCell.x) <= 1 &&
                    abs(targetCell.y - safeCell.y) <= 1

            if (!targetCell.isMine && !isInsideSafeZone) {
                cells[randomIndex] = targetCell.copy(isMine = true)
                minesPlaced++
            }
        }

        // 2. Расчет цифр (оптимизировано)
        for (i in cells.indices) {
            val cell = cells[i]
            if (cell.isMine) continue

            var count = 0
            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    val nx = cell.x + dx
                    val ny = cell.y + dy

                    // Важно: nx проверяем по рядам, ny по колонкам (или наоборот, зависит от твоей логики)
                    if (nx in 0 until rows && ny in 0 until cols) {
                        // Используй индекс вместо .find для скорости
                        val neighborIndex = nx * cols + ny
                        if (cells.getOrNull(neighborIndex)?.isMine == true) {
                            count++
                        }
                    }
                }
            }
            cells[i] = cell.copy(adjacentMines = count)
        }

        // 3. Проверка на наличие "пустой" ячейки (0)
        // С зоной 3х3 это условие почти всегда выполняется автоматически,
        // но оставляем для надежности.
        if (cells.none { !it.isMine && it.adjacentMines == 0 }) {
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