package com.hxd.svc

import android.graphics.Path
import android.graphics.PointF
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * CreateTime: 2020/7/11  10:07
 * Author: hxd
 * Content: 辅助计算类
 * UpdateTime:
 * UpdateName;
 * UpdateContent:
 */
class SvcSizeUtils {
    companion object {
        fun drawPartCircle(start: PointF, end: PointF, path: Path, outer: Boolean) {

            //贝赛尔曲线系数
            val c = 0.551915024494f
            // 中点
            val middle = PointF(start.x + (end.x - start.x) / 2, start.y + (end.y - start.y) / 2)
            //半径
            // kotlin 内求平方根的方法表示为 sqrt（） 代替了 Java 内 Math.sqrt（）
            // kotlin 的 n次方 表示方式为 pow（n） Java 的 n 次方表示方式为  Math.pow(参数，n 次方)
            val r1 =
                    sqrt((((middle.x - start.x).toDouble()).pow(2) + (middle.y - start.y).pow(2))).toFloat()
            //gap值
            val gap = r1 * c

            if (start.x == end.x) {
                //绘制竖直方向的
                //是否是从上到下
                val flag = if (end.y - start.y > 0) 1 else -1
                if (outer) {
                    //凸的 两个半圆
                    path.cubicTo(start.x + gap * flag,
                            start.y,
                            middle.x + r1 * flag,
                            middle.y - gap * flag,
                            middle.x + r1 * flag,
                            middle.y)

                    path.cubicTo(middle.x + r1 * flag,
                            middle.y + gap * flag,
                            end.x + gap * flag,
                            end.y,
                            end.x,
                            end.y)
                } else {
                    //凹的 两个半圆
                    path.cubicTo(start.x - gap * flag,
                            start.y,
                            middle.x - r1 * flag,
                            middle.y - gap * flag,
                            middle.x - r1 * flag,
                            middle.y)

                    path.cubicTo(middle.x - r1 * flag,
                            middle.y + gap * flag,
                            end.x - gap * flag,
                            end.y,
                            end.x,
                            end.y)
                }

            } else {
                //绘制水平方向的
                //是否是从左到右
                val flag = if (end.x - start.x > 0) 1 else -1
                if (outer) {
                    //凸的 两个半圆
                    path.cubicTo(start.x,
                            start.y - gap * flag,
                            middle.x - gap * flag,
                            middle.y - r1 * flag,
                            middle.x,
                            middle.y + -r1 * flag)

                    path.cubicTo(middle.x + gap * flag,
                            middle.y - r1 * flag,
                            end.x,
                            end.y - gap * flag,
                            end.x,
                            end.y)
                } else {
                    //凹 两个半圆
                    path.cubicTo(start.x,
                            start.y + gap * flag,
                            middle.x - gap * flag,
                            middle.y + r1 * flag,
                            middle.x,
                            middle.y + r1 * flag)

                    path.cubicTo(middle.x + gap * flag,
                            middle.y + r1 * flag,
                            end.x,
                            end.y + gap * flag,
                            end.x,
                            end.y)
                }
            }
        }
    }
}