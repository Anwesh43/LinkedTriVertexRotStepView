package com.anwesh.uiprojects.trivertex

/**
 * Created by anweshmishra on 23/11/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path

val nodes : Int = 5
val tris : Int = 3
val color : Int = Color.parseColor("#673AB7")
val sizeFactor : Int = 3
val scDiv : Double = 0.51
val scGap : Float = 0.05f

fun Int.getInverse() : Float = 1f / this

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(this - n.getInverse() * i, 0f)) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.getInverse() + (scaleFactor()) * b.getInverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = dir * scGap * mirrorValue(a, b)

fun Canvas.drawTVRNode(i : Int, scale : Float, paint : Paint) {
    var w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val deg : Float = 360f * tris.getInverse()
    val size : Float = gap / sizeFactor
    val triR : Float = size / 8
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = color
    save()
    translate(gap * (i + 1), h/2)
    rotate(- 90f * sc2)
    for (j in 0..(tris - 1)) {
        val sc : Float = sc1.divideScale(j, tris)
        save()
        rotate( deg * j)
        translate(size, 0f)
        save()
        rotate(90f * sc)
        val path : Path = Path()
        for (k in 0..(tris-1)) {
            val d : Double = (2 * Math.PI / tris)
            val x : Float = triR * Math.cos(k * d).toFloat()
            val y : Float = triR * Math.sin(k * d).toFloat()
            if (k == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(path, paint)
        restore()
        restore()
    }
    restore()
}

class TriVertexRotStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}