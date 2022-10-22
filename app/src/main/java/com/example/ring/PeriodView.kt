package com.example.ring

import android.R.attr
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * Copyright (c) 2022, Lollitech
 * All rights reserved
 * author: funaihui@lollitech.com
 * describe:
 **/
class PeriodView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 白点的半径
    private val pointRadius = 2f.dp

    // 内部的圆形
    private val innerCircleRadius = 125f.dp
    private val centerX by lazy { (width / 2).toFloat() }
    private val centerY by lazy { (height / 2).toFloat() }
    private var oldAngle = 0.0
    private var realAngle //圆圈的角度，绘制圆弧和圆圈的标准，0～360
            = 0.0
    private var curAngle //当前手指滑动对应的位置角度
            = 0.0
    private var downAngle = 0.0 //手指按下时对应的角度

    private var downRealAngle = 0.0 //手指按下时，realAngle的值
    private val pointNum = 60
    private var clickIndex = 0

    // 画圆弧矩形的半径
    private val arcRectRadius by lazy { (bgBitmap.height - 54.dp) / 2 }


    // TODO: 符乃辉 2022/10/20  画圆弧
    // 指示器的阴影补偿
    private val indicatorShadowCompensate = 3.5f.dp

    // 背景的bitmap
    private val bgBitmap by lazy {
        context.createBitmapFromLocal(
            ScreenUtils.getDisplayScreenWidth(context),
            ScreenUtils.getDisplayScreenWidth(context),
            R.drawable.home_new_pic_ring_bg
        )
    }

    private val indicatorBitmap by lazy {
        context.createBitmapFromLocal(158.dp, 170.dp, R.drawable.home_btn_selected_bg)
    }

    private val srcIndicatorRect by lazy { Rect(0, 0, indicatorBitmap.width, indicatorBitmap.height) }
    private val destIndicatorRectF by lazy {
        RectF(
            centerX - indicatorBitmap.width / 2,
            centerY - innerCircleRadius - indicatorBitmap.height + indicatorShadowCompensate,
            centerX + indicatorBitmap.width / 2,
            centerY - innerCircleRadius + indicatorShadowCompensate
        )
    }

    private val periodRect by lazy {
        RectF(
            centerX - arcRectRadius, centerY - arcRectRadius, centerX + arcRectRadius, centerY + arcRectRadius
        )
    }

    private val periodStrokeOutRect by lazy {
        val outDistance = (bgBitmap.height - 20.dp) / 2
        RectF(
            centerX - outDistance, centerY - outDistance, centerX + outDistance, centerY + outDistance
        )
    }

    private val periodStrokeInnerRect by lazy {
        val outDistance = (bgBitmap.height - 86.dp) / 2

        RectF(
            centerX - outDistance, centerY - outDistance, centerX + outDistance, centerY + outDistance
        )
    }

    private val periodPathOut by lazy { Path() }
    private val periodPathInner by lazy { Path() }

    private val canvasMatrix by lazy { Matrix() }
    private val pointsSrc by lazy { floatArrayOf(0f, 0f, width.toFloat() / 2, 0f, width.toFloat(), 0f) }

    private val pointsDst by lazy {
        floatArrayOf(
            0f, (height / 8).toFloat(), width.toFloat() / 2,
            0f, width.toFloat(), (height / 2).toFloat()
        )
    }


    private val arcFillPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F7599C")
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeWidth = 34f.dp
        }
    }

    private val periodPaintOut by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F7599C")
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeWidth = 1f.dp
        }
    }

    private val periodPaintInner by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F7599C")
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeWidth = 32f.dp
        }
    }

    private val rectF by lazy {
        val pointXY = getPointXY(0.0)
        val radius = 16.5f.dp
        RectF(
            pointXY.x - 10 * radius,
            centerY - arcRectRadius - radius,
            pointXY.x + 10 * radius,
            centerY - arcRectRadius + radius
        )
    }


    // 背景起始矩形
    private val srcBgRect by lazy { Rect(0, 0, bgBitmap.width, bgBitmap.height) }

    // 背景放置的矩形
    private val destBgRect by lazy {
        val left = ((width - bgBitmap.width) / 2).toFloat()
        val top = ((height - bgBitmap.height) / 2).toFloat()
        RectF(left, top, bgBitmap.width.toFloat() + left, top + bgBitmap.height.toFloat())
    }

    private val bgPaint by lazy { Paint() }
    private val innerCirclePaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
    }

    private val clickPaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f.dp
            color = Color.RED
        }
    }

    private val pointPaint by lazy {
        Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }



    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawBgBitmap(canvas)
        drawInnerCircle(canvas)
        drawPregnancyFillArc(canvas)
        drawPregnancyStrokeArc(canvas)
        drawSmallPoint(canvas)
//        drawClickRing(canvas)
        drawIndicator(canvas)
    }

    private fun drawPregnancyStrokeArc(canvas: Canvas) {
//        canvas.save()
//        canvas.rotate(30f,centerX,centerY)
//        val startAngle = 6f * 5
//        val sweetAngle = 3f * 10
//        val realStartAngel = 90 + startAngle
//        val realEndAngel = 90.0 + startAngle + sweetAngle
//        periodPathOut.addArc(periodStrokeOutRect, startAngle, sweetAngle)
//        canvas.drawPath(periodPathOut, periodPaintOut)
//        val endPointXY = getPointXY(realEndAngel)
//        periodPathOut.addArc(periodStrokeInnerRect, startAngle, sweetAngle)
//        val radius = 16.5f.dp
//        periodPathOut.addArc(
//            (endPointXY.x-radius),
//            (endPointXY.y-3.65*radius).toFloat(),
//            (endPointXY.x+radius),
//            (endPointXY.y-1.65*radius).toFloat(),
//            90f,
//            180f
//        )
//
//        canvas.drawPath(periodPathOut, periodPaintOut)
//        canvas.restore()

//        val startAngle = 6f * 5
//        val sweetAngle = 3f * 10
//        val realStartAngel = 90 + startAngle
//        val realEndAngel = 90.0 + startAngle + sweetAngle
//        periodPathOut.addArc(periodRect, startAngle, sweetAngle)
//        canvas.drawPath(periodPathOut, periodPaintOut)
//        val endPointXY = getPointXY(0.0)
//        periodPathOut.addArc(periodStrokeInnerRect, startAngle, sweetAngle)
//        periodPathOut.fillType = Path.FillType.WINDING;

        val radius = 16.5f.dp
//        periodPathOut.addArc(
//            (endPointXY.x-radius),
//            (endPointXY.y-3.65*radius).toFloat(),
//            (endPointXY.x+radius),
//            (endPointXY.y-1.65*radius).toFloat(),
//            90f,
//            180f
//        )

        canvasMatrix.reset()
        val polyToPoly = canvasMatrix.setPolyToPoly(pointsSrc, 0, pointsDst, 0, 3)
        canvas.save()

        canvas.concat(canvasMatrix)
        canvas.drawRoundRect(rectF, radius, radius, periodPaintOut)
        canvas.restore()


//        canvas.drawPath(periodPathOut, periodPaintOut)
    }

    //孕期弧形
    private fun drawPregnancyFillArc(canvas: Canvas) {
        canvas.drawArc(periodRect, 0f, 6f * 5, false, arcFillPaint)
    }

    private fun drawIndicator(canvas: Canvas) {
        canvas.save()
        canvas.rotate(6f * clickIndex, centerX, centerY)
        canvas.drawBitmap(indicatorBitmap, srcIndicatorRect, destIndicatorRectF, bgPaint)
        canvas.restore()

    }

    private fun drawClickRing(canvas: Canvas) {
        val pointXY = getPointXY(6.0 * clickIndex)
        val radius = if (pointXY.x > centerX) {
            (pointXY.x - centerX) / 2
        } else {
            (centerX - pointXY.x) / 2
        }


        canvas.drawCircle(centerX, centerY - (bgBitmap.height - 48.dp) / 2, radius, clickPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var distance = 0f// 手指位置与圆点之间的距离
        var downX = 0f
        var downY = 0f//手指按下的位置坐标
        var pressX = 0f
        var pressY = 0f//手指当前的位置坐标
        var deltaAngle = 0.0//相对转动的角度

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                // 点击的位置距离圆心的距离
                distance = (downX - centerX) * (downX - centerX) + (downY - centerY) * (downY - centerY)
                // 确保点击的位置在圆环内
                if (distance < (bgBitmap.width / 2) * (bgBitmap.width / 2) && distance > (innerCircleRadius * innerCircleRadius)) {
                    downAngle = location2Angle(downX, downY)
                    // 判断角度在哪个点的范围
                    clickIndex = (downAngle / 6).roundToInt()
                    Log.d("fnh", "角度属于位置：$clickIndex")
                    invalidate()
                    downRealAngle = realAngle
                    Log.d("fnh", "onTouchEvent: " + downAngle + "downX: " + downX + "downY: " + downY)
                } else {
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // TODO: 符乃辉 2022/10/20  有时间再优化
//                pressX = event.x
//                pressY = event.y
//                // 点击的位置距离圆心的距离
//                distance = (pressX - centerX) * (pressX - centerX) + (pressY - centerY) * (pressY - centerY)
//                // 确保点击的位置在圆环内
//                if (distance < (bgBitmap.width / 2) * (bgBitmap.width / 2) && distance > (innerCircleRadius * innerCircleRadius)) {
//                    curAngle = location2Angle(pressX, pressY)
//                    oldAngle = realAngle
//                    deltaAngle = curAngle - downAngle
//                    realAngle = (downRealAngle + deltaAngle) % 360
//                    realAngle = if (realAngle < 0) realAngle + 360 else realAngle
//                    val minute = realAngle.toInt() / 6
//                    Log.d(
//                        "fnh",
//                        "realAngle: " + realAngle + " curAngle: " + curAngle + "downAngle: " + downAngle + " deltaAngle: " + deltaAngle
//                    )
//                }
            }
            MotionEvent.ACTION_UP -> {}
        }
        return true
    }

    private fun location2Angle(pressX: Float, pressY: Float): Double {
        var angle: Double
        if (pressX < centerX) {
            if (pressY < centerY) {     // 左上区域
                angle = atan(((centerY - pressY) / (centerX - pressX)).toDouble()) //得到弧度制角度
                angle = angle * 180 / Math.PI
                angle += 270 //换算成角度制
            } else {                     // 左下区域
                angle = atan(((pressY - centerY) / (centerX - pressX)).toDouble()) //得到弧度制角度
                angle = angle * 180 / Math.PI
                angle = 270 - angle //换算成角度制
            }
        } else {
            if (pressY < centerY) {      // 右上区域
                angle = atan(((centerY - pressY) / (pressX - centerX)).toDouble()) //得到弧度制角度
                angle = angle * 180 / Math.PI
                angle = 90 - angle //换算成角度制
            } else {                    // 右下区域
                angle = atan(((pressY - centerY) / (pressX - centerX)).toDouble()) //得到弧度制角度
                angle = angle * 180 / Math.PI
                angle += 90 //换算成角度制
            }
        }
        return angle
    }


    /**
     * 计算白色原点的坐标
     */
    private fun getPointXY(realAngle: Double): Point {
        val radius = centerY - (bgBitmap.height - 48.dp) / 2

        val point = Point()
        if (realAngle >= 270) {     // 左上区域
            point.x = (centerX - (cos(angle2Radians(realAngle - 270)) * radius)).toInt()
            point.y = (centerY - (sin(angle2Radians(realAngle - 270)) * radius)).toInt()
        } else if (realAngle >= 180) {                     // 左下区域
            point.x = (centerX - (cos(angle2Radians(270 - realAngle)) * radius)).toInt()
            point.y = (centerY + (sin(angle2Radians(270 - realAngle)) * radius)).toInt()
        } else if (realAngle >= 90) {      //  右下区域
            point.x = (centerX + (cos(angle2Radians(realAngle - 90)) * radius)).toInt()
            point.y = (centerY + (sin(angle2Radians(realAngle - 90)) * radius)).toInt()
        } else {                    // 右上区域
            point.x = (centerX + (cos(angle2Radians(90 - realAngle)) * radius)).toInt()
            point.y = (centerY - (sin(angle2Radians(90 - realAngle)) * radius)).toInt()
        }
        Log.d(
            "fnh",
            "realAngle: " + angle2Radians(realAngle).toString() + "ballMoveX: " + point.x.toString() + "ballMoveY: " + point.x
        )
        return point
    }

    // 两点之间的距离
    private fun getPerPointDistance(): Pair<Int, Int> {

        val perAngle = 360.0 / pointNum
        val point1 = getPointXY(0.0)
        val point2 = getPointXY(perAngle)

        val distanceX = abs(point1.x - point2.x)
        val distanceY = abs(point1.y - point2.y)
        return Pair(distanceX, distanceY)
    }


    /**
     * 角度制转弧度制
     */
    private fun angle2Radians(angle: Double): Double {
        return angle / 180 * Math.PI
    }

    private fun drawInnerCircle(canvas: Canvas) {

        canvas.drawCircle(centerX, centerY, innerCircleRadius, innerCirclePaint)
    }

    private fun drawSmallPoint(canvas: Canvas) {
        val centerX = (width / 2).toFloat()
        val centerY = (height / 2).toFloat()
        canvas.save()
        for (index in 0 until 60) {
            canvas.rotate(6f, centerX, centerY)
            canvas.drawCircle(
                centerX,
                centerY - arcRectRadius,
                pointRadius,
                pointPaint
            )
        }
        canvas.restore()
    }

    private fun drawBgBitmap(canvas: Canvas) {
        canvas.drawBitmap(bgBitmap, srcBgRect, destBgRect, bgPaint)
    }
}