package com.example.trial.adapter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.trial.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RecycleViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `viewModel should load expenses from repository`() = runTest {
        // Given
        val mockRepository = mock(ExpenseRepository::class.java)
        val expectedExpenses = listOf(
            com.example.trial.data.local.entities.ExpenseEntity(
                title = "Test",
                amount = 100.0,
                category = "Food",
                date = System.currentTimeMillis()
            )
        )

        `when`(mockRepository.allExpenses).thenReturn(flowOf(expectedExpenses))

        // When
        val viewModel = RecycleViewModel(mockRepository)

        // Then
        val actualExpenses = viewModel.allExpenses.value
        assert(actualExpenses == expectedExpenses)
    }
}