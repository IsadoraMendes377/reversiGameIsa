package model

enum class Player(val symbol: Char) {
    P1('@'),
    P2('#');

    val other get() = if (this == P1) P2 else P1

    override fun toString() = symbol.toString()
}
