package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

class ArticleItemView(context: Context): ViewGroup(context) {

    private val padding = context.dpToIntPx(16)
    private val marginMedium = context.dpToIntPx(16)
    private val posterMarginBottom = context.dpToIntPx(28)
    private val titleMarginEnd = context.dpToIntPx(24)
    private val marginSmall = context.dpToIntPx(8)
    private val imageViewPosterSize = context.dpToIntPx(64)
    private val imageViewCategorySize = context.dpToIntPx(40)
    private val defaultImageSize = context.dpToIntPx(16)
    private val iconTint = context.getColorStateList(R.color.color_gray)

    private val tvDate = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(context.getColor(R.color.color_gray))
    }
    private val tvAuthor = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(attrValue(android.R.attr.colorPrimary))
    }
    private val tvTitle = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setTextColor(attrValue(android.R.attr.colorPrimary))
        setTypeface(null, Typeface.BOLD)
    }
    private val ivPoster = ImageView(context).apply { setSize(imageViewPosterSize) }
    private val ivCategory = ImageView(context).apply { setSize(imageViewCategorySize) }
    private val tvDescription = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(context.getColor(R.color.color_gray))
    }
    private val ivLikes = ImageView(context).apply {
        setSize(defaultImageSize)
        imageTintList = iconTint
        setImageResource(R.drawable.ic_favorite_black_24dp)
    }
    private val tvLikesCount = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(context.getColor(R.color.color_gray))
    }
    private val ivComments = ImageView(context).apply {
        setSize(defaultImageSize)
        setImageResource(R.drawable.ic_insert_comment_black_24dp)
        imageTintList = iconTint
    }
    private val tvCommentsCount = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(context.getColor(R.color.color_gray))
    }
    private val tvReadDuration = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(context.getColor(R.color.color_gray))
    }
    private val ivBookmark = ImageView(context).apply {
        setSize(defaultImageSize)
        imageTintList = iconTint
        setImageResource(R.drawable.bookmark_states)
    }

    private var bottomBarrier = 0

    init {
        setPadding(padding)
        addView(tvDate)
        addView(tvAuthor)
        addView(tvTitle)
        addView(ivPoster)
        addView(ivCategory)
        addView(tvDescription)
        addView(ivLikes)
        addView(tvLikesCount)
        addView(ivComments)
        addView(tvCommentsCount)
        addView(tvReadDuration)
        addView(ivBookmark)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        children.forEach { measureChild(it, heightMeasureSpec, widthMeasureSpec) }
        tvDescription.measure(
            MeasureSpec.makeMeasureSpec(width - paddingLeft - paddingRight, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        tvTitle.measure(
            MeasureSpec.makeMeasureSpec(width - paddingLeft - paddingRight - ivPoster.measuredWidth - titleMarginEnd, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = paddingTop + paddingBottom + tvDate.measuredHeight + ivPoster.measuredHeight +
            marginMedium + posterMarginBottom + marginSmall + tvDescription.measuredHeight + marginSmall + tvReadDuration.measuredHeight
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val right = r - paddingRight
        tvDate.layout(
            paddingLeft,
            paddingTop,
            paddingLeft + tvDate.measuredWidth,
            paddingTop + tvDate.measuredHeight
        )
        tvAuthor.layout(
            tvDate.right + marginMedium,
            paddingTop,
            l + width,
            paddingTop + tvAuthor.measuredHeight
        )
        ivPoster.layout(
            right - ivPoster.measuredWidth,
            tvAuthor.bottom + marginMedium,
            right,
            tvAuthor.bottom + marginMedium + ivPoster.measuredHeight
        )
        ivCategory.layout(
            ivPoster.left - ivCategory.measuredWidth / 2,
            ivPoster.bottom - ivCategory.measuredHeight / 2,
            ivPoster.left + ivCategory.measuredWidth / 2,
            ivPoster.bottom + ivCategory.measuredHeight / 2
        )
        bottomBarrier = ivPoster.bottom + posterMarginBottom
        val tvTitleTop = tvAuthor.bottom + ((bottomBarrier - tvAuthor.bottom - tvTitle.measuredHeight) / 2)
        tvTitle.layout(
            paddingLeft,
            tvTitleTop,
            ivPoster.left - titleMarginEnd,
            tvTitleTop + tvTitle.measuredHeight
        )
        tvDescription.layout(
            paddingLeft,
            bottomBarrier + marginSmall,
            right,
            bottomBarrier + marginSmall + tvDescription.measuredHeight
        )
        tvLikesCount.layout(
            paddingLeft + ivLikes.measuredWidth + marginSmall,
            tvDescription.bottom + marginSmall,
            paddingLeft + ivLikes.measuredWidth + marginSmall + tvLikesCount.measuredWidth,
            tvDescription.bottom + marginSmall + tvLikesCount.measuredHeight
        )
        ivLikes.layout(
            paddingLeft,
            (tvLikesCount.top + tvLikesCount.bottom) / 2 - ivLikes.measuredHeight / 2,
            paddingLeft + ivLikes.measuredWidth,
            (tvLikesCount.top + tvLikesCount.bottom) / 2 + ivLikes.measuredHeight / 2,
        )
        ivComments.layout(
            tvLikesCount.right + marginMedium,
            tvDescription.bottom + marginSmall + tvCommentsCount.measuredHeight / 2 - ivComments.measuredHeight /2,
            tvLikesCount.right + marginMedium + ivComments.measuredWidth,
            tvDescription.bottom + marginSmall + tvCommentsCount.measuredHeight / 2 + ivComments.measuredHeight /2,
        )
        tvCommentsCount.layout(
            ivComments.right + marginSmall,
            tvDescription.bottom + marginSmall,
            ivComments.right + marginSmall + tvCommentsCount.measuredWidth,
            tvDescription.bottom + marginSmall + tvCommentsCount.measuredHeight
        )
        tvReadDuration.layout(
            tvCommentsCount.right + marginMedium,
            tvDescription.bottom + marginSmall,
            right - marginMedium - ivBookmark.measuredWidth,
            measuredHeight
        )
        ivBookmark.layout(
            right - ivBookmark.measuredWidth,
            tvReadDuration.top,
            right,
            tvReadDuration.top + ivBookmark.measuredHeight
        )
    }

    fun bind(
        item: ArticleItem,
        onClick: (ArticleItem) -> Unit,
        onToggleBookmark: (ArticleItem, Boolean) -> Unit) {
        setOnClickListener { onClick(item) }
        ivLikes.setOnClickListener { onToggleBookmark(item, true) }
        tvAuthor.text = item.author
        tvDescription.text = item.description
        tvTitle.text = item.title
        tvDate.text = item.date.format()
        tvLikesCount.text = item.likeCount.toString()
        tvCommentsCount.text = item.commentCount.toString()
        tvReadDuration.text = item.readDuration.toString()
        ivLikes.isEnabled = item.isBookmark
        Glide.with(ivPoster)
            .load(item.poster)
            .into(ivPoster)

        Glide.with(ivCategory)
            .load(item.categoryIcon)
            .into(ivCategory)

        ivBookmark.isEnabled = item.isBookmark
        requestLayout()
        invalidate()
    }
}

private fun ImageView.setSize(size: Int) {
    layoutParams = ViewGroup.LayoutParams(size, size)
}