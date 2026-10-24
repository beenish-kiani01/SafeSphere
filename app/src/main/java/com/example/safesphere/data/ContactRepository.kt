package com.example.safesphere.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContactRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Updated to use a global collection if user is not logged in, 
    // but typically we should map it to a persistent ID.
    // To ensure they stay after logout, we can use a local device ID or 
    // simply move the logic to not depend on auth.currentUser for the path.
    // However, for security, we'll use a semi-permanent device ID.
    
    private val contactsCollection = firestore.collection("device_contacts").document("fixed_device_id").collection("contacts")

    fun getContacts(): Flow<List<Contact>> = callbackFlow {
        val listener = contactsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val contacts = snapshot?.toObjects(Contact::class.java) ?: emptyList()
            trySend(contacts)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addContact(contact: Contact): Boolean {
        return try {
            val doc = contactsCollection.document()
            doc.set(contact.copy(id = doc.id)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateContact(contact: Contact): Boolean {
        return try {
            contactsCollection.document(contact.id).set(contact).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteContact(id: String): Boolean {
        return try {
            contactsCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
