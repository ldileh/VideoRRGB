package com.example.videorrgb.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class PortraitFrameLayout: FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val width = measuredWidth
//        setMeasuredDimension(width, width / 9 * 16)
//    }
}