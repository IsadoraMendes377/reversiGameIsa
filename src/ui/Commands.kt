package ui

import model.*

class Command(
    val syntax: String = "",
    val isTerminate: Boolean = false,
    val execute: (args: List<String>, clash: Clash) -> Clash = { _, clash -> clash }
)

/* ---- Comandos principais ---- */

private val playCommand = Command("<cell>") { args, clash ->
    require(args.isNotEmpty()) { "Missing position" }
    val pos = requireNotNull(Position.fromTextOrNull(args[0])) { "Illegal position ${args[0]}" }

    when (clash) {
        is LocalClash -> clash.play(pos)
        else -> clash.play(pos)
    }
}

private val passCommand = Command("pass") { _, clash ->
    when (clash) {
        is LocalClash -> clash.pass()
        is ClashRun -> {
            val board = clash.game.board as? BoardRun ?: error("Game already finished")
            val player = board.turn
            val myMoves = validMoves(board.occupation, player)
            require(myMoves.isEmpty()) { "You still have moves; cannot pass" }

            val newBoard = BoardRun(player.other, board.occupation)
            val newGame = clash.game.copy(board = newBoard)
            clash.storage.update(clash.name, newGame)
            ClashRun(clash.storage, clash.name, newGame, clash.sidePlayer)
        }
        else -> error("There is no clash yet")
    }
}

/**
 * Comando NEW:
 * - new #          → jogo local (jogador com pretas)
 * - new @          → jogo local (jogador com brancas)
 * - new # <nome>   → jogo remoto (cria jogo nomeado)
 * - new @ <nome>   → jogo remoto (cria jogo nomeado)
 */
private val newCommand = Command("<#|@> [<name>]") { args, clash ->
    require(args.isNotEmpty()) { "Missing color (# or @)" }

    val myCell = when (args[0]) {
        "#" -> Cell.BLACK
        "@" -> Cell.WHITE
        else -> throw IllegalArgumentException("Invalid color")
    }

    // determinar o jogador inicial com base na cor escolhida
    val firstTurn = if (myCell == Cell.BLACK) Player.BLACK else Player.WHITE

    if (args.size == 1) {
        // jogo local
        val initialBoard = BoardFactory.initial(firstTurn)
        val counts = countCells(initialBoard.occupation)
        val scoreMap = mapOf(Player.BLACK to counts.first, Player.WHITE to counts.second)
        val newGame = Game(initialBoard, firstTurn, scoreMap)
        LocalClash(newGame, firstTurn, myCell)
    } else {
        // jogo remoto
        val name = Name(args[1])
        val initialBoard = BoardFactory.initial(firstTurn)
        val counts = countCells(initialBoard.occupation)
        val scoreMap = mapOf(Player.BLACK to counts.first, Player.WHITE to counts.second)
        val newGame = Game(initialBoard, firstTurn, scoreMap)
        ClashRun(clash.storage, name, newGame, firstTurn)
            .also { clash.storage.create(name, newGame) }
    }

}

private fun commandNoArgs(fx: Clash.() -> Clash) = Command { _, clash -> clash.fx() }
private fun commandWithName(fx: Clash.(Name) -> Clash) =
    Command("<name>") { args, clash ->
        require(args.isNotEmpty()) { "Missing name" }
        clash.fx(Name(args[0]))
    }

/* ---- Tabela de comandos ---- */
fun getCommands() = mapOf(
    "EXIT" to Command(isTerminate = true),
    "NEW" to newCommand,
    "JOIN" to commandWithName(Clash::join),
    "PLAY" to playCommand,
    "PASS" to passCommand,
    "REFRESH" to commandNoArgs(Clash::refresh),
    "NEWBOARD" to commandNoArgs(Clash::newBoard),
    "SCORE" to Command { _, clash ->
        when (clash) {
            is LocalClash -> clash.game.showScore()
            is ClashRun -> clash.game.showScore()
        }
        clash
    },
    "TARGETS" to Command("<ON|OFF>") { args, clash -> clash }
)
