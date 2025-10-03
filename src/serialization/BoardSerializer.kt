package serialization

import model.*
import storage.Serializer

/**
 * Serializa e desserializa o estado do tabuleiro de Reversi.
 *
 * Formatos de exemplo:
 *   RUN P1 | 27:@ 28:# ...
 *   WIN P2 | 15:# 16:@ ...
 *   DRAW   |  ...
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
                Position(pos.toInt()) to Player.valueOf(ply)
            }

        return when (type) {
            "RUN" -> BoardRun(Player.valueOf(player!!), moves)
            "WIN" -> BoardWin(Player.valueOf(player!!), moves)
            "DRAW" -> BoardDraw(moves)
            else -> error("Invalid board state: $type")
        }
    }
}
