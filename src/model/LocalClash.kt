package model

import storage.*
import ui.*

/**
 * Implementa o modo local do jogo Reversi:
 * ambos os jogadores jogam alternadamente na mesma instância (sem storage).
 */
class LocalClash(
    val game: Game,
    val currentPlayer: Player,
    val myCell: Cell
) : Clash(MemoryStorage()) {

    /** Executa uma jogada local (alternando o jogador). */
    fun play(pos: Position): LocalClash {
        val board = game.board as? BoardRun ?: error("Game already finished")
        require(board.turn == currentPlayer) { "Not your turn" }

        val newGame = game.play(pos)
        val nextPlayer = currentPlayer.other
        return LocalClash(newGame, nextPlayer, myCell)
    }

    /** Passa a vez, se o jogador atual não tiver jogadas possíveis. */
    fun pass(): LocalClash {
        val board = game.board as? BoardRun ?: error("Game already finished")
        val moves = validMoves(board.occupation, currentPlayer)
        require(moves.isEmpty()) { "You still have moves; cannot pass" }

        val newBoard = BoardRun(currentPlayer.other, board.occupation)
        val newGame = game.copy(board = newBoard)
        return LocalClash(newGame, currentPlayer.other, myCell)
    }

    /** Mostra o tabuleiro e informação básica. */
    fun show(targetsOn: Boolean = false) {
        val playerSymbol = if (myCell == Cell.BLACK) "#" else "@"
        println("You are player $playerSymbol in local game.")
        game.show(targetsOn)
    }

}
