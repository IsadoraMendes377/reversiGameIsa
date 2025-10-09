package cli

import model.*
import serialization.GameSerializer
import storage.TextFileStorage

class ReversiCLI {

    private var game: Game? = null                     // única variável mutável de estado
    private var showTargets = false                    // configuração da interface
    private val storage = TextFileStorage<String, Game>("games", GameSerializer)

    fun start() {
        println("Reversi CLI - Comandos: new | join | play | pass | refresh | targets | show | exit")

        while (true) {
            print("> ")
            val cmd = readlnOrNull()?.trim()?.split(" ") ?: break
            try {
                when (cmd[0].lowercase()) {
                    "new" -> {
                        val firstSymbol = cmd.getOrNull(1)
                        val name = cmd.getOrNull(2)

                        val firstPlayer =
                            when (firstSymbol) {
                                "#" -> Player.P2 // pretas
                                "@", null -> Player.P1 // brancas
                                else -> error("Símbolo inválido: use # ou @")
                            }

                        val newGame = Game().newBoard().copy(firstPlayer = firstPlayer)
                        game = newGame
                        if (name != null) storage.create(name, newGame)

                        println("Novo jogo iniciado (${if (name == null) "local" else "nome=$name"})")
                    }

                    "join" -> {
                        val name = cmd.getOrNull(1) ?: error("Uso: join <nome>")
                        val joined = storage.read(name) ?: error("Jogo $name não existe")

                        val joinedGame = joined.copy(
                            firstPlayer = joined.firstPlayer.other // o adversário joga com a outra cor
                        )

                        game = joinedGame
                        println("Ligado ao jogo $name")
                    }

                    "play" -> {
                        val move = parseMove(cmd.getOrNull(1) ?: error("Uso: play <célula>"))
                        val current = game ?: error("Nenhum jogo em curso")
                        val board = current.board ?: error("Sem tabuleiro")

                        if (board !is BoardRun)
                            error("O jogo já terminou")

                        val currentTurn = board.turn
                        if (currentTurn != current.firstPlayer)
                            error("Não é a sua vez de jogar (${currentTurn.symbol})")

                        val newGame = current.play(move)
                        game = newGame

                        // Se o jogo tiver nome, atualizar o ficheiro
                        storage.updateIfExists(newGame)

                        println("Jogada efetuada: $move (${currentTurn.symbol})")
                    }

                    "pass" -> {
                        val current = game ?: error("Nenhum jogo em curso")
                        val board = current.board
                        require(board is BoardRun) { "O jogo já terminou" }

                        val valid = board.validMoves()
                        require(valid.isEmpty()) { "Ainda há jogadas possíveis. Não pode passar." }

                        // Passa a vez para o adversário
                        val nextBoard = BoardRun(board.turn.other, board.moves)
                        game = current.copy(board = nextBoard)

                        storage.updateIfExists(game!!)
                        println("Passou a vez para o adversário (${nextBoard.turn.symbol}).")
                    }

                    "refresh" -> {
                        val name = storage.findNameFor(game)
                        if (name != null) {
                            val updated = storage.read(name)
                            if (updated != null) {
                                game = updated
                                println("Jogo atualizado do ficheiro.")
                            } else println("Nenhum estado novo encontrado.")
                        } else println("Comando refresh só tem efeito em jogos nomeados.")
                    }

                    "targets" -> {
                        val arg = cmd.getOrNull(1)?.uppercase()
                        when (arg) {
                            "ON" -> showTargets = true
                            "OFF" -> showTargets = false
                            null -> {
                                println("Targets estão ${if (showTargets) "ON" else "OFF"}")
                                continue
                            }
                            else -> error("Argumento inválido: use ON ou OFF")
                        }
                        println("Targets ${if (showTargets) "ON" else "OFF"}")
                    }

                    "show" -> showGame()
                    "exit" -> break
                    else -> println("Comando inválido: ${cmd[0]}")
                }

                if (cmd[0].lowercase() != "targets") showGame()

            } catch (e: Exception) {
                println("Erro: ${e.message}")
            }
        }
    }

    private fun showGame() {
        game?.let {
            val valid = if (showTargets && it.board is BoardRun)
                it.board.validMoves()
            else emptyList()

            it.board?.print(valid)
        } ?: println("Nenhum jogo em curso.")
    }

    private fun parseMove(cell: String): Position {
        val row = cell[0].digitToInt() - 1
        val col = cell[1].lowercaseChar() - 'a'
        return Position(row, col)
    }

    // auxiliares para refresh e atualização
    private fun TextFileStorage<String, Game>.findNameFor(game: Game?): String? {
        val folder = java.io.File("games")
        val serialized = GameSerializer.serialize(game ?: return null)
        return folder.listFiles()
            ?.find { it.readText() == serialized }
            ?.nameWithoutExtension
    }


    private fun TextFileStorage<String, Game>.updateIfExists(game: Game) {
        val name = findNameFor(game)
        if (name != null) update(name, game)
    }
}
