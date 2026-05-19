package com.example.skinsmart.ui.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skinsmart.databinding.ItemPostBinding
import com.example.skinsmart.model.SocialPost
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

/**
 * Adapter for displaying social feed posts.
 *
 * @param posts         Initial list of posts.
 * @param currentUserId Firebase UID of the logged-in user. When provided, posts belonging
 *                      to this user will show Edit and Delete buttons.
 * @param onEditClicked Callback invoked when the user clicks Edit on their own post.
 * @param onDeleteClicked Callback invoked when the user clicks Delete on their own post.
 */
class SocialPostAdapter(
    private var posts: List<SocialPost>,
    private val currentUserId: String? = null,
    private val onEditClicked: ((SocialPost) -> Unit)? = null,
    private val onDeleteClicked: ((SocialPost) -> Unit)? = null
) : RecyclerView.Adapter<SocialPostAdapter.PostViewHolder>() {

    fun submitList(newPosts: List<SocialPost>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: SocialPost) {
            binding.tvAuthorName.text = post.authorName
            binding.tvAuthorSkinType.text = post.authorSkinType

            // Badge color by skin type — tint keeps the pill drawable shape intact
            val (bgTint, textColor) = when (post.authorSkinType.lowercase()) {
                "dry"         -> 0xFFDBEAFE.toInt() to 0xFF1D4ED8.toInt()
                "combination" -> 0xFFEDE9FE.toInt() to 0xFF6D28D9.toInt()
                "sensitive"   -> 0xFFFFE4E6.toInt() to 0xFFBE123C.toInt()
                else          -> 0xFFFFEDD5.toInt() to 0xFFC2410C.toInt()
            }
            binding.tvAuthorSkinType.backgroundTintList =
                android.content.res.ColorStateList.valueOf(bgTint)
            binding.tvAuthorSkinType.setTextColor(textColor)

            binding.tvProductName.text = post.productName
            binding.tvReviewText.text = "\"${post.reviewText}\""
            binding.tvReviewText.setTypeface(null, Typeface.ITALIC)
            binding.rbRating.rating = post.rating.toFloat()

            // Time-ago label
            val diffMs = System.currentTimeMillis() - post.timestamp
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
            binding.tvTime.text = when {
                hours < 1   -> "just now"
                hours < 24  -> "${hours}h ago"
                else        -> "${TimeUnit.MILLISECONDS.toDays(diffMs)}d ago"
            }

            // Load post image if available
            if (post.imageUrl.isNotEmpty()) {
                binding.ivPostImage.visibility = View.VISIBLE
                Picasso.get().load(post.imageUrl).fit().centerCrop().into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            // Show Edit/Delete only for the current user's own posts
            val isOwnPost = currentUserId != null && post.userId == currentUserId
            if (isOwnPost && onEditClicked != null && onDeleteClicked != null) {
                binding.layoutEditDelete.visibility = View.VISIBLE
                binding.btnEditPost.setOnClickListener { onEditClicked.invoke(post) }
                binding.btnDeletePost.setOnClickListener { onDeleteClicked.invoke(post) }
            } else {
                binding.layoutEditDelete.visibility = View.GONE
            }
        }
    }
}
