package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE googleId = :googleId LIMIT 1")
    fun getUserFlow(googleId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE googleId = :googleId LIMIT 1")
    suspend fun getUser(googleId: String): User?

    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): User?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUser(): User? // Useful to check if device is already registered to someone

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)
}
