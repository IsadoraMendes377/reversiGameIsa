package model

@JvmInline
value class Position private constructor(val index: Int) {
    init {
        require(index in 0 until BOARD_CELLS) { "Invalid position $index" }
    }
    val row: Int get() = index / BOARD_DIM
    val col: Int get() = index % BOARD_DIM

    override fun toString(): String {
        val colChar = 'a' + col
        return "$colChar${row + 1}"
    }

    companion object {
        val values: List<Position> = List(BOARD_CELLS) { Position(it) }
        operator fun invoke(idx: Int) = values[idx]

        /**
         * Parse textual cell like "d3" -> Position or null.
         * Accepts columns a-h (case insensitive) and rows 1..BOARD_DIM
         */
        fun fromTextOrNull(text: String): Position? {
            val t = text.trim().lowercase()
            if (t.length < 2) return null
            val c = t[0]
            val col = c - 'a'
            val row = t.substring(1).toIntOrNull()?.minus(1) ?: return null
            if (col !in 0 until BOARD_DIM || row !in 0 until BOARD_DIM) return null
            return Position(row * BOARD_DIM + col)
        }
    }
}