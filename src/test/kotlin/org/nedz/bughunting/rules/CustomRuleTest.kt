package org.nedz.bughunting.rules

import kotlin.test.Test
import kotlin.test.assertTrue
import org.nedz.bughunting.testing.TestFramework
import java.io.File

class CustomRuleTest {

    @Test
    fun `test custom rule on file1`() {
        // Arrange
        val rule = CustomRule()
        val testFile = File("src/main/resources/package1/file1.kt")

        // Act & Assert
        val testFramework = TestFramework()
        val result = testFramework.checkFileWithComments("src/main/resources/package1/file1.kt", listOf(rule))

        // Print the result
        println(result.details)

        // Assert that the test passed
        assertTrue(result.success, "Test failed: ${result.details}")
    }
}
