package ru.skillbranch.skillarticles.ui.articles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem

/**
 * @author Valeriy Minnulin
 */
class ArticlesAdapter(
    private val onClick : (ArticleItem) -> Unit,
    private val onToggleBookmark: (ArticleItem, Boolean) -> Unit
) : PagingDataAdapter<ArticleItem, ArticleVH>(ArticleDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        return ArticleVH(ArticleItemView(parent.context))
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position)!!, onClick, onToggleBookmark)
    }
}

class ArticleDiffCallback: DiffUtil.ItemCallback<ArticleItem>(){
    override fun areItemsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean  = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean  = oldItem == newItem

}

class ArticleVH(private val containerView: View) : RecyclerView.ViewHolder(containerView){

    fun bind(
        item: ArticleItem,
        onClick: (ArticleItem) -> Unit,
        onToggleBookmark: (ArticleItem, Boolean) -> Unit,
    ){
        (containerView as ArticleItemView).bind(item, onClick, onToggleBookmark)
    }
}