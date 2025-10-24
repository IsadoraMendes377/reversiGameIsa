package model

import storage.Serializer

/**
 * Serializa e desserializa um tabuleiro de Reversi em texto.
 * Exemplo textual:
 * 8
 * --------
 * ----@---
 * ---##---
 * ...
 */
object BoardSerializer : Serializer<Board> {
    override fun serialize(data: Board): String {
        val occ = data.occupation.entries.joinToString(" ") { "${it.key.index}:${it.value.symbol}" }
        return when (data) {
            is BoardRun -> "RUN ${data.turn} | $occ"
            is BoardEnd -> "END ${data.blackCount}:${data.whiteCount} | $occ"
        }
    }

    override fun deserialize(text: String): Board {
        val parts = text.split(" | ")
        val left = parts[0].trim()
        val occPart = if (parts.size > 1) parts[1].trim() else ""
        val occ = if (occPart.isBlank()) emptyMap()
        else occPart.split(" ").associate {
            val (idx, sym) = it.split(":")
            Position(idx.toInt()) to Cell.fromChar(sym.first())
        }
        val leftTokens = left.split(" ")
        return when (leftTokens[0]) {
            "RUN" -> {
                val player = Player.valueOf(leftTokens[1])
                BoardRun(player, occ)
            }
            "END" -> {
                val (b, w) = leftTokens[1].split(":").let { it[0].toInt() to it[1].toInt() }
                BoardEnd(b, w, occ)
            }
            else -> error("Invalid board data")
        }
    }
}