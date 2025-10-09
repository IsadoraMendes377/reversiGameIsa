package model

typealias Score = Map<Player?, Int>

data class Game(
    val board: Board? = null,
    val firstPlayer: Player = Player.P1,
    val score: Score = mapOf(Player.P1 to 2, Player.P2 to 2, null to 0)
)

private fun Score.advance(player: Player?) =
    this - player + (player to checkNotNull(this[player]) + 1)

fun Game.newBoard() = Game(
    board = Board(turn = firstPlayer),
    firstPlayer = firstPlayer.other,
    score = mapOf(Player.P1 to 2, Player.P2 to 2, null to 0)
)

fun Game.play(pos: Position): Game {
    checkNotNull(board) { "Sem tabuleiro" }
    val boardAfter = board.play(pos)
    return copy(
        board = boardAfter,
        score = when (boardAfter) {
            is BoardWin -> score.advance(boardAfter.winner)
            is BoardDraw -> score.advance(null)
            is BoardRun -> score
        }
    )
}
