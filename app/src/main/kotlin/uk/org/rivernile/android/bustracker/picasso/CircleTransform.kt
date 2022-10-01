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

package uk.org.rivernile.android.bustracker.picasso

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import com.squareup.picasso.Transformation
import javax.inject.Inject
import kotlin.math.min

/**
 * This class is used by [com.squareup.picasso.Picasso] to transform [Bitmap]s from being square
 * images to rounded images.
 *
 * The solution was obtained on Stack Overflow
 * https://stackoverflow.com/questions/26112150/android-create-circular-image-with-picasso
 * Some modifications have been made.
 *
 * @author Niall Scott
 */
class CircleTransform @Inject constructor() : Transformation {

    override fun transform(source: Bitmap): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height
        val size = min(sourceWidth, sourceHeight)
        val x = (sourceWidth - size) / 2
        val y = (sourceHeight - size) / 2
        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)

        if (squaredBitmap !== source) {
            source.recycle()
        }

        val paint = Paint().apply {
            shader = BitmapShader(
                    squaredBitmap,
                    Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP)
            isAntiAlias = true
        }

        val radius = size / 2f
        val bitmap = Bitmap.createBitmap(size, size, source.config)
        Canvas(bitmap).drawCircle(radius, radius, radius, paint)
        squaredBitmap.recycle()

        return bitmap
    }

    override fun key() = "circle"
}