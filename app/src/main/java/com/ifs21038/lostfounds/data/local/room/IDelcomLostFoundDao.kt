package com.ifs21038.lostfounds.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ifs21038.lostfounds.data.local.entity.DelcomLostFoundEntity

@Dao
interface IDelcomLostFoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(delcomTodo: DelcomLostFoundEntity)
    @Delete
    fun delete(delcomTodo: DelcomLostFoundEntity)
    @Query("SELECT * FROM delcom_todos WHERE id = :id LIMIT 1")
    fun get(id: Int): LiveData<DelcomLostFoundEntity?>
    @Query("SELECT * FROM delcom_todos ORDER BY created_at DESC")
    fun getAllTodos(): LiveData<List<DelcomLostFoundEntity>?>
    @Query("SELECT * FROM delcom_todos")
    fun getAllLostFounds(): LiveData<List<DelcomLostFoundEntity>?>
}
