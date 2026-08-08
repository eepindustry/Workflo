package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val googleId: String = "",
    val name: String = "",
    val currentLevel: Int = 1,
    val levelsToQualify: Int = 100,
    val totalTimeWorkedSeconds: Long = 0,
    val rank: Int = 50,
    val extraLivesUsedToday: Int = 0,
    val lastExtraLiveDate: Long = 0L,
    val wcoins: Int = 0,
    val adLivesUsedInCycle: Int = 0,
    val adFreeRewardStartTime: Long = 0L
)
