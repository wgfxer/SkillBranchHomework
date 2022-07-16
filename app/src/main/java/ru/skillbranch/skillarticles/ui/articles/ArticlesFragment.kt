package ru.skillbranch.skillarticles.ui.articles

import ArticlesState
import ArticlesViewModel
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.FragmentArticlesBinding
import ru.skillbranch.skillarticles.ui.BaseFragment
import ru.skillbranch.skillarticles.ui.delegates.viewBinding

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
            onClick = { articleItem -> viewModel.navigateToArticle(articleItem) },
            onToggleBookmark = { articleItem, isChecked ->
                viewModel.checkBookmark(articleItem, isChecked)
            }
        )

        viewBinding.rvArticles.apply {
            adapter = articlesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        articlesAdapter = null
    }

    override fun observeViewModelData() {
        viewModel.articles.observe(viewLifecycleOwner) { articlesAdapter?.submitList(it) }
    }
}