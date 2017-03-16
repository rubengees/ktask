package com.rubengees.ktask.sample.android

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.View

class MarginDecoration(private val marginDp: Int, private val columns: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val position = parent.getChildLayoutPosition(view)
        val marginPx = marginDp * (view.context.resources.displayMetrics.densityDpi.toFloat() /
                DisplayMetrics.DENSITY_DEFAULT).toInt()

        outRect.right = marginPx
        outRect.bottom = marginPx

        if (position < columns) {
            outRect.top = marginPx
        }

        if (position % columns == 0) {
            outRect.left = marginPx
        }
    }
}
