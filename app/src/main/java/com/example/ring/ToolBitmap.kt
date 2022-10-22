@file:JvmName("ToolBitmap")
package com.example.ring

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.roundToInt

/**
 * Copyright (c) 2022, Lollitech
 * All rights reserved
 * author: funaihui@lollitech.com
 * describe: bitmap的工具类
 **/
fun Context.createBitmapFromLocal(reqWidth: Int, reqHeight: Int, resId: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true//只读取图片，不加载到内存中
    BitmapFactory.decodeResource(resources, resId, options)
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeResource(resources, resId, options)
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val heightRatio = (height / reqHeight).toDouble().roundToInt()
        val widthRatio = (width / reqWidth).toDouble().roundToInt()
        inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
    }
    return inSampleSize
}