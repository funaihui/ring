package com.example.ring

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.WindowManager


/**
 * Copyright (c) 2022, Bongmi
 * All rights reserved
 * Author: yaoxiawen@lollitech.com
 */
object ScreenUtils {
    private var realStatusBarHeight: Int = 0

    fun getDisplayScreenWidth(context: Context?): Int {
        if (context == null) {
            return 0
        }
        val metric = DisplayMetrics()
        val windowManager = context
            .applicationContext.getSystemService(
                Context.WINDOW_SERVICE
            ) as WindowManager
        windowManager.defaultDisplay.getMetrics(metric)
        return metric.widthPixels
    }

    fun getDisplayScreenHeight(context: Context?): Int {
        if (context == null) {
            return 0
        }
        val metric = DisplayMetrics()
        val windowManager = context
            .applicationContext.getSystemService(
                Context.WINDOW_SERVICE
            ) as WindowManager
        windowManager.defaultDisplay.getMetrics(metric)
        return metric.heightPixels
    }

    /**
     * 获取状态栏高度.
     *
     * @param context
     * @return
     */
    fun getStatusBarHeight(context: Context): Int {
        //获取status_bar_height资源的ID
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return if (resourceId <= 0) {
            16.dp
        } else context.resources.getDimensionPixelSize(resourceId)
        //根据资源ID获取响应的尺寸值
    }

    fun getActivityStatusBarHeight(activity: Activity, callback: (Int) -> Unit) {
        //google pixel 4a手机在Android 12 系统下，使用status_bar_height属性获取出状态栏高度28dp，但是依然有额外的空间
        //因此使用VisibleDisplayFrame获取
        if (realStatusBarHeight > 0) {
            callback.invoke(realStatusBarHeight)
            return
        }

        val decoreView = activity.window.getDecorView()
        val getHeight = {
            val rectgle = Rect()
            decoreView.getWindowVisibleDisplayFrame(rectgle)
            realStatusBarHeight = rectgle.top
            realStatusBarHeight
        }
        if (decoreView.isAttachedToWindow) {
            callback.invoke(getHeight())
        } else {
            decoreView.post {
                callback.invoke(getHeight())
            }
        }

    }
}