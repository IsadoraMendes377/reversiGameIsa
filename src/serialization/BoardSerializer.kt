package serialization

import model.*
import storage.Serializer

/**
 * Serializa e desserializa o estado do tabuleiro de Reversi.
 *
 * Formatos de exemplo:
 *   RUN P1 | 27:P1 28:P2 ...
 *   WIN P2 | 15:P2 16:P1 ...
 *   DRAW   | ...
 */
object BoardSerializer : Serializer<Board> {

    override fun serialize(data: Board): String {
        val moves = data.moves.map { "${it.key.index}:${it.value.name}" }.joinToString(" ")
        return when (data) {
            is BoardRun -> "RUN ${data.turn}"
            is BoardWin -> "WIN ${data.winner}"
            is BoardDraw -> "DRAW"
        } + " | $moves"
    }

    override fun deserialize(text: String): Board {
        val (state, plays) = text.split(" | ")
        val parts = state.split(" ")
        val type = parts[0]
        val player = parts.getOrNull(1)

        val moves: Moves =
            if (plays.isEmpty()) emptyMap()
            else plays.split(" ").associate {
                val (pos, ply) = it.split(":")
                Position(pos.toInt()) to parsePlayer(ply)
            }

        return when (type) {
            "RUN" -> BoardRun(parsePlayer(player!!), moves)
            "WIN" -> BoardWin(parsePlayer(player!!), moves)
            "DRAW" -> BoardDraw(moves)
            else -> error("Invalid board state: $type")
        }
    }

    /**
     * Permite ler tanto valores antigos ('@', '#') como novos ('P1', 'P2').
     */
    private fun parsePlayer(value: String): Player =
        when (value.uppercase()) {
            "P1", "@" -> Player.P1
            "P2", "#" -> Player.P2
            else -> error("Invalid player value: $value")
        }
}
