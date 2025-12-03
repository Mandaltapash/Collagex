package com.tods.project_olx.model

import java.io.Serializable

data class Review(
    var id: String = "",
    var adId: String = "",
    var reviewerId: String = "",
    var reviewedUserId: String = "",
    var rating: Int = 0,
    var comment: String = "",
    var createdAt: Long = 0L
) : Serializable
