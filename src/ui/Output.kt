package ui

import model.*

fun Clash.show(targetsOn: Boolean = false) {
    when (this) {
        is LocalClash -> {
            val playerSymbol = if (myCell == Cell.BLACK) "#" else "@"
            println("You are player $playerSymbol in local game.")
        }
        is ClashRun -> {
            val playerSymbol = if (sidePlayer == Player.BLACK) "#" else "@"
            println("You are player $playerSymbol in ${name.value}.")
        }
    }
    (this as? ClashRun)?.game?.show(targetsOn)
    (this as? LocalClash)?.game?.show(targetsOn)
}


fun Game.show(targetsOn: Boolean = false) {
    board?.show(targetsOn)
}

/**
 * Mostra apenas os contadores (usado pelo comando SCORE).
 */
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
    println("# = $blackCnt | @ = $whiteCnt")
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

    // Cabeçalho
    print("  ")
    for (c in 0 until BOARD_DIM) print("${('A' + c)} ")
    println()

    // Linhas numeradas
    for (r in 0 until BOARD_DIM) {
        print("${r + 1} ")
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

    // Contadores e turno
    val (bCnt, wCnt) = countCells(occ)
    println("# = $bCnt | @ = $wCnt")
    println("Turn: ${br.turn.cell.symbol}")
}

private fun showEnd(be: BoardEnd) {
    // Cabeçalho
    print("  ")
    for (c in 0 until BOARD_DIM) print("${('A' + c)} ")
    println()

    // Linhas numeradas
    for (r in 0 until BOARD_DIM) {
        print("${r + 1} ")
        for (c in 0 until BOARD_DIM) {
            val pos = Position(r * BOARD_DIM + c)
            val ch = be.occupation[pos]?.symbol ?: '.'
            print("$ch ")
        }
        println()
    }

    // Resultado final
    println("Game over: #=${be.blackCount} | @=${be.whiteCount}")
}
