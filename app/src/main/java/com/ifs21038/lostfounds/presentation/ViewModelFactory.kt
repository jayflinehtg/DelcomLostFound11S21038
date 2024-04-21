package com.ifs21038.lostfounds.presentation

import com.ifs21038.lostfounds.di.Injection
import com.ifs21038.lostfounds.presentation.login.LoginViewModel
import com.ifs21038.lostfounds.presentation.lostfound.LostFoundViewModel
import com.ifs21038.lostfounds.presentation.main.MainViewModel
import com.ifs21038.lostfounds.presentation.profile.ProfileViewModel
import com.ifs21038.lostfounds.presentation.register.RegisterViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ifs21038.lostfounds.data.repository.AuthRepository
import com.ifs21038.lostfounds.data.repository.LocalLostFoundRepository
import com.ifs21038.lostfounds.data.repository.LostFoundRepository
import com.ifs21038.lostfounds.data.repository.UserRepository

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val lostfoundRepository: LostFoundRepository,
    private val localLostFoundRepository: LocalLostFoundRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel
                    .getInstance(authRepository) as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel
                    .getInstance(authRepository) as T
            }

            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel
                    .getInstance(authRepository, lostfoundRepository) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel
                    .getInstance(authRepository, userRepository) as T
            }

            modelClass.isAssignableFrom(LostFoundViewModel::class.java) -> {
                LostFoundViewModel
                    .getInstance(lostfoundRepository, localLostFoundRepository) as T
            }

            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: " + modelClass.name
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = ViewModelFactory(
                    Injection.provideAuthRepository(context),
                    Injection.provideUserRepository(context),
                    Injection.provideLostFoundRepository(context),
                    Injection.provideLocalLostFoundRepository(context),
                )
            }
            return INSTANCE as ViewModelFactory
        }
    }
}