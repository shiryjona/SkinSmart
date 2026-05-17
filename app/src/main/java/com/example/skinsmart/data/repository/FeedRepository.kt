package com.example.skinsmart.data.repository

import com.example.skinsmart.model.SocialPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FeedRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("social_posts")

    /**
     * Publishes a new review to the global Firestore collection
     */
    suspend fun publishPost(post: SocialPost): Result<Boolean> {
        return try {
            // Generate a random ID for the new post
            val docRef = postsCollection.document()
            val finalPost = post.copy(postId = docRef.id)
            docRef.set(finalPost).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listens to the latest 50 posts from the global feed in real-time
     */
    fun listenToFeed(
        onUpdate: (List<SocialPost>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                
                val posts = snapshot?.toObjects(SocialPost::class.java) ?: emptyList()
                onUpdate(posts)
            }
    }

    /**
     * Returns all posts published by a specific user, sorted by timestamp descending.
     * NOTE: We sort client-side to avoid requiring a composite Firestore index.
     */
    suspend fun getUserPosts(userId: String): Result<List<SocialPost>> {
        return try {
            val snapshot = postsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val posts = snapshot.toObjects(SocialPost::class.java)
                .sortedByDescending { it.timestamp }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing post's text, rating, and optionally its image URL.
     */
    suspend fun updatePost(post: SocialPost): Result<Boolean> {
        return try {
            postsCollection.document(post.postId).set(post).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a post by its ID from Firestore.
     */
    suspend fun deletePost(postId: String): Result<Boolean> {
        return try {
            postsCollection.document(postId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves the latest 50 posts from the global feed
     */
    suspend fun getGlobalFeed(): Result<List<SocialPost>> {
        return try {
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            val posts = snapshot.toObjects(SocialPost::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
