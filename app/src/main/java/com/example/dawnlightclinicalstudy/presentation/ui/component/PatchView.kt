package com.example.dawnlightclinicalstudy.presentation.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.View

class PatchView(context: Context) : View(context) {
    var graph1: EcgGraph = EcgGraph(Color.GREEN)
    var graph2: EcgGraph = EcgGraph(Color.BLUE)
    var graph3: EcgGraph = EcgGraph(Color.YELLOW)
    var paint: Paint = Paint()

    // Display Realtime data
    fun updateData(ecg0: ArrayList<Int>, ecg1: ArrayList<Int>, resp1: ArrayList<Int>) {
        graph1.addPoints(ecg0, width)
        graph2.addPoints(ecg1, width)
        graph3.addPoints(resp1, width)

        // Note; This example app ignores the respiration data..
        Handler(Looper.getMainLooper()).post { invalidate() }
    }

    fun updateEcgData1(ecg1: ArrayList<Int>) {
        graph2.addPoints(ecg1, width)

        // Note; This example app ignores the respiration data..
        Handler(Looper.getMainLooper()).post { invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height
        // Draw waveforms around 1/3rd and 2/3rd of the canvas height
        val cx: Int = graph2.draw(canvas, w, h, h * 1 / 3)
        graph1.draw(canvas, w, h, h * 2 / 3)
        canvas.drawLine(cx.toFloat(), 0f, cx.toFloat(), h.toFloat(), paint)
        //graph3.draw(canvas, w, h, h * 1 / 4);
        //canvas.drawLine(cx, 0, cx, h, paint);
    }

    init {
        paint.color = Color.RED
    }
}

class EcgGraph constructor(graphColor: Int) {
    var currentLine: ArrayList<Int?> = ArrayList()
    var prevLine: ArrayList<Int?> = ArrayList()
    var paint: Paint = Paint()
    fun addPoints(points: ArrayList<Int>, graphWidth: Int) {
        synchronized(this) {
            for (i in points.indices) {
                if (currentLine.size >= graphWidth) {  // Wrap back
                    prevLine.clear()
                    for (p in currentLine) prevLine.add(p)
                    currentLine.clear()
                }
                currentLine.add(points[i])
            }
        }
    }

    fun draw(canvas: Canvas, width: Int, height: Int, center: Int): Int {
        var x: Int
        var currX: Int
        synchronized(this) {
            currX = currentLine.size
            x = 1
            while (x < currX) {
                if (currentLine[x - 1] != null && currentLine[x] != null) canvas.drawLine(
                    (x - 1).toFloat(),
                    (height - currentLine[x - 1]!! - center).toFloat(),
                    x.toFloat(),
                    (height - currentLine[x]!! - center).toFloat(),
                    paint
                )
                x++
            }
            x += 5
            while (x < width && x < prevLine.size) {
                if (prevLine[x - 1] != null && prevLine[x] != null) canvas.drawLine(
                    (x - 1).toFloat(),
                    (height - prevLine[x - 1]!! - center).toFloat(),
                    x.toFloat(),
                    (height - prevLine[x]!! - center).toFloat(),
                    paint
                )
                x++
            }
        }
        return currX
    }

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = graphColor
    }
}