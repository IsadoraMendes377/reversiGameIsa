package model

enum class Cell(val symbol: Char) {
    EMPTY('.'), BLACK('#'), WHITE('@');

    fun opposite(): Cell = when (this) {
        BLACK -> WHITE
        WHITE -> BLACK
        else -> EMPTY
    }

    companion object {
        fun fromChar(c: Char): Cell = when (c) {
            '#' -> BLACK
            '@' -> WHITE
            else -> EMPTY
        }
    }
}