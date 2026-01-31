package com.oralvis.oralvisclient.core.session

import com.oralvis.oralvisclient.domain.model.User

/**
 * In-memory session: current user and resolved clinicId.
 * Clear on logout; set after login and after resolving clinic-id.
 */
class SessionManager {

    @Volatile
    private var currentUser: User? = null

    @Volatile
    private var clinicId: String? = null

    fun setUser(user: User?) {
        currentUser = user
        if (user == null) clinicId = null
    }

    fun setClinicId(id: String?) {
        clinicId = id
    }

    fun getCurrentUser(): User? = currentUser

    fun getClinicId(): String? = clinicId

    fun getUserId(): String? = currentUser?.id

    fun clear() {
        currentUser = null
        clinicId = null
    }
}
