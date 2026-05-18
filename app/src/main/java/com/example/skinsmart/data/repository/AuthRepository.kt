package com.example.skinsmart.data.repository

import android.graphics.Bitmap
import com.example.skinsmart.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userRecord = fetchUserFromFirestore(authResult.user!!.uid)
            Result.success(userRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, name: String, skinType: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user!!.uid
            val newUser = User(id = userId, name = name, email = email, skinType = skinType, avatarUrl = "")
            firestore.collection("users").document(userId).set(newUser).await()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): Result<User> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
        return try {
            val userRecord = fetchUserFromFirestore(userId)
            Result.success(userRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the user profile and all their posts in a single atomic batch.
     * Ensures consistency between the user document and their social feed entries.
     */
    suspend fun updateUserProfile(userId: String, name: String, skinType: String, imageBitmap: Bitmap? = null): Result<User> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val userSnapshot = userRef.get().await()
            val currentAvatarUrl = userSnapshot.getString("avatarUrl") ?: ""

            val batch = firestore.batch()
            val userUpdates = mutableMapOf<String, Any>(
                "name" to name,
                "skinType" to skinType
            )

            var avatarToSync = currentAvatarUrl
            if (imageBitmap != null) {
                avatarToSync = uploadProfileImage(userId, imageBitmap)
                userUpdates["avatarUrl"] = avatarToSync
            }

            // 1. Update the User document
            batch.update(userRef, userUpdates)

            // 2. Query all posts by this user from the CORRECT collection "social_posts"
            // We search by "userId" field, which matches SocialPost model
            val postsQuery = firestore.collection("social_posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // 3. Update fields in each post document to maintain consistency
            for (document in postsQuery.documents) {
                batch.update(document.reference, mapOf(
                    "authorName" to name,
                    "authorSkinType" to skinType,
                    "authorAvatarUrl" to avatarToSync
                ))
            }

            // 4. Commit all changes atomically
            batch.commit().await()

            val updatedUser = fetchUserFromFirestore(userId)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadProfileImage(userId: String, bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val data = baos.toByteArray()
        val storageRef = storage.reference.child("avatars/$userId.jpg")
        storageRef.putBytes(data).await()
        return storageRef.downloadUrl.await().toString()
    }

    private suspend fun fetchUserFromFirestore(userId: String): User {
        val documentSnapshot = firestore.collection("users").document(userId).get().await()
        return documentSnapshot.toObject(User::class.java) ?: throw Exception("User not found")
    }
}
