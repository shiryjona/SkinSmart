package com.example.skinsmart.data.repository

import com.example.skinsmart.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Checks if a user is already logged in (Persistent Session).
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Gets the current user ID. null if not logged in.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Logs in the user with email and password.
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userRecord = fetchUserFromFirestore(authResult.user!!.uid)
            Result.success(userRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registers a new user with Firebase Auth and saves their profile details to Firestore.
     */
    suspend fun register(email: String, password: String, name: String, skinType: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user!!.uid

            val newUser = User(
                id = userId,
                name = name,
                email = email,
                skinType = skinType,
                avatarUrl = "" // Default empty avatar
            )

            // Save the user data to Firestore
            firestore.collection("users").document(userId).set(newUser).await()

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logs out the user.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Retrieves the current user data from Firestore using the persistent Auth token.
     */
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
     * Utility method to fetch user data cleanly from Firestore.
     */
    private suspend fun fetchUserFromFirestore(userId: String): User {
        val documentSnapshot = firestore.collection("users").document(userId).get().await()
        return documentSnapshot.toObject(User::class.java)
            ?: throw Exception("User document not found in Firestore")
    }
}
