package com.tods.project_olx.model

import java.io.Serializable

data class Message(
    var id: String = "",
    var threadId: String = "",
    var senderId: String = "",
    var text: String = "",
    var sentAt: Long = 0L
) : Serializable
