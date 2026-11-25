package com.thuonegame.blockblast.core

data class Cell(val row: Int, val col: Int)
data class Piece(val cells: List<Cell>) {
    init { require(cells.isNotEmpty()); require(cells.all { it.row >= 0 && it.col >= 0 }) }
    val height: Int get() = cells.maxOf { it.row } + 1
    val width: Int get() = cells.maxOf { it.col } + 1
}
data class Placement(val pieceIndex: Int, val row: Int, val col: Int)
data class SolveResult(val placements: List<Placement>, val score: Int, val finalBoard: List<Int>)

object BlockBlastSolver {
    private const val SIZE = 8
    private data class State(val rows: IntArray, val used: Int, val score: Int, val moves: List<Placement>)

    fun solve(board: List<Int>, pieces: List<Piece>, beamWidth: Int = 180): SolveResult? {
        require(board.size == SIZE && board.all { it in 0..255 })
        require(beamWidth > 0)
        if (pieces.isEmpty() || pieces.size > 3) return null
        var beam = listOf(State(board.toIntArray(), 0, 0, emptyList()))
        repeat(pieces.size) {
            val next = ArrayList<State>()
            for (state in beam) {
                for (index in pieces.indices) {
                    if (state.used and (1 shl index) != 0) continue
                    val piece = pieces[index]
                    for (row in 0..(SIZE - piece.height)) {
                        for (col in 0..(SIZE - piece.width)) {
                            val placed = place(state.rows, piece, row, col) ?: continue
                            val (clearedBoard, lines) = clearLines(placed)
                            next.add(State(clearedBoard, state.used or (1 shl index),
                                state.score + lines * 100, state.moves + Placement(index, row, col)))
                        }
                    }
                }
            }
            if (next.isEmpty()) return beam.maxByOrNull(::evaluate)?.toResult()
            beam = next.distinctBy { it.used.toString() + ":" + it.rows.joinToString(",") }
                .sortedByDescending(::evaluate).take(beamWidth)
        }
        return beam.maxByOrNull(::evaluate)?.toResult()
    }

    private fun place(source: IntArray, piece: Piece, row: Int, col: Int): IntArray? {
        val result = source.copyOf()
        for (cell in piece.cells) {
            val r = row + cell.row
            val bit = 1 shl (col + cell.col)
            if (result[r] and bit != 0) return null
            result[r] = result[r] or bit
        }
        return result
    }

    private fun clearLines(source: IntArray): Pair<IntArray, Int> {
        val fullRows = (0 until SIZE).filter { source[it] == 255 }.toSet()
        val fullCols = (0 until SIZE).filter { c -> (0 until SIZE).all { r -> source[r] and (1 shl c) != 0 } }.toSet()
        if (fullRows.isEmpty() && fullCols.isEmpty()) return source to 0
        val result = source.copyOf()
        for (r in 0 until SIZE) for (c in 0 until SIZE) {
            if (r in fullRows || c in fullCols) result[r] = result[r] and (1 shl c).inv()
        }
        return result to (fullRows.size + fullCols.size)
    }

    private fun evaluate(s: State): Int {
        val occupied = s.rows.sumOf { Integer.bitCount(it) }
        val nearRows = s.rows.count { Integer.bitCount(it) >= 6 }
        val nearCols = (0 until SIZE).count { c -> (0 until SIZE).count { r -> s.rows[r] and (1 shl c) != 0 } >= 6 }
        val empty = SIZE * SIZE - occupied
        return s.score * 100 + empty - nearRows * 7 - nearCols * 7
    }

    private fun State.toResult() = SolveResult(moves, score, rows.toList())

    fun line(length: Int, horizontal: Boolean = true): Piece {
        require(length in 1..SIZE)
        return Piece((0 until length).map { if (horizontal) Cell(0, it) else Cell(it, 0) })
    }

    fun rectangle(height: Int, width: Int): Piece {
        require(height in 1..SIZE && width in 1..SIZE)
        return Piece((0 until height).flatMap { r -> (0 until width).map { c -> Cell(r, c) } })
    }
}
