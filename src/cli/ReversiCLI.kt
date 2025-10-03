package cli

import model.*
import serialization.GameSerializer
import storage.TextFileStorage

class ReversiCLI {

    private var game: Game? = null
    private var showTargets = false
    private var turn = 1
    private val storage = TextFileStorage<String, Game>("games", GameSerializer)

    fun start() {
        println("Reversi CLI - Comandos: new | join | play | pass | refresh | targets | show | exit")

        while (true) {
            print("> ")
            val cmd = readlnOrNull()?.trim()?.split(" ") ?: break
            try {
                when (cmd[0].lowercase()) {
                    "new" -> {
                        val first = if (cmd.getOrNull(1) == "#") Player.P2 else Player.P1
                        val name = cmd.getOrNull(2)
                        game = Game().newBoard().copy(firstPlayer = first)
                        if (name != null) storage.create(name, game!!)
                        turn = 1
                        println("Novo jogo iniciado (${if (name == null) "local" else "nome=$name"})")
                    }
                    "join" -> {
                        val name = cmd[1]
                        game = storage.read(name) ?: error("Jogo $name não existe")
                        println("Ligado ao jogo $name")
                    }
                    "play" -> {
                        val move = parseMove(cmd[1])
                        game = game?.play(move) ?: error("Nenhum jogo em curso")
                        turn++
                    }
                    "pass" -> {
                        game = game?.copy(board = BoardRun((game!!.board as BoardRun).turn.other, game!!.board!!.moves))
                        turn++
                    }
                    "refresh" -> {
                        println("Atualizado (se aplicável).")
                        // reload from storage se tiver nome
                    }
                    "targets" -> {
                        val arg = cmd.getOrNull(1)?.uppercase()
                            ?: error("Uso: targets ON | targets OFF")
                        when (arg) {
                            "ON" -> showTargets = true
                            "OFF" -> showTargets = false
                            else -> error("Argumento inválido: use ON ou OFF")
                        }
                        println("Targets ${if (showTargets) "ON" else "OFF"}")
                    }

                    "show" -> showGame()
                    "exit" -> break
                    else -> println("Comando inválido: ${cmd[0]}")
                }
                showGame()
            } catch (e: Exception) {
                println("Erro: ${e.message}")
            }
        }
    }

    private fun showGame() {
        game?.let {
            it.board?.print(if (showTargets) it.board!!.validMoves() else emptyList())
        }
    }



    private fun parseMove(cell: String): Position {
        val row = cell[0].digitToInt() - 1
        val col = cell[1].lowercaseChar() - 'a'
        return Position(row * SIZE + col)
    }
}
