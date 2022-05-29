package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.databinding.LayoutBottombarBinding
import ru.skillbranch.skillarticles.databinding.LayoutSubmenuBinding
import kotlin.math.hypot

class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    val binding = LayoutBottombarBinding.inflate(LayoutInflater.from(context), this)

    var isSearchMode = false

    init {
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return BottombarBehavior()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = SavedState(super.onSaveInstanceState())
        superState.ssIsSearchMode = isSearchMode
        return superState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            isSearchMode = state.ssIsSearchMode
            binding.reveal.isVisible = state.ssIsSearchMode
            binding.bottomGroup.isVisible = !state.ssIsSearchMode
        }
    }

    fun setSearchState(isSearch: Boolean) {
        if (isSearchMode == isSearch || !isAttachedToWindow) return
        isSearchMode = isSearch
        if (isSearchMode) animateShowSearch() else animateHideSearch()
    }

    fun setSearchInfo(searchCount: Int = 0, position: Int = 0) {
        with(binding) {
            btnResultDown.isEnabled = searchCount > 0
            btnResultUp.isEnabled = searchCount > 0
            tvSearchResult.text = if (searchCount == 0) "Not found" else "${position.inc()} of $searchCount"

            when(position) {
                0 -> btnResultUp.isEnabled = false
                searchCount.dec() -> btnResultDown.isEnabled = false
            }
        }
    }

    private fun animateShowSearch() {
        binding.reveal.isVisible = true

        val endRadius = hypot(width.toDouble(), height / 2.toDouble())

        val anim = ViewAnimationUtils.createCircularReveal(
            binding.reveal,
            width,
            height / 2,
            0f,
            endRadius.toFloat()
        )
        anim.doOnEnd {
            binding.bottomGroup.isVisible = false
        }
        anim.start()
    }

    private fun animateHideSearch() {
        binding.bottomGroup.isVisible = true

        val endRadius = hypot(width.toDouble(), height / 2.toDouble())

        val anim = ViewAnimationUtils.createCircularReveal(
            binding.reveal,
            width,
            height / 2,
            endRadius.toFloat(),
            0f
        )
        anim.doOnEnd {
            binding.reveal.isVisible = false
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
}