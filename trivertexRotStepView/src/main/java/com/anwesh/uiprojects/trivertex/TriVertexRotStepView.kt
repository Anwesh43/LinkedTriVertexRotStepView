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

fun Int.percentileNegate() : Float = 1f - 2 * (this % 2)

fun Canvas.drawTVRNode(i : Int, scale : Float, paint : Paint) {
    var w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val deg : Double = 2 * Math.PI * tris.getInverse()
    val size : Float = gap / sizeFactor
    val triR : Float = size / 2.2f
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val offsetX : Float = w/4 * i.percentileNegate()
    paint.color = color
    save()
    translate(w/2 + offsetX, gap * (i + 1))
    rotate(- 90f * sc2)
    for (j in 0..(tris - 1)) {
        val sc : Float = sc1.divideScale(j, tris)
        val cx : Float = size * Math.cos(deg * j).toFloat()
        val cy : Float = size * Math.sin(deg * j).toFloat()
        save()
        translate(cx, cy)
        rotate(90f * sc2 + 90f * sc)
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
    }
    restore()
}

class TriVertexRotStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scale.updateScale(dir, tris, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TVRNode(var i : Int, val state : State = State()) {

        private var prev : TVRNode? = null

        private var next : TVRNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = TVRNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTVRNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TVRNode {
            var curr : TVRNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class TriVertexRotStep(var i : Int) {

        private val root : TVRNode = TVRNode(0)

        private var curr : TVRNode = root

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TriVertexRotStepView) {

        private val tvrs : TriVertexRotStep = TriVertexRotStep(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            tvrs.draw(canvas, paint)
            animator.animate {
                tvrs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            tvrs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : TriVertexRotStepView {
            val view : TriVertexRotStepView = TriVertexRotStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}