package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.Px
import androidx.core.widget.NestedScrollView
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.screenHeight

class StickyScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    @IdRes
    private val targetId: Int

    @Px
    private val threshold: Int
    private lateinit var stickyView: View
    private val screenH = screenHeight()
    private var stickyState: StickyState = StickyState.IDLE

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StickyScrollView,
            0, 0
        ).apply {
            try {
                targetId = getResourceId(R.styleable.StickyScrollView_stickyView, -1)
                threshold = getDimensionPixelSize(R.styleable.StickyScrollView_threshold, 0)
            } finally {
                recycle()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        stickyView = findViewById(targetId)
    }

    override fun onScrollChanged(l: Int, top: Int, oldl: Int, oldTop: Int) {
        super.onScrollChanged(l, top, oldl, oldTop)
        val isScrollDown = top - oldTop > 0
        val topEdge = threshold + top
        val bottomEdge = screenH - threshold + top
        Log.i("MYTAG", "bottomEdge $bottomEdge topEdge: $topEdge stickyviewTop ${stickyView.top} scrollDown: $isScrollDown")

        when {
            bottomEdge < stickyView.top -> stickyState = StickyState.IDLE
            isScrollDown && bottomEdge > stickyView.top -> stickyState = StickyState.TOP
            !isScrollDown && topEdge < stickyView.bottom -> stickyState = StickyState.BOTTOM
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            post { animateState() }
        }
        return super.onTouchEvent(ev)
    }

    override fun onStopNestedScroll(target: View) {
        super.onStopNestedScroll(target)
        post { animateState() }

    }

    override fun onStopNestedScroll(target: View, type: Int) {
        super.onStopNestedScroll(target, type)
    }

    override fun setOnScrollChangeListener(l: View.OnScrollChangeListener?) {
        super.setOnScrollChangeListener(l)
    }

    private fun animateState() {
        val y = when (stickyState) {
            StickyState.TOP -> stickyView.top
            StickyState.BOTTOM -> stickyView.top - screenH
            else -> return
        }

        smoothScrollTo(0, y)
    }

    private enum class StickyState {
        TOP, BOTTOM, IDLE
    }
}