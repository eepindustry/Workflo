package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.User
import com.example.util.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.ui.utils.Language

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    val networkObserver = NetworkConnectivityObserver(application)
    
    val isConnected: StateFlow<Boolean> = networkObserver.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Assume connected initially until checked
        )

    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage = _currentLanguage.asStateFlow()

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()
    
    private val _contestants = MutableStateFlow<List<User>>(emptyList())
    val contestants = _contestants.asStateFlow()

    private val _contestStartTime = MutableStateFlow<Long?>(null)
    val contestStartTime = _contestStartTime.asStateFlow()
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.userDao())
        
        // Load existing user
        viewModelScope.launch {
            val existingUser = repository.getRegisteredDeviceUser()
            if (existingUser != null) {
                _currentUser.value = existingUser
            }
            
            // Start timer to track active time
            launch {
                while (true) {
                    kotlinx.coroutines.delay(60000) // Every 1 minute
                    val user = _currentUser.value
                    if (user != null) {
                        val updatedUser = user.copy(totalTimeWorkedSeconds = user.totalTimeWorkedSeconds + 60)
                        repository.updateUser(updatedUser)
                        _currentUser.value = updatedUser
                        
                        // Sync to Firebase occasionally
                        try {
                            val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                            db.child("users").child(updatedUser.googleId).setValue(updatedUser)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        
        // Listen to Firebase for contestants
        try {
            val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
            db.child("users").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val list = mutableListOf<User>()
                    for (child in snapshot.children) {
                        val user = child.getValue(User::class.java)
                        if (user != null && user.currentLevel >= 100) {
                            list.add(user)
                        }
                    }
                    list.sortByDescending { it.currentLevel }
                    _contestants.value = list
                    
                    if (list.size >= 1000) {
                        startContest()
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
            
            db.child("contest").child("startTime").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val time = snapshot.getValue(Long::class.java)
                    _contestStartTime.value = time
                    checkContestEnd(time, _contestants.value)
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun checkContestEnd(startTime: Long?, currentContestants: List<User>) {
        if (startTime == null) return
        val endTime = startTime + (30L * 24 * 60 * 60 * 1000)
        if (System.currentTimeMillis() >= endTime) {
            try {
                val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                db.child("contest").child("startTime").runTransaction(object : com.google.firebase.database.Transaction.Handler {
                    override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                        if (currentData.value != null) {
                            currentData.value = null // clear it
                            return com.google.firebase.database.Transaction.success(currentData)
                        }
                        return com.google.firebase.database.Transaction.abort()
                    }

                    override fun onComplete(
                        error: com.google.firebase.database.DatabaseError?,
                        committed: Boolean,
                        currentData: com.google.firebase.database.DataSnapshot?
                    ) {
                        if (committed) {
                            // We successfully ended the contest, distribute rewards!
                            for (i in currentContestants.indices) {
                                val user = currentContestants[i]
                                var reward = 0
                                if (i == 0) reward = 12000
                                else if (i == 1) reward = 7000
                                else if (i == 2) reward = 4000
                                
                                val updatedUser = user.copy(wcoins = user.wcoins + reward, currentLevel = 1)
                                db.child("users").child(updatedUser.googleId).setValue(updatedUser)
                                
                                if (updatedUser.googleId == _currentUser.value?.googleId) {
                                    viewModelScope.launch {
                                        repository.updateUser(updatedUser)
                                        _currentUser.value = updatedUser
                                    }
                                }
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startContest() {
        try {
            val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
            db.child("contest").child("startTime").addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    if (!snapshot.exists()) {
                        db.child("contest").child("startTime").setValue(System.currentTimeMillis())
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun mockGoogleLogin(androidId: String, email: String) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                db.child("device_registrations").child(androidId).get().addOnSuccessListener { snapshot ->
                    val registeredEmail = snapshot.getValue(String::class.java)
                    val trimmedEmail = email.trim()
                    if (registeredEmail != null && !registeredEmail.trim().equals(trimmedEmail, ignoreCase = true)) {
                        _loginError.value = "continue with your already registered Gmail: $registeredEmail"
                    } else {
                        if (registeredEmail == null) {
                            db.child("device_registrations").child(androidId).setValue(trimmedEmail)
                        }
                        
                        db.child("users").child(androidId).get().addOnSuccessListener { userSnapshot ->
                            val remoteUser = userSnapshot.getValue(User::class.java)
                            viewModelScope.launch {
                                if (remoteUser != null) {
                                    repository.insertUser(remoteUser)
                                    _currentUser.value = remoteUser
                                } else {
                                    // Completely new user
                                    val newUser = User(googleId = androidId, name = "")
                                    repository.insertUser(newUser)
                                    _currentUser.value = newUser
                                }
                            }
                        }.addOnFailureListener {
                            viewModelScope.launch {
                                val localUser = repository.getUser(androidId)
                                if (localUser != null) {
                                    _currentUser.value = localUser
                                } else {
                                    val newUser = User(googleId = androidId, name = "")
                                    repository.insertUser(newUser)
                                    _currentUser.value = newUser
                                }
                            }
                        }
                    }
                }.addOnFailureListener {
                    _loginError.value = "Network error while verifying device registration."
                }
            } catch (e: Exception) {
                _loginError.value = "Error: ${e.message}"
            }
        }
    }

    fun registerName(name: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (repository.getUserByName(name) != null) {
                _loginError.value = "Name already exists"
                return@launch
            }
            
            val newUser = user.copy(name = name)
            repository.insertUser(newUser)
            _currentUser.value = newUser
            
            // Sync to Firebase Database
            try {
                val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                db.child("users").child(newUser.googleId).setValue(newUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun updateScore(levelPassed: Boolean) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newUser = if (levelPassed) {
                user.copy(
                    currentLevel = user.currentLevel + 1
                )
            } else {
                user
            }
            repository.updateUser(newUser)
            _currentUser.value = newUser
            
            // Sync to Firebase Database (Leaderboard)
            try {
                val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                db.child("users").child(newUser.googleId).setValue(newUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun useExtraLife(onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentTime
        
        val userCalendar = java.util.Calendar.getInstance()
        userCalendar.timeInMillis = user.lastExtraLiveDate
        
        val isSameDay = calendar.get(java.util.Calendar.YEAR) == userCalendar.get(java.util.Calendar.YEAR) &&
                        calendar.get(java.util.Calendar.DAY_OF_YEAR) == userCalendar.get(java.util.Calendar.DAY_OF_YEAR)
                        
        val usedToday = if (isSameDay) user.extraLivesUsedToday else 0
        if (usedToday >= 60) return
        
        var newAdLivesUsed = user.adLivesUsedInCycle
        var newAdFreeRewardStart = user.adFreeRewardStartTime
        
        val inAdFreePeriod = currentTime < newAdFreeRewardStart + 3600_000L
        
        if (!inAdFreePeriod) {
            newAdLivesUsed++
            if (newAdLivesUsed >= 5) {
                newAdFreeRewardStart = currentTime
                newAdLivesUsed = 0
            }
        }
        
        val newUser = user.copy(
            extraLivesUsedToday = usedToday + 1,
            lastExtraLiveDate = currentTime,
            adLivesUsedInCycle = newAdLivesUsed,
            adFreeRewardStartTime = newAdFreeRewardStart
        )
        viewModelScope.launch {
            repository.updateUser(newUser)
            _currentUser.value = newUser
            
            // Sync to Firebase
            try {
                val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                db.child("users").child(newUser.googleId).setValue(newUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            onSuccess()
        }
    }
}
