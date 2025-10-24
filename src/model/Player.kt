package model

enum class Player(val cell: Cell) {
    BLACK(Cell.BLACK), WHITE(Cell.WHITE);

    val other get() = if (this == BLACK) WHITE else BLACK
}