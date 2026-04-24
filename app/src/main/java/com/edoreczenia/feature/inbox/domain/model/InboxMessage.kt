package com.edoreczenia.feature.inbox.domain.model

data class InboxMessage(
    val id: String,
    val caseNumber: String,
    val senderName: String,
    val subject: String,
    val preview: String,
    val displayDate: String,
    val isRead: Boolean,
    val isStarred: Boolean
)

