/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.services

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.ServicesDao
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.ServiceDetails
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ServicesRepository].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ServicesRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var servicesDao: ServicesDao
    @Mock
    private lateinit var serviceColourOverride: ServiceColourOverride

    @Test
    fun getColoursForServicesFlowGetsInitialValueWhenServiceColourOverrideIsNull() = runTest {
        val serviceColours = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF)
        whenever(servicesDao.getColoursForServices(arrayOf("1", "2")))
                .thenReturn(serviceColours)
        val repository = ServicesRepository(servicesDao, null)

        val observer = repository.getColoursForServicesFlow(arrayOf("1", "2")).test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(serviceColours)
        verify(servicesDao)
                .removeOnServicesChangedListener(any())
    }

    @Test
    fun getColoursForServicesFlowGetsInitialValueWhenServiceColourOverrideIsNotNull() = runTest {
        val initialServiceColours = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF)
        val overriddenServiceColours = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF,
                "3" to 0x0000FF)
        whenever(servicesDao.getColoursForServices(arrayOf("1", "2", "3")))
                .thenReturn(initialServiceColours)
        whenever(serviceColourOverride.overrideServiceColours(arrayOf("1", "2", "3"),
                initialServiceColours))
                .thenReturn(overriddenServiceColours)
        val repository = ServicesRepository(
                servicesDao,
                serviceColourOverride)

        val observer = repository.getColoursForServicesFlow(arrayOf("1", "2", "3")).test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(overriddenServiceColours)
        verify(servicesDao)
                .removeOnServicesChangedListener(any())
    }

    @Test
    fun getColoursForServicesFlowResponseToChangesWhenServiceColourOverrideIsNull() = runTest {
        doAnswer {
            it.getArgument<ServicesDao.OnServicesChangedListener>(0).let { listener ->
                listener.onServicesChanged()
                listener.onServicesChanged()
            }
        }.whenever(servicesDao).addOnServicesChangedListener(any())
        val serviceColours1 = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF)
        val serviceColours2 = mapOf(
                "3" to 0xFF0000,
                "4" to 0x00FF00)
        val serviceColours3 = mapOf(
                "5" to 0x0000FF)
        whenever(servicesDao.getColoursForServices(arrayOf("1", "2", "3", "4", "5")))
                .thenReturn(serviceColours1, serviceColours2, serviceColours3)
        val repository = ServicesRepository(
                servicesDao,
                null)

        val observer = repository.getColoursForServicesFlow(arrayOf("1", "2", "3", "4", "5"))
                .test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(serviceColours1, serviceColours2, serviceColours3)
        verify(servicesDao)
                .removeOnServicesChangedListener(any())
    }

    @Test
    fun getColoursForServicesFlowResponseToChangesWhenServiceColourOverrideIsNotNull() = runTest {
        doAnswer {
            it.getArgument<ServicesDao.OnServicesChangedListener>(0).let { listener ->
                listener.onServicesChanged()
                listener.onServicesChanged()
            }
        }.whenever(servicesDao).addOnServicesChangedListener(any())
        val daoServiceColours1 = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF)
        val daoServiceColours2 = mapOf(
                "3" to 0xFF0000,
                "4" to 0x00FF00)
        val overriddenServiceColours2 = mapOf(
                "1" to 0x000002,
                "2" to 0xFFFFF2,
                "3" to 0xFF0000,
                "4" to 0x00FF00)
        val daoServiceColours3 = mapOf(
                "5" to 0x0000FF)
        whenever(servicesDao.getColoursForServices(arrayOf("1", "2", "3", "4", "5")))
                .thenReturn(daoServiceColours1, daoServiceColours2, daoServiceColours3)
        whenever(serviceColourOverride.overrideServiceColours(arrayOf("1", "2", "3", "4", "5"),
                daoServiceColours1))
                .thenReturn(daoServiceColours1)
        whenever(serviceColourOverride.overrideServiceColours(arrayOf("1", "2", "3", "4", "5"),
                daoServiceColours2))
                .thenReturn(overriddenServiceColours2)
        whenever(serviceColourOverride.overrideServiceColours(arrayOf("1", "2", "3", "4", "5"),
                daoServiceColours3))
                .thenReturn(null)
        val repository = ServicesRepository(
                servicesDao,
                serviceColourOverride)

        val observer = repository.getColoursForServicesFlow(arrayOf("1", "2", "3", "4", "5"))
                .test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(daoServiceColours1, overriddenServiceColours2, null)
        verify(servicesDao)
                .removeOnServicesChangedListener(any())
    }

    @Test
    fun getServiceDetailsFlowGetsInitialValue() = runTest {
        val expected = mapOf(
                "1" to ServiceDetails("1", "Route 1", 1),
                "2" to ServiceDetails("2", "Route 2", 2),
                "3" to ServiceDetails("3", "Route 3", 3))
        whenever(servicesDao.getServiceDetails(setOf("1", "2", "3")))
                .thenReturn(expected)
        val repository = ServicesRepository(servicesDao, null)

        val observer = repository.getServiceDetailsFlow(setOf("1", "2", "3")).test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected)
        verify(servicesDao)
                .removeOnServicesChangedListener(any())
    }

    @Test
    fun getServiceDetailsFlowRespondsToServiceChanges() = runTest {
        doAnswer {
            val listener = it.getArgument<ServicesDao.OnServicesChangedListener>(0)
            listener.onServicesChanged()
            listener.onServicesChanged()
        }.whenever(servicesDao).addOnServicesChangedListener(any())
        val expected1 = mapOf(
                "1" to ServiceDetails("1", "Route 1", 1),
                "2" to ServiceDetails("2", "Route 2", 2),
                "3" to ServiceDetails("3", "Route 3", 3))
        val expected3 = mapOf(
                "1" to ServiceDetails("1", "Route 1", 1),
                "3" to ServiceDetails("3", "Route 3", 3))
        whenever(servicesDao.getServiceDetails(setOf("1", "2", "3")))
                .thenReturn(expected1, null, expected3)
        val repository = ServicesRepository(servicesDao, null)

        val observer = repository.getServiceDetailsFlow(setOf("1", "2", "3")).test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected1, null, expected3)
        verify(servicesDao)
                .removeOnServicesChangedListener(any())
    }

    @Test
    fun allServiceNameFlowGetsFlowFromDao() {
        val repository = ServicesRepository(servicesDao, null)
        val mockFlow = mock<Flow<List<String>?>>()
        whenever(servicesDao.allServiceNamesFlow)
                .thenReturn(mockFlow)

        val result = repository.allServiceNamesFlow

        assertSame(mockFlow, result)
    }
}