package ru.skillbranch.skillarticles.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import kotlin.math.hypot

class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    //sizes
    @Px private val iconSize = context.dpToIntPx(56)
    @Px private val textWidth = context.dpToIntPx(134)
    @Px private val iconPadding = context.dpToIntPx(16)
    private val iconTint = context.getColorStateList(R.color.tint_color)

    //views
    val btnLike = createCheckableImageView(R.drawable.like_states)
    val btnBookmark: CheckableImageView = createCheckableImageView(R.drawable.bookmark_states)
    val btnShare = createShareButton()
    val btnSettings = createCheckableImageView(R.drawable.ic_format_size_black_24dp)

    private val searchBar = SearchBar().apply { isVisible = false }
    val tvSearchResult
        get() = searchBar.tvSearchResult
    val btnResultUp
        get() = searchBar.btnResultUp
    val btnResultDown
        get() = searchBar.btnResultDown
    val btnSearchClose
        get() = searchBar.btnSearchClose

    var isSearchMode = false

     override fun getBehavior(): CoordinatorLayout.Behavior<Bottombar> {
         return BottombarBehavior()
     }

    init {
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg

        addView(btnLike)
        addView(btnBookmark)
        addView(btnShare)
        addView(btnSettings)
        addView(searchBar)
    }

    private fun createShareButton(): AppCompatImageView {
        val image = AppCompatImageView(context)
        image.setImageResource(R.drawable.ic_share_black_24dp)
        image.imageTintList = iconTint
        image.setBackgroundResource(R.drawable.ripple)
        image.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
        return image
    }

    private fun createCheckableImageView(@DrawableRes icon: Int): CheckableImageView {
        val checkableImageView = CheckableImageView(context)
        checkableImageView.setImageResource(icon)
        checkableImageView.imageTintList = iconTint
        checkableImageView.setBackgroundResource(R.drawable.ripple)
        checkableImageView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
        return checkableImageView
    }

    override fun onSaveInstanceState(): Parcelable {
       val saveState = SavedState(super.onSaveInstanceState())
        saveState.ssIsSearchMode = isSearchMode
        return saveState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState){
            isSearchMode = state.ssIsSearchMode
            searchBar.isVisible = isSearchMode
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = iconSize

        searchBar.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
        setMeasuredDimension(width, height)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentX = paddingLeft
        btnLike.layout(
            currentX,
            0,
            currentX + iconSize,
            iconSize
        )
        currentX += iconSize
        btnBookmark.layout(
            currentX,
            0,
            currentX + iconSize,
            iconSize
        )
        currentX += iconSize
        btnShare.layout(
            currentX,
            0,
            currentX + iconSize,
            iconSize
        )
        currentX += iconSize
        val bodyWidth = r - l - paddingLeft - paddingRight
        val right = paddingLeft + bodyWidth
        btnSettings.layout(
            right - iconSize,
            0,
            right,
            iconSize
        )
        searchBar.layout(
            0,
            0,
            right,
            paddingTop + b - t
        )
    }

    fun setSearchState(isSearch: Boolean) {
        if (isSearchMode == isSearch || !isAttachedToWindow) return
        isSearchMode = isSearch
        if (isSearchMode) animateShowSearch() else animateHideSearch()
     }

    fun setSearchInfo(searchCount: Int = 0, position: Int = 0) {
        btnResultDown.isEnabled = searchCount > 0
        btnResultUp.isEnabled = searchCount > 0
        tvSearchResult.text = if (searchCount == 0) "Not found" else "${position.inc()} of $searchCount"

        when(position) {
            0 -> btnResultUp.isEnabled = false
            searchCount.dec() -> btnResultDown.isEnabled = false
        }
    }

    private fun animateShowSearch() {
        searchBar.isVisible = true

        val endRadius = hypot(width.toDouble(), height / 2.toDouble())

        val anim = ViewAnimationUtils.createCircularReveal(
            searchBar,
            width,
            height / 2,
            0f,
            endRadius.toFloat()
        )
        anim.start()
    }

    private fun animateHideSearch() {
        val endRadius = hypot(width.toDouble(), height / 2.toDouble())

        val anim = ViewAnimationUtils.createCircularReveal(
            searchBar,
            width,
            height / 2,
            endRadius.toFloat(),
            0f
        )
        anim.doOnEnd {
            searchBar.isVisible = false
        }
        anim.start()
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsSearchMode: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            ssIsSearchMode = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeByte(if (ssIsSearchMode) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }

    @SuppressLint("ViewConstructor")
    inner class SearchBar : ViewGroup(context, null, 0) {
        @ColorInt private val iconColor = context.attrValue(android.R.attr.colorPrimary)
        internal val btnSearchClose = createImage(R.drawable.ic_close_black_24dp)
        internal val tvSearchResult = TextView(context).apply {
            setTextColor(iconColor)
            gravity = Gravity.CENTER_VERTICAL
            text = "Not found"
            setPaddingOptionally(right = context.dpToIntPx(16))
        }
        internal val btnResultDown = createImage(R.drawable.ic_keyboard_arrow_down_black_24dp)
        internal val btnResultUp = createImage(R.drawable.ic_keyboard_arrow_up_black_24dp)

        init {
            setBackgroundColor(context.getColor(R.color.color_on_article_bar))
            addView(btnSearchClose)
            addView(tvSearchResult)
            addView(btnResultDown)
            addView(btnResultUp)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
            val height = iconSize
            // val tvWidthSpec = MeasureSpec.makeMeasureSpec(width - 3 * iconSize, MeasureSpec.EXACTLY)
            // val tvHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            // tvSearchResult.measure(
            //     tvWidthSpec,
            //     tvHeightSpec
            // )
            measureChild(tvSearchResult, widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(width, height)
        }

        override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
            var currentX = paddingLeft
            btnSearchClose.layout(
                currentX,
                0,
                currentX + iconSize,
                iconSize
            )
            val bodyWidth = r - l - paddingLeft - paddingRight
            val right = paddingLeft + bodyWidth
            currentX = right - 2 * iconSize
            val tvSearchResultTop = (iconSize - tvSearchResult.measuredHeight) / 2
            tvSearchResult.layout(
                btnSearchClose.right,
                tvSearchResultTop,
                btnSearchClose.right + tvSearchResult.measuredWidth,
                tvSearchResultTop + tvSearchResult.measuredHeight
            )

            btnResultDown.layout(
                currentX,
                0,
                currentX + iconSize,
                iconSize
            )
            currentX += iconSize
            btnResultUp.layout(
                currentX,
                0,
                currentX + iconSize,
                iconSize
            )
        }

        private fun createImage(@DrawableRes icon: Int): AppCompatImageView {
            val image = AppCompatImageView(context)
            image.imageTintList = context.getColorStateList(R.color.tint_search_color)
            //image.imageTintList = ColorStateList.valueOf(iconColor)
            image.setImageResource(icon)
            image.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
            return image
        }

    }
}

