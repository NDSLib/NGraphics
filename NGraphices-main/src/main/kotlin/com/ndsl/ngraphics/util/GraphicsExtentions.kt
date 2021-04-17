package com.ndsl.ngraphics.util

import java.awt.Color
import java.awt.Graphics

fun Graphics.setColor(color: Color, r:(Graphics)->Unit){
    this.color = color
    r(this)
}