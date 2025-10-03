package serialization

import model.*
import storage.Serializer

/**
 * Serializa e desserializa o estado de um jogo de Reversi.
 *
 * Formato: <firstPlayer> # <score> # <board>
 * Exemplo:
 *   P1 # P1:12 P2:15 null:0 # RUN P1 | 27:P1 28:P2 ...
 */
object GameSerializer : Serializer<Game> {
    override fun serialize(data: Game): String {
        val score = data.score.map { "${it.key}:${it.value}" }.joinToString(" ")
        val board = data.board?.let { BoardSerializer.serialize(it) } ?: ""
        return "${data.firstPlayer} # $score # $board"
    }

    override fun deserialize(text: String): Game {
        val parts = text.split(" # ")
        require(parts.size == 3) { "Invalid Game serialization: $text" }

        val (player, score, board) = parts
        val firstPlayer = Player.valueOf(player)

        val scoreMap: Score = score.split(" ").associate {
            val (ply, cnt) = it.split(":")
            val key = if (ply == "null") null else Player.valueOf(ply)
            key to cnt.toInt()
        }

        val boardData = if (board.isNotEmpty()) BoardSerializer.deserialize(board) else null
        return Game(boardData, firstPlayer, scoreMap)
    }
}
