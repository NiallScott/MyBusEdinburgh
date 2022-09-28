/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors or contributors be held liable for
 * any damages arising from the use of this software.
 *
 * The aforementioned copyright holder(s) hereby grant you a
 * non-transferrable right to use this software for any purpose (including
 * commercial applications), and to modify it and redistribute it, subject to
 * the following conditions:
 *
 *  1. This notice may not be removed or altered from any file it appears in.
 *
 *  2. Any modifications made to this software, except those defined in
 *     clause 3 of this agreement, must be released under this license, and
 *     the source code of any modifications must be made available on a
 *     publically accessible (and locateable) website, or sent to the
 *     original author of this software.
 *
 *  3. Software modifications that do not alter the functionality of the
 *     software but are simply adaptations to a specific environment are
 *     exempt from clause 2.
 *
 */

package uk.org.rivernile.android.bustracker.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopSearchResult
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.search.SearchHistoryRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [SearchActivityViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SearchActivityViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var featureRepository: FeatureRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var searchHistoryRepository: SearchHistoryRepository

    @Test
    fun isScanMenuItemVisibleEmitsFalseWhenNoCameraFeature() {
        whenever(featureRepository.hasCameraFeature)
                .thenReturn(false)
        val viewModel = createViewModel()

        val observer = viewModel.isScanMenuItemVisibleLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun isScanMenuItemVisibleEmitsTrueWhenHasCameraFeature() {
        whenever(featureRepository.hasCameraFeature)
                .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isScanMenuItemVisibleLiveData.test()

        observer.assertValues(true)
    }

    @Test
    fun onItemClickedShowsStopDetails() {
        val item = UiSearchResult("123456", null, 0, null)
        val viewModel = createViewModel()

        val observer = viewModel.showStopLiveData.test()
        viewModel.onItemClicked(item)

        observer.assertValues("123456")
    }

    @Test
    fun onScanMenuItemClickedShowsQrCodeScanner() {
        val viewModel = createViewModel()

        val observer = viewModel.showQrCodeScannerLiveData.test()
        viewModel.onScanMenuItemClicked()

        observer.assertSize(1)
    }

    @Test
    fun onQrScannerNotFoundShowsInstallQrScannerDialog() {
        val viewModel = createViewModel()

        val observer = viewModel.showInstallQrScannerDialogLiveData.test()
        viewModel.onQrScannerNotFound()

        observer.assertSize(1)
    }

    @Test
    fun onQrScannedWithErrorPerformsNoAction() {
        val viewModel = createViewModel()

        val showStopObserver = viewModel.showStopLiveData.test()
        val showInvalidQrCodeErrorObserver = viewModel.showInvalidQrCodeErrorLiveData.test()
        viewModel.onQrScanned(ScanQrCodeResult.Error)

        showStopObserver.assertEmpty()
        showInvalidQrCodeErrorObserver.assertEmpty()
    }

    @Test
    fun onQrScannedWithNullStopCodeShowsInvalidQrCodeError() {
        val viewModel = createViewModel()

        val showStopObserver = viewModel.showStopLiveData.test()
        val showInvalidQrCodeErrorObserver = viewModel.showInvalidQrCodeErrorLiveData.test()
        viewModel.onQrScanned(ScanQrCodeResult.Success(null))

        showStopObserver.assertEmpty()
        showInvalidQrCodeErrorObserver.assertSize(1)
    }

    @Test
    fun onQrScannedWithEmptyStopCodeShowsInvalidQrCodeError() {
        val viewModel = createViewModel()

        val showStopObserver = viewModel.showStopLiveData.test()
        val showInvalidQrCodeErrorObserver = viewModel.showInvalidQrCodeErrorLiveData.test()
        viewModel.onQrScanned(ScanQrCodeResult.Success(""))

        showStopObserver.assertEmpty()
        showInvalidQrCodeErrorObserver.assertSize(1)
    }

    @Test
    fun onQrScannedWithPopulatedStopCodeShowsStopDetails() {
        val viewModel = createViewModel()

        val showStopObserver = viewModel.showStopLiveData.test()
        val showInvalidQrCodeErrorObserver = viewModel.showInvalidQrCodeErrorLiveData.test()
        viewModel.onQrScanned(ScanQrCodeResult.Success("123456"))

        showStopObserver.assertValues("123456")
        showInvalidQrCodeErrorObserver.assertEmpty()
    }

    @Test
    fun uiStateFlowEmitsEmptySearchTermWhenSearchTermIsNull() = runTest {
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        val searchResultsObserver = viewModel.searchResultsLiveData.test()
        viewModel.searchTerm = null
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.EmptySearchTerm)
        searchResultsObserver.assertValues(null)
    }

    @Test
    fun uiStateFlowEmitsEmptySearchTermWhenSearchTermIsEmpty() = runTest {
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        val searchResultsObserver = viewModel.searchResultsLiveData.test()
        viewModel.searchTerm = ""
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.EmptySearchTerm)
        searchResultsObserver.assertValues(null)
    }

    @Test
    fun uiStateFlowEmitsEmptySearchTermWhenSearchTermIs1CharLength() = runTest {
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        val searchResultsObserver = viewModel.searchResultsLiveData.test()
        viewModel.searchTerm = "a"
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.EmptySearchTerm)
        searchResultsObserver.assertValues(null)
    }

    @Test
    fun uiStateFlowEmitsEmptySearchTermWhenSearchTermIs2CharLength() = runTest {
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        val searchResultsObserver = viewModel.searchResultsLiveData.test()
        viewModel.searchTerm = "ab"
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.EmptySearchTerm)
        searchResultsObserver.assertValues(null)
    }

    @Test
    fun uiStateFlowEmitsNoResultsWhenResultIsNull() = runTest {
        whenever(busStopsRepository.getStopSearchResultsFlow("abc"))
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        val searchResultsObserver = viewModel.searchResultsLiveData.test()
        viewModel.searchTerm = "abc"
        advanceUntilIdle()

        uiStateObserver.assertValues(
                UiState.InProgress,
                UiState.NoResults)
        searchResultsObserver.assertValues(null)
    }

    @Test
    fun uiStateFlowEmitsContentWhenResultIsNotNull() = runTest {
        whenever(busStopsRepository.getStopSearchResultsFlow("abc"))
                .thenReturn(flowOf(
                        listOf(
                                StopSearchResult(
                                        "123456",
                                        StopName(
                                                "Name 1",
                                                "Locality 1"),
                                        1,
                                        "1, 2, 3"))))
        val viewModel = createViewModel()
        val expectedResults = listOf(
                UiSearchResult(
                        "123456",
                        StopName(
                                "Name 1",
                                "Locality 1"),
                        1,
                        "1, 2, 3"))

        val uiStateObserver = viewModel.uiStateLiveData.test()
        val searchResultsObserver = viewModel.searchResultsLiveData.test()
        viewModel.searchTerm = "abc"
        advanceUntilIdle()

        uiStateObserver.assertValues(
                UiState.InProgress,
                UiState.Content(expectedResults))
        searchResultsObserver.assertValues(
                null,
                expectedResults)
    }

    @Test
    fun uiStateFlowEmitsContentUpdates() = runTest {
        val searchResult1 = StopSearchResult(
                "123456",
                StopName(
                        "Name 1",
                        "Locality 1"),
                1,
                "1, 2, 3")
        val searchResult2 = StopSearchResult(
                "987654",
                StopName(
                        "Name 2",
                        "Locality 2"),
                2,
                "4, 5, 6")
        whenever(busStopsRepository.getStopSearchResultsFlow("abc"))
                .thenReturn(flowOf(
                        listOf(searchResult1),
                        null,
                        listOf(searchResult1, searchResult2)))
        val viewModel = createViewModel()
        val expectedResult1 = UiSearchResult(
                "123456",
                StopName(
                        "Name 1",
                        "Locality 1"),
                1,
                "1, 2, 3")
        val expectedResult2 = UiSearchResult(
                "987654",
                StopName(
                        "Name 2",
                        "Locality 2"),
                2,
                "4, 5, 6")
        val expectedResults1 = listOf(expectedResult1)
        val expectedResults2 = listOf(expectedResult1, expectedResult2)

        val uiStateObserver = viewModel.uiStateLiveData.test()
        val searchResultsObserver = viewModel.searchResultsLiveData.test()
        viewModel.searchTerm = "abc"
        advanceUntilIdle()

        uiStateObserver.assertValues(
                UiState.InProgress,
                UiState.Content(expectedResults1),
                UiState.NoResults,
                UiState.Content(expectedResults2))
        searchResultsObserver.assertValues(
                null,
                expectedResults1,
                null,
                expectedResults2)
    }

    @Test
    fun uiStateFlowBehavesCorrectlyWhenSearchTermChanges() = runTest {
        val searchResult1 = StopSearchResult(
                "123456",
                StopName(
                        "Name 1",
                        "Locality 1"),
                1,
                "1, 2, 3")
        val searchResult2 = StopSearchResult(
                "987654",
                StopName(
                        "Name 2",
                        "Locality 2"),
                2,
                "4, 5, 6")
        whenever(busStopsRepository.getStopSearchResultsFlow("abc"))
                .thenReturn(flowOf(listOf(searchResult1)))
        whenever(busStopsRepository.getStopSearchResultsFlow("def"))
                .thenReturn(flowOf(listOf(searchResult2)))
        val viewModel = createViewModel()
        val expectedResult1 = UiSearchResult(
                "123456",
                StopName(
                        "Name 1",
                        "Locality 1"),
                1,
                "1, 2, 3")
        val expectedResult2 = UiSearchResult(
                "987654",
                StopName(
                        "Name 2",
                        "Locality 2"),
                2,
                "4, 5, 6")
        val expectedResults1 = listOf(expectedResult1)
        val expectedResults2 = listOf(expectedResult2)

        val uiStateObserver = viewModel.uiStateLiveData.test()
        val searchResultsObserver = viewModel.searchResultsLiveData.test()
        viewModel.searchTerm = "abc"
        advanceUntilIdle()
        viewModel.searchTerm = "def"
        advanceUntilIdle()

        uiStateObserver.assertValues(
                UiState.InProgress,
                UiState.Content(expectedResults1),
                UiState.InProgress,
                UiState.Content(expectedResults2))
        searchResultsObserver.assertValues(
                null,
                expectedResults1,
                null,
                expectedResults2)
    }

    @Test
    fun submitSearchTermDoesNotAddToSearchHistoryWhenTermIsNull() = runTest {
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        viewModel.submitSearchTerm(null)
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.EmptySearchTerm)
        verify(searchHistoryRepository, never())
                .addSearchTerm(anyOrNull())
    }

    @Test
    fun submitSearchTermDoesNotAddToSearchHistoryWhenTermIsEmpty() = runTest {
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        viewModel.submitSearchTerm("")
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.EmptySearchTerm)
        verify(searchHistoryRepository, never())
                .addSearchTerm(anyOrNull())
    }

    @Test
    fun submitSearchTermDoesNotAddToSearchHistoryWhenTermIs1Char() = runTest {
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        viewModel.submitSearchTerm("a")
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.EmptySearchTerm)
        verify(searchHistoryRepository, never())
                .addSearchTerm(anyOrNull())
    }

    @Test
    fun submitSearchTermDoesNotAddToSearchHistoryWhenTermIs2Chars() = runTest {
        val viewModel = createViewModel()

        val uiStateObserver = viewModel.uiStateLiveData.test()
        viewModel.submitSearchTerm("ab")
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.EmptySearchTerm)
        verify(searchHistoryRepository, never())
                .addSearchTerm(anyOrNull())
    }

    @Test
    fun submitSearchTermAddsToSearchHistoryWhenTermIsAtLeast3Chars() = runTest {
        val viewModel = createViewModel()
        whenever(busStopsRepository.getStopSearchResultsFlow("abc"))
                .thenReturn(flowOf(null))

        val uiStateObserver = viewModel.uiStateLiveData.test()
        viewModel.submitSearchTerm("abc")
        advanceUntilIdle()

        uiStateObserver.assertValues(
                UiState.InProgress,
                UiState.NoResults)
        verify(searchHistoryRepository)
                .addSearchTerm("abc")
    }

    private fun createViewModel() = SearchActivityViewModel(
            featureRepository,
            busStopsRepository,
            searchHistoryRepository,
            coroutineRule.testDispatcher,
            coroutineRule.scope)
}