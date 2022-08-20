package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.extensions.toArticleItem
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

/**
 * @author Valeriy Minnulin
 */
class ArticlesRepository(private val local: LocalDataHolder, private val network: NetworkDataHolder) {

    fun findArticles(): LiveData<List<ArticleItem>> = local.findArticles()
    fun makeArticleDataStore() = ArticlesDataSource(LocalDataHolder)
    @ExperimentalPagingApi
    fun makeArticlesMediator() = ArticlesMediator(network = network, local = local)
}

@ExperimentalPagingApi
class ArticlesMediator(
    val network: NetworkDataHolder,
    val local: LocalDataHolder
): RemoteMediator<Int, ArticleItem>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, ArticleItem>): MediatorResult {
        return try {
            when(loadType) {
                LoadType.REFRESH -> {
                    val articles = network.loadArticles(null, state.config.pageSize)
                    local.insertArticles(articles.map { it.toArticleItem() })
                    MediatorResult.Success(endOfPaginationReached = false)
                }
                LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    val articles = network.loadArticles(lastItem?.id?.toInt()?.inc(), state.config.pageSize)
                    local.insertArticles(articles.map { it.toArticleItem() })
                    MediatorResult.Success(endOfPaginationReached = articles.isEmpty())
                }
            }
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}

class ArticlesDataSource(val local: LocalDataHolder): PagingSource<Int, ArticleItem>() {

    init {
        local.attachDataSource(this)
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleItem>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        val size = state.config.pageSize

        val nextKey = anchorPage.nextKey
        val prevKey = anchorPage.prevKey
        val pageKey = prevKey?.plus(size) ?: nextKey?.minus(size)
        return pageKey
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleItem> {
        val pageKey = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val articles = local.loadArticles(pageKey, pageSize)
            val prevKey = if (pageKey > 0) pageKey.minus(pageSize) else null
            val nextKey = if (articles.isNotEmpty()) pageKey.plus(pageSize) else null


            LoadResult.Page(
                data = articles,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch(t: Throwable) {
            LoadResult.Error(t)
        }
    }
}