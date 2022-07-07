package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import ru.skillbranch.skillarticles.ui.custom.behaviors.SubmenuBehavior
import kotlin.math.hypot

class ArticleSubmenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) , CoordinatorLayout.AttachedBehavior {
    //settings
    @Px private val menuWidth = context.dpToIntPx(200)
    @Px private val menuHeight = context.dpToIntPx(96)
    @Px private val btnHeight = context.dpToIntPx(40)
    @Px private val btnWidth = menuWidth / 2
    @Px private val defaultPadding = context.dpToIntPx(16)
    @ColorInt private val lineColor: Int = context.getColor(R.color.color_divider)
    @ColorInt private val textColor = context.attrValue(R.attr.colorOnSurface)
    private val iconTint = context.getColorStateList(R.color.tint_color)
    @DrawableRes private val bg = context.attrValue(android.R.attr.selectableItemBackground, needRes = true)

    //views
    val btnTextDown = createCheckableImageView(false)
    val btnTextUp = createCheckableImageView(true)
    val switchMode = SwitchMaterial(context)
    val tvLabel = TextView(context).apply {
        setTextColor(textColor)
        text = "Темный режим"
    }

    var isOpen = false

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        strokeWidth = 0f
    }

    init {
        addView(btnTextDown)
        addView(btnTextUp)
        addView(switchMode)
        addView(tvLabel)

        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<ArticleSubmenu> {
        return SubmenuBehavior()
    }
    fun open() {
        if (isOpen || !isAttachedToWindow) return
        isOpen = true
        animatedShow()
    }

    fun close() {
        if (!isOpen || !isAttachedToWindow) return
        isOpen = false
        animatedHide()
    }

    private fun animatedShow() {
        val endRadius = hypot(menuWidth.toFloat(), menuHeight.toFloat()).toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
            this,
            menuWidth,
            menuHeight,
            0f,
            endRadius.toFloat()
        )
        anim.doOnStart {
            visibility = View.VISIBLE
        }
        anim.start()
    }

    private fun animatedHide() {
        val endRadius = hypot(menuWidth.toFloat(), menuHeight.toFloat()).toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
            this,
            menuWidth,
            menuHeight,
            endRadius.toFloat(),
            0f
        )
        anim.doOnEnd {
            visibility = View.GONE
        }
        anim.start()
    }

   //save state
    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.ssIsOpen = isOpen
        return savedState
    }

    //restore state
    override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            isOpen = state.ssIsOpen
            isVisible = isOpen
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(tvLabel, widthMeasureSpec, heightMeasureSpec)
        measureChild(switchMode, widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(menuWidth, menuHeight)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
        btnTextDown.layout(
            paddingLeft,
            paddingTop,
            paddingLeft + btnWidth,
            paddingTop + btnHeight
        )
        btnTextUp.layout(
            paddingLeft + btnWidth,
            paddingTop,
            paddingLeft + btnWidth * 2,
            paddingTop + btnHeight
        )
        val tvLabelTop = btnHeight + (menuHeight - btnHeight - tvLabel.measuredHeight) / 2
        tvLabel.layout(
            paddingLeft + defaultPadding,
            tvLabelTop,
            defaultPadding + paddingLeft + tvLabel.measuredWidth,
            tvLabelTop + tvLabel.measuredHeight
        )
        val switchModeTop = btnHeight + (menuHeight - btnHeight - switchMode.measuredHeight) / 2
        switchMode.layout(
            menuWidth - switchMode.measuredWidth - defaultPadding,
            switchModeTop,
            menuWidth - defaultPadding,
            switchModeTop + switchMode.measuredHeight
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(
            menuWidth / 2f,
            0f,
            menuWidth / 2f,
            btnHeight.toFloat(),
            linePaint
        )
        canvas.drawLine(
            0f,
            btnHeight.toFloat(),
            menuWidth.toFloat(),
            btnHeight.toFloat(),
            linePaint
        )
    }

    private fun createCheckableImageView(isBigger: Boolean) = CheckableImageView(context).apply {
        imageTintList = iconTint
        setImageResource(R.drawable.ic_title_black_24dp)
        setBackgroundResource(bg)
        val padding = context.dpToIntPx(if (isBigger) 8 else 12)
        setPaddingOptionally(top = padding, bottom = padding)
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsOpen: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            ssIsOpen = src.readInt() == 1
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeInt(if (ssIsOpen) 1 else 0)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

}