package com.example.myapplication.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.AuthResponse
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for handling authentication state and operations
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application.applicationContext)
    
    // LiveData for authentication state
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Auth response data
    private val _authResponse = MutableLiveData<AuthResponse?>()
    val authResponse: LiveData<AuthResponse?> = _authResponse
    
    init {
        // Initialize MSAL
        viewModelScope.launch {
            try {
                val initialized = authRepository.initializeMsal()
                if (!initialized) {
                    _errorMessage.value = "Failed to initialize Microsoft authentication"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
            
            // Check if user is already logged in
            _isLoggedIn.value = authRepository.isLoggedIn()
        }
    }
    
    /**
     * Sign in with Microsoft account
     */
    fun signInWithMicrosoft() {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                val result = authRepository.signInWithMicrosoft()
                
                result.fold(
                    onSuccess = { response ->
                        _authResponse.value = response
                        _isLoggedIn.value = true
                        _errorMessage.value = null
                    },
                    onFailure = { error ->
                        Log.e("AuthViewModel", "Sign in failed", error)
                        _errorMessage.value = "Sign in failed: ${error.message}"
                        _isLoggedIn.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in error", e)
                _errorMessage.value = "Error: ${e.message}"
                _isLoggedIn.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = authRepository.signOut()
                
                result.fold(
                    onSuccess = {
                        _isLoggedIn.value = false
                        _authResponse.value = null
                    },
                    onFailure = { error ->
                        _errorMessage.value = "Sign out failed: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear any error messages
     */
    fun clearError() {
        _errorMessage.value = null
    }
} 