package com.example.videorrgb.model

import com.daasuu.gpuv.egl.filter.GlRGBFilter

object RGBFilter{

    fun red() = GlRGBFilter().apply {
        setRed(1f)
        setGreen(0f)
        setBlue(0f)
    }

    fun blue() = GlRGBFilter().apply {
        setRed(0f)
        setGreen(0f)
        setBlue(1f)
    }

    fun green() = GlRGBFilter().apply {
        setRed(0f)
        setGreen(1f)
        setBlue(0f)
    }

    fun clear() = GlRGBFilter().apply {
        setRed(1f)
        setGreen(1f)
        setBlue(1f)
    }
}
