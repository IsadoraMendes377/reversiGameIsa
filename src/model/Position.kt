package model

const val SIZE = 8
const val CELLS = SIZE * SIZE

@JvmInline
value class Position private constructor(val index: Int) {
    init { require(index in 0 until CELLS) { "Invalid index $index" } }

    val row get() = index / SIZE
    val col get() = index % SIZE

    companion object {
        val values = List(CELLS) { Position(it) }
        operator fun invoke(r: Int, c: Int) = values[r * SIZE + c]
        operator fun invoke(idx: Int) = values[idx]
    }

    override fun toString() = "${row+1}${'a' + col}"
}
