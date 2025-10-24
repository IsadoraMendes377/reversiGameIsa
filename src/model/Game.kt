package model

import storage.Serializer

typealias Score = Map<Player, Int>

/**
 * Representa o estado completo de um jogo Reversi.
 * @param board tabuleiro actual (BoardRun ou BoardEnd)
 * @param firstPlayer jogador que começou este round
 * @param score número actual de peças no tabuleiro por jogador
 */
data class Game(
    val board: Board? = null,
    val firstPlayer: Player = Player.BLACK,
    val score: Score = mapOf(Player.BLACK to 2, Player.WHITE to 2)
)

/**
 * Serializador do Game.
 * Formato: <firstPlayer> # BLACK:<n> WHITE:<m> # <board>
 */
object GameSerializer : Serializer<Game> {
    override fun serialize(data: Game): String {
        val score = data.score.entries.joinToString(" ") { "${it.key}:${it.value}" }
        val boardStr = data.board?.let { BoardSerializer.serialize(it) } ?: ""
        return "${data.firstPlayer} # $score # $boardStr"
    }

    override fun deserialize(text: String): Game {
        val parts = text.split(" # ")
        require(parts.size >= 3) { "Invalid game format: $text" }
        val firstPlayer = Player.valueOf(parts[0])
        val scoreMap = parts[1].split(" ").associate {
            val (ply, cnt) = it.split(":")
            Player.valueOf(ply) to cnt.toInt()
        }
        val board = if (parts[2].isNotBlank()) BoardSerializer.deserialize(parts[2]) else null
        return Game(board, firstPlayer, scoreMap)
    }
}

/**
 * Cria um novo tabuleiro para o round atual.
 */
fun Game.newBoard(): Game {
    val newBoard = BoardFactory.initial()
    val nextFirst = firstPlayer.other
    val counts = countCells(newBoard.occupation)
    val scoreMap = mapOf(Player.BLACK to counts.first, Player.WHITE to counts.second)
    return copy(board = newBoard, firstPlayer = nextFirst, score = scoreMap)
}

/**
 * Realiza a jogada em `pos` e devolve um novo Game com board actualizado e score recalculado.
 */
fun Game.play(pos: Position): Game {
    val b = board ?: error("No board")
    return when (b) {
        is BoardEnd -> error("Game already finished")
        is BoardRun -> {
            // validação: pos deve ser uma jogada válida para o turno actual
            val valid = validMoves(b.occupation, b.turn)
            require(pos in valid) { "Invalid move" }

            // aplica a jogada
            val newOcc = applyMove(b.occupation, pos, b.turn)
            val (blackCnt, whiteCnt) = countCells(newOcc)
            val total = blackCnt + whiteCnt

            // determina se o jogo terminou
            val nextBoard: Board = if (total == BOARD_CELLS ||
                (validMoves(newOcc, b.turn.other).isEmpty() && validMoves(newOcc, b.turn).isEmpty())
            ) {
                BoardEnd(blackCnt, whiteCnt, newOcc)
            } else {
                BoardRun(b.turn.other, newOcc)
            }

            val newScore = mapOf(Player.BLACK to blackCnt, Player.WHITE to whiteCnt)
            copy(board = nextBoard, score = newScore)
        }
    }
}