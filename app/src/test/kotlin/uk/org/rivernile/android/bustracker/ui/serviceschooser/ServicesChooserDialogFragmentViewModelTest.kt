/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for [ServicesChooserDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
class ServicesChooserDialogFragmentViewModelTest {

    companion object {

        private const val STATE_SERVICES = "services"
        private const val STATE_SELECTED_SERVICES = "selectedServices"
    }

    @Test
    fun setServicesSetsServices() {
        val services = arrayOf("1", "2", "3")
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_SERVICES to services)))

        assertArrayEquals(services, viewModel.services)
    }

    @Test
    fun getSelectedServicesReturnsNullByDefault() {
        val viewModel = createViewModel()

        val result = viewModel.selectedServices

        assertNull(result)
    }

    @Test
    fun getSelectedServicesReturnsFromSavedStateByDefault() {
        val services = arrayOf("1", "2", "3")
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_SERVICES to services)))

        val result = viewModel.selectedServices

        assertArrayEquals(services, result)
    }

    @Test
    fun setSelectedServicesSetsServices() {
        val services = arrayOf("1", "2", "3")
        val viewModel = createViewModel()

        viewModel.selectedServices = services

        assertArrayEquals(services, viewModel.selectedServices)
    }

    @Test
    fun checkBoxesIsNullByDefault() {
        val viewModel = createViewModel()

        assertNull(viewModel.checkBoxes)
    }

    @Test
    fun checkBoxesIsNullWhenServicesIsEmpty() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_SERVICES to emptyArray<String>())))

        assertNull(viewModel.checkBoxes)
    }

    @Test
    fun checkBoxesHasArrayOfFalseByDefaultWhenServicesExist() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_SERVICES to arrayOf("1", "2", "3"))))
        val expected = booleanArrayOf(false, false, false)

        val result = viewModel.checkBoxes

        assertArrayEquals(expected, result)
    }

    @Test
    fun checkBoxesHasPopulatedArrayWhenServicesExistAndSelectedServicesInSavedState() {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(
                            STATE_SERVICES to arrayOf("1", "2", "3", "4", "5"),
                            STATE_SELECTED_SERVICES to arrayOf("2", "4"))))
        val expected = booleanArrayOf(false, true, false, true, false)

        val result = viewModel.checkBoxes

        assertArrayEquals(expected, result)
    }

    @Test
    fun checkBoxesHasPopulatedArrayWhenServicesExistAndSelectedServices() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_SERVICES to arrayOf("1", "2", "3", "4", "5"))))
        val expected = booleanArrayOf(false, true, false, true, false)

        viewModel.selectedServices = arrayOf("2", "4")
        val result = viewModel.checkBoxes

        assertArrayEquals(expected, result)
    }

    @Test
    fun onItemClickedWithNoServicesDoesNotMakeAnyChanges() {
        val viewModel = createViewModel()

        viewModel.onItemClicked(2, true)

        assertNull(viewModel.selectedServices)
    }

    @Test
    fun onItemClickedAndIsCheckedAddsSelectedService() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_SERVICES to arrayOf("1", "2", "3", "4", "5"))))
        val expected = arrayOf("3")

        viewModel.onItemClicked(2, true)

        assertArrayEquals(expected, viewModel.selectedServices)
    }

    @Test
    fun onItemClickedAndIsCheckedOnlyAddsServiceOnce() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_SERVICES to arrayOf("1", "2", "3", "4", "5"))))
        val expected = arrayOf("3")

        viewModel.onItemClicked(2, true)
        viewModel.onItemClicked(2, true)

        assertArrayEquals(expected, viewModel.selectedServices)
    }

    @Test
    fun onItemClickedWithMultipleServices() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_SERVICES to arrayOf("1", "2", "3", "4", "5"))))
        val expected = arrayOf("2", "4")

        viewModel.onItemClicked(1, true)
        viewModel.onItemClicked(3, true)

        assertArrayEquals(expected, viewModel.selectedServices)
    }

    @Test
    fun onItemClickedAndIsNotCheckedRemovesService() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_SERVICES to arrayOf("1", "2", "3", "4", "5"))))
        val expected = arrayOf("4")

        viewModel.onItemClicked(1, true)
        viewModel.onItemClicked(3, true)
        viewModel.onItemClicked(1, false)

        assertArrayEquals(expected, viewModel.selectedServices)
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
            ServicesChooserDialogFragmentViewModel(savedState)
}