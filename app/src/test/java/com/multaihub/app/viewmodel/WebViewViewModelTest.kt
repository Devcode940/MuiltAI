package com.multaihub.app.viewmodel

import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.repository.AiRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Regression tests for state consistency and persistence failure handling. */
@OptIn(ExperimentalCoroutinesApi::class)
class WebViewViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: AiRepository
    private lateinit var viewModel: WebViewViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mockk(relaxed = true)
        every { repository.getAllPrompts() } returns flowOf(emptyList())
        every { repository.getAllTabs() } returns flowOf(emptyList())
        viewModel = WebViewViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleDesktopModeUpdatesStateOnlyAfterPersistenceSucceeds() = runTest {
        val provider = AiProvider(
            id = "test",
            name = "Test AI",
            url = "https://example.com"
        )
        coEvery { repository.updateLastUsed("test") } returns Unit
        coEvery { repository.toggleDesktopMode("test", true) } returns Unit

        viewModel.setProvider(provider)
        advanceUntilIdle()
        viewModel.toggleDesktopMode()
        advanceUntilIdle()

        assertTrue(viewModel.currentProvider.value?.isDesktopMode == true)
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun toggleDesktopModeKeepsOldStateWhenPersistenceFails() = runTest {
        val provider = AiProvider(
            id = "test",
            name = "Test AI",
            url = "https://example.com"
        )
        coEvery { repository.updateLastUsed("test") } returns Unit
        coEvery { repository.toggleDesktopMode("test", true) } throws IllegalStateException("db")

        viewModel.setProvider(provider)
        advanceUntilIdle()
        viewModel.toggleDesktopMode()
        advanceUntilIdle()

        assertFalse(viewModel.currentProvider.value?.isDesktopMode == true)
        assertTrue(viewModel.error.value?.contains("display mode") == true)
    }
}
