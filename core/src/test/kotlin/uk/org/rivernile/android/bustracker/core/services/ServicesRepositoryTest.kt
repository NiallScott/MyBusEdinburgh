/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.ServicesDao
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

    private lateinit var repository: ServicesRepository

    @Before
    fun setUp() {
        repository = ServicesRepository(servicesDao, coroutineRule.testDispatcher)
    }

    @Test
    fun getColoursForServicesFlowGetsInitialValue() = coroutineRule.runBlockingTest {
        val serviceColours = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF)
        whenever(servicesDao.getColoursForServices(null))
                .thenReturn(serviceColours)

        val observer = repository.getColoursForServicesFlow(null).test(this)
        observer.finish()

        observer.assertValues(serviceColours)
        verify(servicesDao)
                .removeOnServicesChangedListener(any())
    }

    @Test
    fun getColoursForServicesFlowResponseToChanges() = coroutineRule.runBlockingTest {
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
        whenever(servicesDao.getColoursForServices(null))
                .thenReturn(serviceColours1, serviceColours2, serviceColours3)

        val observer = repository.getColoursForServicesFlow(null).test(this)
        observer.finish()

        observer.assertValues(serviceColours1, serviceColours2, serviceColours3)
        verify(servicesDao)
                .removeOnServicesChangedListener(any())
    }
}