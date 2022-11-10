package com.example.flashy.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)
        if (position % 2 == 0) { // Lado izquierdo
            outRect.left = space
            outRect.right = space / 2
        } else { // Lado derecho
            outRect.left = space / 2
            outRect.right = space
        }

        outRect.bottom = space

        if (position <= 1) {
            outRect.top = space
        } else {
            outRect.top = 0
        }
    }
}