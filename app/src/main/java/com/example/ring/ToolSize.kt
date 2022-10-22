@file: JvmName("ToolSize")

package com.example.ring

/**
 * Copyright (c) 2022, Lollitech
 * All rights reserved
 * author: funaihui@lollitech.com
 * describe:
 **/

import android.content.Context
import android.content.res.Resources
import androidx.annotation.DimenRes


/**
 * 将px值转换为dp值
 *
 * @param pxValue px值
 * @return 转化后的dp值
 */
fun px2dp(pxValue: Float): Float {
    val scale: Float = Resources.getSystem().displayMetrics.density
    return pxValue / scale + 0.5f
}


/**
 * 将px值转换为sp值
 *
 * @param pxValue px值
 * @return 转化后的sp值
 */
fun px2sp(pxValue: Float): Float {
    val scale = Resources.getSystem().displayMetrics.scaledDensity
    return pxValue / scale + 0.5f
}

/**
 * 将sp值转换为px值
 *
 * @param spValue sp值
 * @return 转化后的px值
 */
fun sp2px(spValue: Float): Float {
    val scale = Resources.getSystem().displayMetrics.scaledDensity
    return spValue * scale + 0.5f
}

/**
 * 将dp值转换为px值
 * 通过数字加dp的方式使用，如10.dp
 */
inline val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * 将dp值转换为px值
 * 通过数字加dp的方式使用，如10.dp
 */
inline val Float.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

/**
 * 从资源文件id获取像素值
 *
 * @param context context
 * @param id      dimen文件id  R.dimen.resourceId
 * @return R.dimen.resourceId对应的尺寸具体的像素值
 */
fun getDimension(context: Context, @DimenRes id: Int): Float = context.resources.getDimension(id)
