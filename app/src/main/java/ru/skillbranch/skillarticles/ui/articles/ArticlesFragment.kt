package ru.skillbranch.skillarticles.ui.articles

import ArticlesState
import ArticlesViewModel
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.paging.CombinedLoadStates
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.FragmentArticlesBinding
import ru.skillbranch.skillarticles.ui.BaseFragment
import ru.skillbranch.skillarticles.ui.article.LoadStateItemsAdapter
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

/**
 * @author Valeriy Minnulin
 */
class ArticlesFragment: BaseFragment<ArticlesState, ArticlesViewModel, FragmentArticlesBinding>(R.layout.fragment_articles) {
    private var articlesAdapter: ArticlesAdapter? = null

    override val viewModel: ArticlesViewModel by activityViewModels()

    override val viewBinding: FragmentArticlesBinding by viewBinding(FragmentArticlesBinding::bind)

    override fun renderUi(data: ArticlesState) {
    }

    override fun setupViews() {
        articlesAdapter = ArticlesAdapter(
            ::onArticleClick,
            ::onToggleBookmark
        )

        with(viewBinding) {
            with(rvArticles) {
                ArticlesAdapter(::onArticleClick, ::onToggleBookmark)
                    .also { articlesAdapter = it  }
                    .run {
                        adapter = withLoadStateFooter(
                            footer = LoadStateItemsAdapter(::retry)
                        )
                        layoutManager = LinearLayoutManager(requireContext())
                        addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
                        addLoadStateListener(::loadStateListener)
                    }
            }

            btnRetry.setOnClickListener { articlesAdapter?.retry() }
        }

    }

    private fun loadStateListener(state: CombinedLoadStates) {
        with(viewBinding) {
            val isLoading = state.refresh == LoadState.Loading
            val isError = state.refresh is LoadState.Error
            val isSuccessfulLoad = !isLoading && !isError

            rvArticles.isVisible = isSuccessfulLoad
            progress.isVisible = isLoading
            groupErr.isVisible = isError
        }
    }

    private fun onArticleClick(articleItem: ArticleItem) {
        viewModel.navigateToArticle(articleItem)
    }

    private fun onToggleBookmark(articleItem: ArticleItem, isChecked: Boolean) {
        viewModel.checkBookmark(articleItem, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        articlesAdapter = null
    }

    @ExperimentalPagingApi
    override fun observeViewModelData() {
        viewModel.articlesPager.observe(viewLifecycleOwner) { articlesAdapter?.submitData(viewLifecycleOwner.lifecycle, it) }
    }
}