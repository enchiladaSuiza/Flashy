package com.example.flashy.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ListItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)

        outRect.left = space
        outRect.right = space
        outRect.bottom = space
        outRect.top = 0

        if (position < 1) {
            outRect.top = space
        }
    }
}