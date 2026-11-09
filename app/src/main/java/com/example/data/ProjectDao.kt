package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.FurnitureProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<FurnitureProject>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): FurnitureProject?

    @Query("SELECT * FROM projects WHERE customerId = :customerId ORDER BY updatedAt DESC")
    fun getProjectsForCustomer(customerId: Long): Flow<List<FurnitureProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: FurnitureProject): Long

    @Update
    suspend fun updateProject(project: FurnitureProject)

    @Delete
    suspend fun deleteProject(project: FurnitureProject)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectsCount(): Int
}
