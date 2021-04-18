package com.ndsl.ngraphics.pos

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Rect(var left_up: Pos, var right_down: Pos) {
    companion object {
        fun randomRange(xStart: Int, xRange: Int, yStart: Int, yRange: Int): Rect {
            val one = Pos.randomRange(xStart, xRange, yStart, yRange)
            val two = Pos.randomRange(xStart, xRange, yStart, yRange)
            return ToRect(one, two)
        }

        fun random(xMin: Int, xMax: Int, yMin: Int, yMax: Int): Rect {
            return randomRange(xMin, xMax - xMin, yMin, yMax - yMin)
        }
    }

    constructor(left_x: Int, left_y: Int, right_x: Int, right_y: Int) : this(Pos(left_x, left_y), Pos(right_x, right_y))

    fun getWidth() = abs(right_down.x - left_up.x)
    fun getHeight() = abs(right_down.y - left_up.y)

    fun center(): Pos {
        return Pos((left_up.x + right_down.x) / 2, (left_up.y + right_down.y) / 2)
    }

    /**
     * 大きさ変えずに移動
     */
    fun set(x: Int, y: Int) {
        val r = Rect(left_up, right_down)
        left_up = Pos(r.left_up.x + x, r.left_up.y + y)
        right_down = Pos(left_up.x + r.getWidth(), left_up.y + r.getHeight())
    }

    fun set(pos: Pos) {
        set(pos.x, pos.y)
    }

    fun clone() = Rect(left_up, right_down)
}

fun ToRect(from: Pos, to: Pos): Rect = Rect(min(from.x, to.x), min(from.y, to.y), max(from.x, to.x), max(from.y, to.y))