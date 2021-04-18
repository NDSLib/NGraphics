package com.ndsl.ngraphics.pos

import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.nextInt

class Pos(val x: Int, val y: Int) {
    companion object {
        fun randomRange(xStart: Int, xRange: Int, yStart: Int, yRange: Int): Pos {
            return Pos(Random.nextInt(xRange) + xStart, Random.nextInt(yRange) + yStart)
        }

        fun random(xMin: Int, xMax: Int, yMin: Int, yMax: Int): Pos {
            return randomRange(xMin, xMax - xMin, yMin, yMax - yMin)
        }
    }

    fun distance(other: Pos): Double {
        return sqrt(((other.x - x) * (other.x - x) + (other.y - y) * (other.y - y)).toDouble())
    }
}