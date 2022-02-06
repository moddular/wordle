package org.moddular

import java.io.InputStream
import java.io.PrintStream
import java.util.*

sealed interface Guess {
    val value: Char
}
data class Correct(override val value: Char) : Guess
data class WrongPosition(override val value: Char) : Guess
data class Incorrect(override val value: Char) : Guess

class Attempt(private val solution: String, private val guess: String) {
    fun check(): List<Guess> {
        val stillToGuess = solution.filterIndexed { i, c -> c != guess[i] }.groupingBy { it }.eachCount()
        val guessWithCorrectCharsReplaced = guess.mapIndexed { i, c -> if (solution[i] == c) '_' else c }

        return guess.mapIndexed { i, c ->
            when {
                solution[i] == c -> Correct(c)
                stillToGuess.getOrDefault(c, 0) >= guessWithCorrectCharsReplaced.take( i + 1).count { it == c } -> WrongPosition(c)
                else -> Incorrect(c)
            }
        }
    }

    fun invalid() =
        solution.length != guess.length || guess.any { !it.isLetter() }

    fun correct() =
        solution == guess
}

class Problem(val solution: String) {
    private val attempts = mutableListOf<Attempt>()

    fun attempt(guess: String): Pair<Int, Attempt> {
        val attempt = Attempt(solution, guess.lowercase().trim())
        if (!attempt.invalid()) {
            attempts.add(attempt)
        }
        return Pair(attempts.size, attempt)
    }
}

class Terminal(private val input: InputStream, private val output: PrintStream) {
    private fun correct(letter: String) =
        "\u001b[32m${letter}\u001b[0m"

    private fun wrongPosition(letter: String) =
        "\u001b[33m${letter}\u001b[0m"

    private fun incorrect(letter: String) =
        "\u001b[37m${letter}\u001B[0m"

    private fun asColouredString(count: Int, chars: List<Guess>) =
        "${count}. " + chars.joinToString("") {
            when (it) {
                is Correct -> correct(it.value.toString())
                is WrongPosition -> wrongPosition(it.value.toString())
                is Incorrect -> incorrect(it.value.toString())
            }
        }

    fun message(message: String) {
        output.println(message)
    }

    fun run(problem: Problem) {
        val scanner = Scanner(input)

        output.println("A letter in the correct place is shown in ${correct("green")}")
        output.println("A letter that appears in the word, but is not in the right place is shown in ${wrongPosition("yellow")}")
        output.println("A letter that doesn't appear in the word is shown in ${incorrect("grey")}")
        output.println()

        while (true) {
            output.print("Enter guess: ")

            val (count, attempt) = problem.attempt(scanner.nextLine())
            val exit = when {
                attempt.invalid() -> {
                    output.println("Your guess must be ${problem.solution.length} letters")
                    false
                }
                attempt.correct() -> {
                    output.println(asColouredString(count, attempt.check()))
                    output.println("Correct you got it in ${count}! The word was ${problem.solution}")
                    true
                }
                else -> {
                    output.println(asColouredString(count, attempt.check()))
                    if (count > 5) {
                        output.println("\nOh no, you're out of guesses... The word you were looking for was ${problem.solution}")
                        true
                    } else {
                        false
                    }
                }
            }

            if (exit) {
                return
            }
        }
    }
}

class Dictionary {
    private val words = listOf("hello", "foods", "bobby", "table", "fizzy", "fuzzy", "pizza", "angry", "above", "about", "abort", "elbow", "thigh", "heads", "knits", "shoes", "think", "thank", "thunk")

    fun random(length: Int) =
        words.filter { it.length == length }.let { if (it.isNotEmpty()) it.random() else null }
}

class Wordle(private val terminal: Terminal) {
    fun solve(length: Int) {
        val word = Dictionary().random(length)
        if (word != null) {
            terminal.run(Problem(word))
        } else {
            terminal.message("No $length letter words found in the dictionary")
        }
    }
}

fun main(args: Array<String>) {
    Wordle(Terminal(System.`in`, System.out)).solve(args.firstOrNull()?.toInt() ?: 5)
}
