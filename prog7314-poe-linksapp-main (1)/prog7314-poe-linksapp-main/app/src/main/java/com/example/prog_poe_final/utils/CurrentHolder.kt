package com.example.prog_poe_final.utils

import com.example.prog_poe_final.models.User

object CurrentUserHolder {
    var user: User? = null

    fun setCurrentUser(currentUser: User) {
        user = currentUser
    }

    fun getCurrentUser(): User? = user
    fun clear() {
        user = null
    }

    fun clearUser() {
        user = null
    }
}

