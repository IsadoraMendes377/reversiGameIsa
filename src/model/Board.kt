package model

const val BOARD_DIM = 8
const val BOARD_CELLS = BOARD_DIM * BOARD_DIM

typealias Occupation = Map<Position, Cell>

sealed class Board(open val occupation: Occupation) {
    override fun equals(other: Any?) = other is Board && occupation == other.occupation
    override fun hashCode() = occupation.hashCode()
}

data class BoardRun(val turn: Player, override val occupation: Occupation) : Board(occupation)
data class BoardEnd(val blackCount: Int, val whiteCount: Int, override val occupation: Occupation) : Board(occupation)

object BoardFactory {
    fun initial(firstTurn: Player = Player.BLACK): BoardRun {
        val mid = BOARD_DIM / 2
        val mut = mutableMapOf<Position, Cell>()
        // posições iniciais padrão
        mut[Position((mid - 1) * BOARD_DIM + (mid - 1))] = Cell.WHITE
        mut[Position(mid * BOARD_DIM + mid)] = Cell.WHITE
        mut[Position((mid - 1) * BOARD_DIM + mid)] = Cell.BLACK
        mut[Position(mid * BOARD_DIM + (mid - 1))] = Cell.BLACK
        return BoardRun(firstTurn, mut.toMap())
    }
}


private val DR = intArrayOf(-1, -1, -1, 0, 0, 1, 1, 1)
private val DC = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)

private fun inBounds(r: Int, c: Int) = r in 0 until BOARD_DIM && c in 0 until BOARD_DIM

/**
 * Returns the list of positions that would be flipped by placing 'cell' at position p,
 * according to Reversi rules. Empty list if move is invalid.
 */
fun flipsForMove(occupation: Occupation, p: Position, cell: Cell): List<Position> {
    if (occupation.containsKey(p)) return emptyList()
    val opp = cell.opposite()
    val result = mutableListOf<Position>()
    for (d in DR.indices) {
        var nr = p.row + DR[d]
        var nc = p.col + DC[d]
        val chain = mutableListOf<Position>()
        while (inBounds(nr, nc)) {
            val pos = Position(nr * BOARD_DIM + nc)
            val occ = occupation[pos]
            if (occ == opp) {
                chain.add(pos)
                nr += DR[d]; nc += DC[d]
            } else {
                if (occ == cell && chain.isNotEmpty()) {
                    result.addAll(chain)
                }
                break
            }
        }
    }
    return result
}

fun validMoves(occupation: Occupation, player: Player): Set<Position> {
    val cell = player.cell
    val moves = mutableSetOf<Position>()
    for (pos in Position.values) {
        if (!occupation.containsKey(pos)) {
            if (flipsForMove(occupation, pos, cell).isNotEmpty()) moves.add(pos)
        }
    }
    return moves
}

fun applyMove(occupation: Occupation, pos: Position, player: Player): Occupation {
    val flips = flipsForMove(occupation, pos, player.cell)
    require(flips.isNotEmpty()) { "Invalid move" }
    val newMap = occupation.toMutableMap()
    newMap[pos] = player.cell
    for (f in flips) newMap[f] = player.cell
    return newMap.toMap()
}

fun countCells(occupation: Occupation): Pair<Int, Int> {
    var b = 0; var w = 0
    for (v in occupation.values) {
        when (v) {
            Cell.BLACK -> b++
            Cell.WHITE -> w++
            else -> {}
        }
    }
    return b to w
}