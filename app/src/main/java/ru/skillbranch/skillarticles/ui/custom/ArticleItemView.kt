package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.widget.FrameLayout
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

class ArticleItemView(context: Context): FrameLayout(context) {//TODO в каком пакете?
    fun bind(item: ArticleItem, onClick: (ArticleItem) -> Unit, onToggleBookmark: (ArticleItem, Boolean) -> Unit) {
    }
}
