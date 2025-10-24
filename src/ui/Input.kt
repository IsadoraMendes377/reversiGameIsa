package ui

data class LineCommand(val name: String, val args: List<String>)

tailrec fun readCommand(): LineCommand {
    print("> ")
    val words = readln().trim().split(Regex("\\s+"))
    return if (words.isNotEmpty() && words.first().isNotBlank())
        LineCommand(words[0].uppercase(), words.drop(1))
    else readCommand()
}