package model

import storage.Storage

@JvmInline
value class Name(val value: String) {
    init {
        require(value.isNotEmpty() && value.all { it.isLetterOrDigit() }) { "Name not valid" }
    }
    override fun toString() = value
}

typealias GameStorage = Storage<Name, Game>

open class Clash(val storage: GameStorage)

class ClashRun(
    storage: GameStorage,
    val name: Name,
    val game: Game,
    val sidePlayer: Player
) : Clash(storage)

/** start a named clash: create game and store it */
fun Clash.start(name: Name): Clash =
    ClashRun(storage, name, Game().newBoard(), Player.BLACK)
        .also { storage.create(name, it.game) }

/** join an existing clash as opposing side */
/** join an existing clash as opposing side */
fun Clash.join(name: Name): Clash {
    val existingGame = checkNotNull(storage.read(name)) { "Clash $name not found" }
    val board = existingGame.board
    val firstTurn = when (board) {
        is BoardRun -> board.turn
        is BoardEnd -> existingGame.firstPlayer
        else -> existingGame.firstPlayer
    }
    // o jogador que entra joga com a cor oposta
    val joiner = firstTurn.other
    return ClashRun(storage, name, existingGame, joiner)
}


/**
 * Helper to ensure we are inside a ClashRun and return a new ClashRun
 * with the game computed by the provided lambda.
 */
private fun Clash.onClashRun(getGame: ClashRun.() -> Game): Clash {
    check(this is ClashRun) { "There is no clash yet" }
    val newGame = getGame(this)
    return ClashRun(storage, name, newGame, sidePlayer)
}

/** refresh ensures there is a change in the stored game */
fun Clash.refresh() = onClashRun {
    val stored = checkNotNull(storage.read(name)) { "Clash $name not found" }
    check(stored != game) { "No changes in clash $name" }
    stored
}

/** play a position: checks turn and updates storage */
fun Clash.play(pos: Position) = onClashRun {
    val curBoard = game.board
    check(curBoard is BoardRun && curBoard.turn == sidePlayer) { "Not your turn" }
    val newGame = game.play(pos)
    storage.update(name, newGame)
    newGame
}

/** start a new board within an existing clash */
fun Clash.newBoard() = onClashRun {
    val newGame = game.newBoard()
    storage.update(name, newGame)
    newGame
}