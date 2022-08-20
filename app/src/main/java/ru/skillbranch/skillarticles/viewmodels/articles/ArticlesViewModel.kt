import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.NavCommand
import ru.skillbranch.skillarticles.viewmodels.VMState
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem
import ru.skillbranch.skillarticles.viewmodels.articles.IArticlesViewModel

class ArticlesViewModel(savedStateHandle: SavedStateHandle) :
    BaseViewModel<ArticlesState>(ArticlesState(), savedStateHandle), IArticlesViewModel {
    private val repository: ArticlesRepository = ArticlesRepository(local = LocalDataHolder, network = NetworkDataHolder)
    val articles: LiveData<List<ArticleItem>> = repository.findArticles()

    @ExperimentalPagingApi
    val articlesPager: LiveData<PagingData<ArticleItem>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            initialLoadSize = 10,
            prefetchDistance = 30,
            enablePlaceholders = false
        ),
        remoteMediator = repository.makeArticlesMediator(),
        pagingSourceFactory = {
            repository.makeArticleDataStore()
        }
    )
        .liveData
        .cachedIn(viewModelScope)

    init {
        Log.e("ArticlesViewModel", "init viewmodel ${this::class.simpleName} ${this.hashCode()}")
    }


    override fun navigateToArticle(articleItem: ArticleItem) {
        articleItem.run {
            val options = NavOptions.Builder()
                .setEnterAnim(androidx.navigation.ui.R.animator.nav_default_enter_anim)
                .setExitAnim(androidx.navigation.ui.R.animator.nav_default_exit_anim)
                .setPopEnterAnim(androidx.navigation.ui.R.animator.nav_default_pop_enter_anim)
                .setPopExitAnim(androidx.navigation.ui.R.animator.nav_default_pop_exit_anim)

            navigate(//TODO в чем плюс билдера с бандлом ?
                NavCommand.Builder(
                    R.id.page_article,
                    bundleOf(
                        "article_id" to id,
                        "author" to author,
                        "author_avatar" to authorAvatar,
                        "category" to category,
                        "category_icon" to categoryIcon,
                        "poster" to poster,
                        "title" to title,
                        "date" to date
                    ),
                    options.build()
                )
            )

        }

    }

    override fun checkBookmark(articleItem: ArticleItem, checked: Boolean) {
    }
}

data class ArticlesState(
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val isLoading: Boolean = true,
    val isBookmark: Boolean = false,
    val selectedCategories: List<String> = emptyList(),
    val isHashtagSearch: Boolean = false,
    val tags: List<String> = emptyList(),
) : VMState