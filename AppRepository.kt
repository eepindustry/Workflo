package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val userDao: UserDao) {
    fun getUserFlow(googleId: String): Flow<User?> = userDao.getUserFlow(googleId)

    suspend fun getUser(googleId: String): User? = userDao.getUser(googleId)

    suspend fun getUserByName(name: String): User? = userDao.getUserByName(name)
    
    suspend fun getRegisteredDeviceUser(): User? = userDao.getAnyUser()

    suspend fun insertUser(user: User) = userDao.insertUser(user)
    
    suspend fun updateUser(user: User) = userDao.updateUser(user)
}
