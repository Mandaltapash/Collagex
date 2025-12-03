package com.tods.project_olx.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

data class User(
    var name: String = "",
    var email: String = "",
    var id: String = "",
    var password: String = "",
    var phone: String = "",
    var photoUrl: String = "",
    var ratingAverage: Double = 0.0,
    var ratingCount: Int = 0,
    var collegeName: String = ""
){
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    fun configCurrentUser(): FirebaseUser? {
        auth = Firebase.auth
        return auth.currentUser
    }

    fun save(){
        val user = User(name = name, email = email)
        user.id = configCurrentUser()!!.uid
        user.phone = phone
        user.photoUrl = photoUrl
        user.ratingAverage = ratingAverage
        user.ratingCount = ratingCount
        user.collegeName = collegeName

        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(configCurrentUser()!!.uid).setValue(user)
    }
}
