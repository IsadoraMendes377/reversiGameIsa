package model

val directions = listOf(
    -1 to -1, -1 to 0, -1 to 1,
    0 to -1,          0 to 1,
    1 to -1,  1 to 0, 1 to 1
)

// Extensão para obter jogadas válidas
fun Board.validMoves(): List<Position> = when(this) {
    is BoardRun -> Position.values.filter { isValid(it, turn) }
    else -> emptyList()
}

private fun BoardRun.isValid(pos: Position, player: Player): Boolean {
    if (pos in moves) return false
    return directions.any { canCapture(pos, player, it) }
}

private fun BoardRun.canCapture(pos: Position, player: Player, dir: Pair<Int,Int>): Boolean {
    var r = pos.row + dir.first
    var c = pos.col + dir.second
    var seenOpponent = false
    while (r in 0 until SIZE && c in 0 until SIZE) {
        val p = moves[Position(r,c)]
        when {
            p == player.other -> seenOpponent = true
            p == player -> return seenOpponent
            else -> return false
        }
        r += dir.first
        c += dir.second
    }
    return false
}

// Aplica jogada
fun Board.play(pos: Position): Board {
    require(this is BoardRun) { "Game is over" }
    require(isValid(pos, turn)) { "Invalid move $pos" }
    val newMoves = moves.toMutableMap()
    newMoves[pos] = turn
    for (d in directions) {
        if (canCapture(pos, turn, d)) {
            var r = pos.row + d.first
            var c = pos.col + d.second
            while (Position(r,c) in moves && moves[Position(r,c)] == turn.other) {
                newMoves[Position(r,c)] = turn
                r += d.first; c += d.second
            }
        }
    }
    val nextTurn = turn.other
    val remaining = Position.values - newMoves.keys
    val hasNext = remaining.any { BoardRun(nextTurn, newMoves).isValid(it, nextTurn) }
    return when {
        remaining.isEmpty() -> decideWinner(newMoves)
        !hasNext -> decideWinner(newMoves)
        else -> BoardRun(nextTurn, newMoves)
    }
}

// Decide vencedor ou empate
private fun decideWinner(moves: Moves): Board {
    val c1 = moves.values.count { it == Player.P1 }
    val c2 = moves.values.count { it == Player.P2 }
    return when {
        c1 > c2 -> BoardWin(Player.P1, moves)
        c2 > c1 -> BoardWin(Player.P2, moves)
        else -> BoardDraw(moves)
    }
}

// Impressão do tabuleiro
fun Board.print(valid: List<Position> = emptyList()) {
    println(" a b c d e f g h")
    for (r in 0 until SIZE) {
        print("${r + 1} ")
        for (c in 0 until SIZE) {
            val pos = Position(r, c)
            val cell = when (moves[pos]) {
                Player.P1 -> "@"
                Player.P2 -> "#"
                else -> if (valid.contains(pos)) "*" else "."
            }
            print("$cell ")
        }
        println()
    }
}
