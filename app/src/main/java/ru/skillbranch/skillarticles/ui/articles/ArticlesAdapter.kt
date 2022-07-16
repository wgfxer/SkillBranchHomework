package ru.skillbranch.skillarticles.ui.articles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.viewmodels.articles.ArticleItem
import java.util.Date

/**
 * @author Valeriy Minnulin
 */
class ArticlesAdapter(
    private val onClick : (ArticleItem) -> Unit,
    private val onToggleBookmark: (ArticleItem, Boolean) -> Unit
) : ListAdapter<ArticleItem, ArticleVH>(ArticleDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        return ArticleVH(LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false))
        //return ArticleVH(ArticleItemView(parent.context))
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position), onClick, onToggleBookmark)
    }

}

class ArticleDiffCallback: DiffUtil.ItemCallback<ArticleItem>(){
    override fun areItemsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean  = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean  = oldItem == newItem

}


class ArticleVH(private val containerView: View) : RecyclerView.ViewHolder(containerView){
    private val tvDate = containerView.findViewById<TextView>(R.id.tv_date)
    private val tvAuthor = containerView.findViewById<TextView>(R.id.tv_author)
    private val tvTitle = containerView.findViewById<TextView>(R.id.tv_title)
    private val ivPoster = containerView.findViewById<ImageView>(R.id.iv_poster)
    private val ivCategory = containerView.findViewById<ImageView>(R.id.iv_category)
    private val tvDescription = containerView.findViewById<TextView>(R.id.tv_description)
    private val ivLikes = containerView.findViewById<ImageView>(R.id.iv_likes)
    private val tvLikesCount = containerView.findViewById<TextView>(R.id.tv_likes_count)
    private val ivComments = containerView.findViewById<ImageView>(R.id.iv_comments)
    private val tvCommentsCount = containerView.findViewById<TextView>(R.id.tv_comments_count)
    private val tvReadDuration = containerView.findViewById<TextView>(R.id.tv_read_duration)
    private val ivBookmark = containerView.findViewById<ImageView>(R.id.iv_bookmark)


    fun bind(
        item: ArticleItem,
        onClick: (ArticleItem) -> Unit,
        onToggleBookmark: (ArticleItem, Boolean) -> Unit,
    ){
        containerView.setOnClickListener { onClick(item) }
        ivLikes.setOnClickListener { onToggleBookmark(item, true) }
        tvAuthor.text = item.author
        tvTitle.text = item.title
        tvDate.text = item.date.format()
        tvLikesCount.text = item.likeCount.toString()
        tvCommentsCount.text = item.commentCount.toString()
        tvReadDuration.text = item.readDuration.toString()
        ivLikes.isEnabled = item.isBookmark
    }
}