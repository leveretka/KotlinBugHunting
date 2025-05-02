package org.nedz.bughunting.rules

import kotlin.test.Test
import kotlin.test.assertTrue
import org.nedz.bughunting.testing.TestFramework

class BadFunctionNameTest {

    @Test
    fun `test custom rule on file1`() {
        // Arrange
        val rule = BadNameFunctionNameRule()

        // Act & Assert
        val testFramework = TestFramework()
        val result = testFramework.checkFileWithComments("src/main/resources/package1/bad_function_name_test.kt", listOf(rule))

        // Print the result
        println(result.details)

        // Assert that the test passed
        assertTrue(result.success, "Test failed: ${result.details}")
    }

    @Test
    fun `test custom rule on file2`() {
        // Arrange
        val rule = BadNameFunctionNameRule2()

        // Act & Assert
        val testFramework = TestFramework()
        val result = testFramework.checkFileWithComments("src/main/resources/package1/bad_function_name_test2.kt", listOf(rule))

        // Print the result
        println(result.details)

        // Assert that the test passed
        assertTrue(result.success, "Test failed: ${result.details}")
    }
}
