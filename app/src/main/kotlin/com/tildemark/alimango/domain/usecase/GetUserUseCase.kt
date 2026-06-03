package com.tildemark.alimango.domain.usecase

import com.tildemark.alimango.domain.model.User
import com.tildemark.alimango.domain.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(pat: String? = null): User? {
        val currentPat = pat ?: userRepository.getSavedPat() ?: return null
        return userRepository.getOrFetchUser(currentPat)
    }
}
