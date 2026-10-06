package com.example

import com.example.data.api.AstraOfflineSolver
import com.example.ui.components.parseMarkdownBlocks
import com.example.ui.components.MarkdownBlock
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testOfflineSolverReturnsRelevantAdvice() {
        val routineAdvice = AstraOfflineSolver.getOfflineSolution("morning routine")
        assertTrue(routineAdvice.contains("Morning Routine"))

        val budgetAdvice = AstraOfflineSolver.getOfflineSolution("manage my budget and money")
        assertTrue(budgetAdvice.contains("50/30/20"))

        val pythonAdvice = AstraOfflineSolver.getOfflineSolution("write python code")
        assertTrue(pythonAdvice.contains("Python"))
    }

    @Test
    fun testMarkdownCodeBlockParsing() {
        val raw = "Here is some code:\n```python\nprint('hello world')\n```\nDone."
        val blocks = parseMarkdownBlocks(raw)
        val codeBlock = blocks.filterIsInstance<MarkdownBlock.Code>().firstOrNull()
        assertNotNull(codeBlock)
        assertEquals("python", codeBlock?.language)
        assertEquals("print('hello world')", codeBlock?.content)
    }
}
