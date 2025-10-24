package main

import model.*
import storage.*
import ui.*

fun main() {
    var clash: Clash = Clash(TextFileStorage("games", GameSerializer))
    val cmds = getCommands()
    var targetsOn = false

    while (true) {
        val (name, args) = readCommand()
        val cmd = cmds[name]
        if (cmd == null) {
            println("Invalid command $name")
            continue
        }
        try {
            // TARGETS toggle (igual ao teu)
            if (name == "TARGETS") {
                if (args.isEmpty()) {
                    println("Targets: ${if (targetsOn) "ON" else "OFF"}")
                } else {
                    val arg = args[0].uppercase()
                    if (arg != "ON" && arg != "OFF")
                        throw IllegalArgumentException("Usage: TARGETS [ON|OFF]")
                    targetsOn = arg == "ON"
                    println("Targets now: ${if (targetsOn) "ON" else "OFF"}")
                }

                // mostrar estado após TARGETS
                when (clash) {
                    is LocalClash -> clash.show(targetsOn)
                    else -> clash.show(targetsOn)
                }
                continue
            }

            // executa comando
            clash = cmd.execute(args, clash)

            // terminar?
            if (cmd.isTerminate) break

            // mostrar estado atualizado
            when (clash) {
                is LocalClash -> clash.show(targetsOn)
                else -> clash.show(targetsOn)
            }

        } catch (e: IllegalArgumentException) {
            println("${e.message}. Use: $name ${cmd.syntax}")
        } catch (e: IllegalStateException) {
            println(e.message)
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }
}
