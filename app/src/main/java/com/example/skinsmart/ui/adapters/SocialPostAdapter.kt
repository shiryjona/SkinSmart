package com.example.skinsmart.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skinsmart.databinding.ItemPostBinding
import com.example.skinsmart.model.SocialPost
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

class SocialPostAdapter(
    private var posts: List<SocialPost>
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

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: SocialPost) {
            binding.tvAuthorName.text = post.authorName
            binding.tvAuthorSkinType.text = post.authorSkinType
            binding.tvProductName.text = post.productName
            binding.tvReviewText.text = post.reviewText
            binding.rbRating.rating = post.rating.toFloat()

            // Calculate simple time ago
            val diffMs = System.currentTimeMillis() - post.timestamp
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
            if (hours < 24) {
                binding.tvTime.text = "${hours}h ago"
            } else {
                binding.tvTime.text = "${TimeUnit.MILLISECONDS.toDays(diffMs)}d ago"
            }

            // Optional Image (will implement URL loading later in MVP stage 2)
            binding.ivPostImage.visibility = View.GONE
        }
    }
}
