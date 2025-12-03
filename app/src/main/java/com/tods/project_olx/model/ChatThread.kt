package com.tods.project_olx.model

import java.io.Serializable

data class ChatThread(
    var id: String = "",
    var adId: String = "",
    var adTitle: String = "",
    var adImageUrl: String = "",
    var buyerId: String = "",
    var sellerId: String = "",
    var lastMessage: String = "",
    var lastSenderId: String = "",
    var lastUpdatedAt: Long = 0L
) : Serializable
