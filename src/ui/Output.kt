package ui

import model.*

private val sepLine = "  " + (('a' until ('a' + BOARD_DIM)).joinToString(" ") { " " })

fun Clash.show(targetsOn: Boolean = false) {
    val cr = this as? ClashRun ?: return
    println("Clash: ${cr.name} you are ${cr.sidePlayer}")
    cr.game.show(targetsOn)
}

fun Game.show(targetsOn: Boolean = false) {
    board?.show(targetsOn)
}

fun Game.showScore() {
    val b = board
    if (b == null) {
        println("No board")
        return
    }
    // conta as peças no tabuleiro actual
    val (blackCnt, whiteCnt) = when (b) {
        is BoardRun -> countCells(b.occupation)
        is BoardEnd -> b.blackCount to b.whiteCount
        else -> 0 to 0
    }
    println("${Cell.BLACK.symbol} = $blackCnt")
    println("${Cell.WHITE.symbol} = $whiteCnt")
}


fun Board.show(targetsOn: Boolean = false) {
    when (this) {
        is BoardRun -> showRun(this, targetsOn)
        is BoardEnd -> showEnd(this)
    }
}

private fun showRun(br: BoardRun, targetsOn: Boolean) {
    val occ = br.occupation
    val targets = if (targetsOn) validMoves(occ, br.turn) else emptySet()
    // header
    print("  ")
    for (c in 0 until BOARD_DIM) print("${('a' + c)} ")
    println()
    for (r in 0 until BOARD_DIM) {
        print("${r + 1} ".padStart(2))
        for (c in 0 until BOARD_DIM) {
            val pos = Position(r * BOARD_DIM + c)
            val ch = when {
                pos in targets -> '*'
                pos in occ -> occ[pos]!!.symbol
                else -> '.'
            }
            print("$ch ")
        }
        println()
    }
    val (bCnt, wCnt) = countCells(occ)
    println("Turn: ${br.turn}  Score: ${Cell.BLACK.symbol}=$bCnt ${Cell.WHITE.symbol}=$wCnt")
}

private fun showEnd(be: BoardEnd) {
    // show final occupation grid
    print("  ")
    for (c in 0 until BOARD_DIM) print("${('a' + c)} ")
    println()
    for (r in 0 until BOARD_DIM) {
        print("${r + 1} ".padStart(2))
        for (c in 0 until BOARD_DIM) {
            val pos = Position(r * BOARD_DIM + c)
            val ch = be.occupation[pos]?.symbol ?: '.'
            print("$ch ")
        }
        println()
    }
    println("Game over: #=${be.blackCount} @=${be.whiteCount}")
}