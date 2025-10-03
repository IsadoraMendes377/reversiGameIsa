package model

typealias Moves = Map<Position, Player>

sealed class Board(val moves: Moves) {
    override fun equals(other: Any?) = other is Board && moves == other.moves
    override fun hashCode() = moves.hashCode()
}

class BoardRun(val turn: Player, moves: Moves): Board(moves)
class BoardWin(val winner: Player, moves: Moves): Board(moves)
class BoardDraw(moves: Moves): Board(moves)

// Criação inicial
fun Board(turn: Player = Player.P1): Board = BoardRun(turn, initBoard())

// Inicializa tabuleiro 8x8
private fun initBoard(): Moves {
    val m = mutableMapOf<Position, Player>()
    val mid = SIZE / 2
    m[Position(mid-1, mid-1)] = Player.P1
    m[Position(mid, mid)]     = Player.P1
    m[Position(mid-1, mid)]   = Player.P2
    m[Position(mid, mid-1)]   = Player.P2
    return m
}
