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
        return "${data.firstPlayer.name} # $score # $board"
    }

    override fun deserialize(text: String): Game {
        val parts = text.split(" # ")
        require(parts.size == 3) { "Invalid Game serialization: $text" }

        val (playerStr, scoreStr, boardStr) = parts
        val firstPlayer = parsePlayer(playerStr)

        val scoreMap: Score = scoreStr.split(" ").associate {
            val (ply, cnt) = it.split(":")
            val key = when (ply) {
                "null" -> null
                else -> parsePlayer(ply)
            }
            key to cnt.toInt()
        }

        val boardData = if (boardStr.isNotEmpty()) BoardSerializer.deserialize(boardStr) else null
        return Game(boardData, firstPlayer, scoreMap)
    }

    /**
     * Permite compatibilidade com ficheiros antigos ('@', '#') e novos ('P1', 'P2').
     */
    private fun parsePlayer(value: String): Player =
        when (value.uppercase()) {
            "P1", "@" -> Player.P1
            "P2", "#" -> Player.P2
            else -> error("Invalid player value: $value")
        }
}
