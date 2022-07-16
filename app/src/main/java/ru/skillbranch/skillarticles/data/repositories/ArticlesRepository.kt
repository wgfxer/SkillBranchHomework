package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

/**
 * @author Valeriy Minnulin
 */
class ArticlesRepository(private val local: LocalDataHolder = LocalDataHolder) {

    fun findArticles(): LiveData<List<ArticleItem>> = local.findArticles()
}