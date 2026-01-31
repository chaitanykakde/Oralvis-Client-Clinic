package com.oralvis.oralvisclient.domain.model

/**
 * Logged-in user (from /api/me or login response).
 */
data class User(
    val id: String,
    val name: String,
    val phoneNo: String,
    val role: String,
    val email: String? = null,
    val image: String? = null,
    val lastLogin: String? = null,
    val clinicId: String? = null
)
