package com.example.skinsmart.data.repository

import com.example.skinsmart.model.SocialPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
