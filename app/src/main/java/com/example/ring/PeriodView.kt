package com.example.ring

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.addListener
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

    //圆圈的角度，绘制圆弧和圆圈的标准，0～360
    private var realAngle = 0.0

    //当前手指滑动对应的位置角度
    private var curAngle = 0.0

    //手指按下时对应的角度
    private var downAngle = 0.0

    //手指按下时，realAngle的值
    private var downRealAngle = 0.0
    private val pointNum = 60
    private var clickIndex = 0

    // 画圆弧矩形的半径
    private val arcRectRadius by lazy { (bgBitmap.height - 54.dp) / 2 }

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

    private val ovulationBitmap by lazy {
        context.createBitmapFromLocal(22.dp, 22.dp, R.drawable.ic_ovulation)
    }

    private val srcOvuRect by lazy {
        Rect(0, 0, ovulationBitmap.width, ovulationBitmap.height)
    }

    private val destOvuRect by lazy {
        RectF(
            centerX - ovulationBitmap.width / 2,
            centerY - arcRectRadius - ovulationBitmap.height / 2,
            centerX + ovulationBitmap.width / 2,
            centerY - arcRectRadius + ovulationBitmap.height / 2,
        )
    }

    // 上次点击的位置，动画需要
    private var oldClickIndex = 0

    private val indicatorBitmap by lazy {
        context.createBitmapFromLocal(158.dp, 170.dp, R.drawable.home_btn_selected_bg)
    }

    private val srcIndicatorRect by lazy {
        Rect(0, 0, indicatorBitmap.width, indicatorBitmap.height)
    }

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
            centerX - arcRectRadius, centerY - arcRectRadius,
            centerX + arcRectRadius, centerY + arcRectRadius
        )
    }

    private val periodStrokeOutRect by lazy {
        val outDistance = (bgBitmap.height - 20.dp) / 2
        RectF(
            centerX - outDistance, centerY - outDistance,
            centerX + outDistance, centerY + outDistance
        )
    }

    private val periodStrokeInnerRect by lazy {
        val innerDistance = (bgBitmap.height - 86.dp) / 2
        RectF(
            centerX - innerDistance, centerY - innerDistance,
            centerX + innerDistance, centerY + innerDistance
        )
    }

    // 周期环的path
    private val periodStrokePath by lazy { Path() }

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

    private val periodStrokePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F7599C")
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeWidth = 1f.dp
        }
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
            isAntiAlias = true
        }
    }

    private val pointPaint by lazy {
        Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }

    // indicator上面的文字
    private val topTextPaint by lazy {
        Paint().apply {
            color = Color.parseColor("#802d2d2d")
            textSize = topTextSize
            isAntiAlias = true
            strokeWidth = 1f.dp
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            // TODO: 设置字体
//        typeface = ResourcesCompat.getFont(context)
        }
    }

    private val topFontMetrics by lazy { topTextPaint.fontMetrics }
    private val bottomFontMetrics by lazy { bottomTextPaint.fontMetrics }

    // indicator下面的文字
    private val bottomTextPaint by lazy {
        Paint().apply {

            textSize = bottomTextSize
            isAntiAlias = true
            strokeWidth = 1f.dp
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            // TODO: 设置字体
//        typeface = ResourcesCompat.getFont(context)
        }
    }

    private val topTextSize = sp2px(12f)

    private val bottomTextSize = sp2px(14f)


    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawBgBitmap(canvas)
        drawInnerCircle(canvas)
        drawMenstruationFillArc(canvas)
        drawPregnancyFillArc(canvas)

        // 预测易孕期
//        drawPregnancyStrokeArc(canvas)
        drawSmallPoint(canvas)
//        drawClickRing(canvas)

        drawOvulationBitmap(canvas, 25)
        drawIndicator(canvas)
    }


    /**
     * 画经期的文字
     */
    private fun drawMenstruationFillText(canvas: Canvas, startIndex: Int, endIndex: Int) {
        drawMenstruationFillText(canvas, startIndex)
        drawMenstruationFillText(canvas, endIndex)
    }

    /**
     * 画孕期的文字
     */
    private fun drawPregnancyFillText(canvas: Canvas, startIndex: Int, endIndex: Int) {
        drawPregnancyFillText(canvas, startIndex)
        drawPregnancyFillText(canvas, endIndex)
    }

    private fun drawPregnancyFillText(canvas: Canvas, index: Int) {
        if (getOvulationIndex() != index) {
            topTextPaint.color = Color.parseColor("#B3FFFFFF")
            drawTopText(canvas, index)
            bottomTextPaint.color = Color.parseColor("#FFFFFF")
            bottomTextPaint.textSize = topTextSize
            drawBottomText(canvas, index)
        }
    }

    private fun drawMenstruationFillText(canvas: Canvas, index: Int) {
        topTextPaint.color = Color.parseColor("#B3FFFFFF")
        drawTopText(canvas, index)
        bottomTextPaint.color = Color.parseColor("#FFFFFF")
        bottomTextPaint.textSize = topTextSize
        drawBottomText(canvas, index)
    }

    private fun getPregnancyStartIndex() = 15
    private fun getMenstruationStartIndex() = 0
    private fun getPregnancyEndIndex() = 25
    private fun getMenstruationEndIndex() = 5
    private fun getOvulationIndex() = 25

    // 排卵期
    private fun drawOvulationBitmap(canvas: Canvas, index: Int) {
        canvas.save()
        canvas.rotate(360f/pointNum * index, centerX, centerY)
        canvas.drawBitmap(ovulationBitmap, srcOvuRect, destOvuRect, bgPaint)
        canvas.restore()
    }

    // 经期
    private fun drawMenstruationFillArc(canvas: Canvas) {
        arcFillPaint.color = Color.parseColor("#F7599C")
        canvas.drawArc(periodRect, -90f, 360f/pointNum  * 5, false, arcFillPaint)
        drawMenstruationFillText(canvas, 0, 5)
    }

    private fun drawBottomText(canvas: Canvas, index: Int) {
        val pointXY = getPointXY(360f/pointNum  * index)
        val baseLineY = pointXY.second - bottomFontMetrics.top / 1.4f
        canvas.drawText("16", pointXY.first, baseLineY, bottomTextPaint)
    }

    private fun drawTopText(canvas: Canvas, index: Int) {
        val pointXY = getPointXY(360f/pointNum  * index)
        val baseLineY = pointXY.second - topFontMetrics.bottom
        canvas.drawText("Fri", pointXY.first, baseLineY, topTextPaint)
    }

    //预测易孕期
    private fun drawPregnancyStrokeArc(canvas: Canvas) {
        val startAngle = 360f/pointNum  * 20
        val sweetAngle = 3f * 10
        val realStartAngel = 90f + startAngle
        val realEndAngel = 90f + startAngle + sweetAngle
        periodStrokePath.addArc(periodStrokeOutRect, startAngle, sweetAngle)
        val endPointXY = getPointXY(realEndAngel)
        val startPointXY = getPointXY(realStartAngel)
        periodStrokePath.addArc(periodStrokeInnerRect, startAngle, sweetAngle)
        // 直径 = 外环半径-内环半径
        val radius = 33f.dp / 2
        // 结束时的半环
        periodStrokePath.addArc(
            (endPointXY.first - radius),
            (endPointXY.second - radius),
            (endPointXY.first + radius),
            (endPointXY.second + radius),
            startAngle + sweetAngle,
            180f
        )

        // 开始时的半环
        periodStrokePath.addArc(
            (startPointXY.first - radius),
            (startPointXY.second - radius),
            (startPointXY.first + radius),
            (startPointXY.second + radius),
            90 + realStartAngel,
            180f
        )

        canvas.drawPath(periodStrokePath, periodStrokePaint)
    }

    //易孕期孕期弧形
    private fun drawPregnancyFillArc(canvas: Canvas) {
        arcFillPaint.color = Color.parseColor("#77D0DD")
        canvas.drawArc(periodRect, 0f, 360f/pointNum  * 10, false, arcFillPaint)
        // 圆弧填充的首尾文字
        drawPregnancyFillText(canvas, 15, 25)
    }

    private fun drawIndicator(canvas: Canvas) {
        canvas.save()
        canvas.rotate(360f/pointNum  * clickIndex, centerX, centerY)
        canvas.drawBitmap(indicatorBitmap, srcIndicatorRect, destIndicatorRectF, bgPaint)
        canvas.restore()

        topTextPaint.color = Color.parseColor("#802d2d2d")
        drawTopText(canvas, clickIndex)
        bottomTextPaint.color = Color.parseColor("#F7599C")
        bottomTextPaint.textSize = bottomTextSize
        drawBottomText(canvas, clickIndex)
    }

    private fun drawClickRing(canvas: Canvas) {
        val radius = 33f.dp / 2
        canvas.save()
        canvas.rotate(360f/pointNum  * clickIndex, centerX, centerY)
        canvas.drawCircle(centerX, centerY - arcRectRadius, radius, clickPaint)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val distance: Float// 手指位置与圆点之间的距离
        val downX: Float
        val downY: Float//手指按下的位置坐标
//        var pressX = 0f
//        var pressY = 0f//手指当前的位置坐标
//        var deltaAngle = 0.0//相对转动的角度

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                // 点击的位置距离圆心的距离
                distance =
                    (downX - centerX) * (downX - centerX) + (downY - centerY) * (downY - centerY)
                // 确保点击的位置在圆环内
                if (distance < (bgBitmap.width / 2) * (bgBitmap.width / 2) && distance > (innerCircleRadius * innerCircleRadius)) {
                    downAngle = location2Angle(downX, downY)
                    // 判断角度在哪个点的范围
                    clickIndex = (downAngle / 6).roundToInt()
                    Log.d("fnh", "角度属于位置：$clickIndex")
                    startIndicatorAnimator()
                    downRealAngle = realAngle
                    Log.d(
                        "fnh",
                        "onTouchEvent: " + downAngle + "downX: " + downX + "downY: " + downY
                    )
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
    private fun getPointXY(realAngle: Float): Pair<Float, Float> {
        val radius = centerY - arcRectRadius + indicatorShadowCompensate / 2

        return if (realAngle >= 270) {     // 左上区域
            Pair(
                centerX - (cos(angle2Radians(realAngle - 270)) * radius),
                centerY - (sin(angle2Radians(realAngle - 270)) * radius)
            )
        } else if (realAngle >= 180) {
            // 左下区域
            Pair(
                centerX - (cos(angle2Radians(270 - realAngle)) * radius),
                centerY + (sin(angle2Radians(270 - realAngle)) * radius)
            )
        } else if (realAngle >= 90) {      //  右下区域
            Pair(
                centerX + (cos(angle2Radians(realAngle - 90)) * radius),
                centerY + (sin(angle2Radians(realAngle - 90)) * radius)
            )
        } else {                    // 右上区域
            Pair(
                centerX + (cos(angle2Radians(90 - realAngle)) * radius),
                centerY - (sin(angle2Radians(90 - realAngle)) * radius)
            )
        }
//        Log.d(
//            "fnh",
//            "realAngle: " + angle2Radians(realAngle).toString() + "ballMoveX: " + point.x.toString() + "ballMoveY: " + point.x
//        )
    }

    // 两点之间的距离
    private fun getPerPointDistance(): Pair<Float, Float> {

        val perAngle = 360f / pointNum
        val point1 = getPointXY(0f)
        val point2 = getPointXY(perAngle)

        val distanceX = abs(point1.first - point2.first)
        val distanceY = abs(point1.second - point2.second)
        return Pair(distanceX, distanceY)
    }


    /**
     * 角度制转弧度制
     */
    private fun angle2Radians(angle: Float): Float {
        return (angle / 180 * Math.PI).toFloat()
    }

    private fun drawInnerCircle(canvas: Canvas) {

        canvas.drawCircle(centerX, centerY, innerCircleRadius, innerCirclePaint)
    }

    private fun drawSmallPoint(canvas: Canvas) {
        val centerX = (width / 2).toFloat()
        val centerY = (height / 2).toFloat()
        canvas.save()
        for (index in 0 until pointNum) {
            // 第一个点不用旋转画布
            if (index != 0) {
                canvas.rotate(360f / pointNum, centerX, centerY)
            }

            if (isDrawSmallPoint(index)) {
                canvas.drawCircle(
                    centerX,
                    centerY - arcRectRadius,
                    pointRadius,
                    pointPaint
                )
            }
        }
        canvas.restore()
    }

    /**
     * 判断是否画白色圆点
     * @return true:画白色圆点
     */
    private fun isDrawSmallPoint(index: Int): Boolean {

        return index != processIndex(getPregnancyStartIndex())
                && index != processIndex(getPregnancyStartIndex() - 1)
                && index != processIndex(getPregnancyEndIndex())
                && index != processIndex(getPregnancyEndIndex() + 1)
                && index != processIndex(getMenstruationStartIndex())
                && index != processIndex(getMenstruationStartIndex() - 1)
                && index != processIndex(getMenstruationEndIndex())
                && index != processIndex(getMenstruationEndIndex() + 1)
    }

    private fun processIndex(index: Int): Int {
        var afterIndex = index
        if (index < 0) {
            afterIndex = pointNum - 1
        }
        return afterIndex
    }

    private fun drawBgBitmap(canvas: Canvas) {
        canvas.drawBitmap(bgBitmap, srcBgRect, destBgRect, bgPaint)
    }


    private fun startIndicatorAnimator() {
        // 倒回去
        if (clickIndex < oldClickIndex) {

        }
        val valueAnimation = ValueAnimator.ofInt(oldClickIndex, clickIndex)
        valueAnimation.addUpdateListener {
            clickIndex = it.animatedValue as Int
            invalidate()
        }
        valueAnimation.addListener(onStart = {
            oldClickIndex = clickIndex
        })
        valueAnimation.duration = 300
        valueAnimation.start()
    }
}