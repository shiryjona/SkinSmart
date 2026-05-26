package com.example.skinsmart.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Handles uploading and deleting images from Firebase Storage.
 */
class StorageRepository {

    private val storage by lazy { FirebaseStorage.getInstance() }

    /**
     * Uploads an image URI to Firebase Storage under the "posts/" folder.
     * Returns the public download URL string.
     */
    suspend fun uploadPostImage(imageUri: Uri, userId: String): Result<String> {
        return try {
            val fileName = "posts/${userId}_${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child(fileName)
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes an image from Firebase Storage by its full URL.
     */
    suspend fun deleteImageByUrl(imageUrl: String): Result<Boolean> {
        return try {
            if (imageUrl.isNotEmpty()) {
                storage.getReferenceFromUrl(imageUrl).delete().await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
