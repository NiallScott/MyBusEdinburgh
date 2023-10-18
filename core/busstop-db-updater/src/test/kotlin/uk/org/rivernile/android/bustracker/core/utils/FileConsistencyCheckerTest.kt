/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.utils

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import java.io.File
import java.io.IOException

/**
 * Unit tests for [FileConsistencyChecker].
 *
 * @author Niall Scott
 */
class FileConsistencyCheckerTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var fileConsistencyChecker: FileConsistencyChecker

    @Before
    fun setUp() {
        fileConsistencyChecker = FileConsistencyChecker(coroutineRule.testDispatcher)
    }

    @Test(expected = IOException::class)
    fun throwsIoExceptionWhenFileDoesNotExistOrIsInaccessible() = runTest {
        val file = File("/path/does/not/exist")

        fileConsistencyChecker.checkFileMatchesHash(file, "abc123")
    }

    @Test
    fun correctlyCalculatesHashForEmptyFile() = runTest {
        val file = getFileForPath("/empty_file.txt")

        val result = fileConsistencyChecker.checkFileMatchesHash(file,
                "d41d8cd98f00b204e9800998ecf8427e")

        assertTrue(result)
    }

    @Test
    fun correctlyCalculatesHashForNonEmptyFile() = runTest {
        val file = getFileForPath("/non_empty_file.txt")

        val result = fileConsistencyChecker.checkFileMatchesHash(file,
                "f336a8073df3d4f06afbf281671715bf")

        assertTrue(result)
    }

    @Test
    fun returnsFalseWhenHashDoesNotMatchOnEmptyFile() = runTest {
        val file = getFileForPath("/empty_file.txt")

        val result = fileConsistencyChecker.checkFileMatchesHash(file, "abc123")

        assertFalse(result)
    }

    @Test
    fun returnsFalseWhenHashDoesNotMatchOnNonEmptyFile() = runTest {
        val file = getFileForPath("/non_empty_file.txt")

        val result = fileConsistencyChecker.checkFileMatchesHash(file, "abc123")

        assertFalse(result)
    }

    private fun getFileForPath(path: String) =
            javaClass.getResource(path)?.file?.let(::File) ?: throw UnsupportedOperationException()
}