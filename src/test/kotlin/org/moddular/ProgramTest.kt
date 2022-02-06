package org.moddular

import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import com.natpryce.hamkrest.assertion.assertThat

class ProgramTest {

    @Test
    fun `should handle a correct guess`() {
        val problem = Problem("hello")
        assertThat(result(problem,"hello").correct(), equalTo(true))
    }

    @Test
    fun `should handle a guess in mixed case`() {
        val problem = Problem("hello")
        assertThat(result(problem,"HeLlo").correct(), equalTo(true))
    }

    @Test
    fun `should handle an incorrect guess`() {
        val problem = Problem("hello")
        assertThat(result(problem,"yello").correct(), equalTo(false))
    }

    @Test
    fun `should handle a guess that is too short`() {
        val problem = Problem("hello")
        assertThat(result(problem, "ello").invalid(), equalTo(true))
    }

    @Test
    fun `should handle a guess that is too long`() {
        val problem = Problem("hello")
        assertThat(result(problem, "helloh").invalid(), equalTo(true))
    }

    @Test
    fun `should handle a guess that contains numbers`() {
        val problem = Problem("hello")
        assertThat(result(problem, "hell0").invalid(), equalTo(true))
    }

    @Test
    fun `should handle a guess that contains punctuation`() {
        val problem = Problem("hello")
        assertThat(result(problem, "hell.").invalid(), equalTo(true))
    }

    @Test
    fun `should count the attempts`() {
        val problem = Problem("hello")
        assertThat(count(problem, "check"), equalTo(1))
        assertThat(count(problem, "jello"), equalTo(2))
    }

    @Test
    fun `should be able to check the state of each letter in a correct solution`() {
        val problem = Problem("hello")
        assertThat(result(problem, "hello").check(), equalTo(listOf(
            Correct('h'),
            Correct('e'),
            Correct('l'),
            Correct('l'),
            Correct('o')
        )))
    }

    @Test
    fun `should be able to check the state of each letter in a solution where some letters are in the wrong position`() {
        val problem = Problem("hello")
        assertThat(result(problem, "ehlol").check(), equalTo(listOf(
           WrongPosition('e'),
           WrongPosition('h'),
           Correct('l'),
           WrongPosition('o'),
           WrongPosition('l')
        )))
    }

    @Test
    fun `should be able to check the state of each letter in solution where some letters are incorrect`() {
        val problem = Problem("hello")
        assertThat(result(problem, "abcde").check(), equalTo(listOf(
            Incorrect('a'),
            Incorrect('b'),
            Incorrect('c'),
            Incorrect('d'),
            WrongPosition('e')
        )))
    }

    @Test
    fun `should be able to check the state of each letter where a letter appears more times in the guess than the solution`() {
        val problem = Problem("elbow")
        assertThat(result(problem, "green").check(), equalTo(listOf(
            Incorrect('g'),
            Incorrect('r'),
            WrongPosition('e'),
            Incorrect('e'),
            Incorrect('n')
        )))
    }

    @Test
    fun `should be able to check the state of each letter where a correctly guessed letter appears more times in the guess than the solution`() {
        val problem = Problem("abbed")
        assertThat(result(problem, "abcbb").check(), equalTo(listOf(
            Correct('a'),
            Correct('b'),
            Incorrect('c'),
            WrongPosition('b'),
            Incorrect('b')
        )))
    }

    private fun result(problem: Problem, guess: String) =
        problem.attempt(guess).second

    private fun count(problem: Problem, guess: String) =
        problem.attempt(guess).first
}
