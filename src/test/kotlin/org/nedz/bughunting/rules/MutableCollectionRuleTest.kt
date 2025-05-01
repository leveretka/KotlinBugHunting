package org.nedz.bughunting.rules

import org.nedz.bughunting.testing.TestFramework
import kotlin.test.Test
import kotlin.test.assertTrue

class MutableCollectionRuleTest {

    @Test
    fun `test mutable collections with explicit types`() {
        // Arrange
        val rule = MutableCollectionRule1()

        // Act & Assert
        val testFramework = TestFramework()
        val result = testFramework.checkFileWithComments("src/main/resources/package1/collections_test1.kt", listOf(rule))

        // Print the result
        println(result.details)

        // Assert that the test passed
        assertTrue(result.success, "Test failed: ${result.details}")
    }

    @Test
    fun `test mutable collections with mutating operations detection`() {
        // Arrange
        val rule = MutableCollectionRule2()

        // Act & Assert
        val testFramework = TestFramework()
        val result = testFramework.checkFileWithComments("src/main/resources/package1/collections_test1.kt", listOf(rule))

        // Print the result
        println(result.details)

        // Assert that the test passed
        assertTrue(result.success, "Test failed: ${result.details}")
    }

    @Test
    fun `test mutable collections with function parameter detection`() {
        // Arrange
        val rule = MutableCollectionRule3()

        // Act & Assert
        val testFramework = TestFramework()
        val result = testFramework.checkFileWithComments("src/main/resources/package1/collections_test3.kt", listOf(rule))

        // Print the result
        println(result.details)

        // Assert that the test passed
        assertTrue(result.success, "Test failed: ${result.details}")
    }
}
