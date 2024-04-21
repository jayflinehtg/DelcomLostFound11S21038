package com.ifs21038.lostfounds.presentation.lostfound

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ifs18005.delcomtodo.data.remote.response.DataAddLostFoundResponse

import com.ifs18005.delcomtodo.data.remote.response.DelcomLostFoundResponse
import com.ifs18005.delcomtodo.data.remote.response.DelcomResponse
import com.ifs21038.lostfounds.data.local.entity.DelcomLostFoundEntity
import com.ifs21038.lostfounds.data.remote.MyResult
import com.ifs21038.lostfounds.data.repository.LostFoundRepository
import com.ifs21038.lostfounds.presentation.ViewModelFactory

class LostFoundViewModel(
    private val lostFoundRepository: LostFoundRepository,
    private val localLostFoundRepository: LostFoundRepository
) : ViewModel() {
    fun getLostFound(lostfoundId: Int): LiveData<MyResult<DelcomLostFoundResponse>> {
        return lostFoundRepository.getDetail(lostfoundId).asLiveData()
    }
    fun postLostFound(
        title: String,
        description: String,
        status: String,
    ): LiveData<MyResult<DataAddLostFoundResponse>> {
        return lostFoundRepository.postLostFound(
            title,
            description,
            status
        ).asLiveData()
    }
    fun putLostFound(
        lostfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ): LiveData<MyResult<DelcomResponse>> {
        return lostFoundRepository.putLostFound(
            lostfoundId,
            title,
            description,
            status,
            isCompleted,
        ).asLiveData()
    }
    fun delete(lostfoundId: Int): LiveData<MyResult<DelcomResponse>> {
        return lostFoundRepository.delete(lostfoundId).asLiveData()
    }

    fun getLocalTodos(): LiveData<List<DelcomLostFoundEntity>?> {
        return localLostFoundRepository.getAllLostFounds()
    }
    fun getLocalTodo(lostfoundId: Int): LiveData<DelcomLostFoundEntity?> {
        return localLostFoundRepository.get(lostfoundId)
    }
    fun insertLocalTodo(todo: DelcomLostFoundEntity) {
        localLostFoundRepository.insert(todo)
    }
    fun deleteLocalLostFound(todo: DelcomLostFoundEntity) {
        localLostFoundRepository.delete(lostfound)
    }
    companion object {
        @Volatile
        private var INSTANCE: LostFoundViewModel? = null
        fun getInstance(
            todoRepository: LostFoundRepository,
            localLostFoundRepository: LostFoundRepository,
        ): LostFoundViewModel {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = LostFoundViewModel(
                    todoRepository,
                    localLostFoundRepository
                )
            }
            return INSTANCE as LostFoundViewModel
        }
    }
}
