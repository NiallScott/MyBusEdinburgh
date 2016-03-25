/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * This class is used by {@link com.squareup.picasso.Picasso} to transform {@link Bitmap}s from
 * being square images to rounded images.
 *
 * <p>
 *     The solution was obtained on Stack Overflow
 *     <a href="http://stackoverflow.com/questions/26112150/android-create-circular-image-with-picasso">here</a>.
 *     Some modifications have been made.
 * </p>
 *
 * @author Niall Scott
 */
public class CircleTransform implements Transformation {

    @Override
    public Bitmap transform(final Bitmap source) {
        final int sourceWidth = source.getWidth();
        final int sourceHeight = source.getHeight();
        final int size = Math.min(sourceWidth, sourceHeight);
        final int x = (sourceWidth - size) / 2;
        final int y = (sourceHeight - size) / 2;
        final Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        final Bitmap.Config sourceConfig = source.getConfig();

        if (squaredBitmap != source) {
            source.recycle();
        }

        final Bitmap bitmap = Bitmap.createBitmap(size, size, sourceConfig);
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        final BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP,
                BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        final float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        squaredBitmap.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}