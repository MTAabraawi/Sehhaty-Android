package com.mtaapps.sehhaty.data.local.dao

import androidx.room.*
import com.mtaapps.sehhaty.data.local.entity.SupervisorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupervisorDao {
    @Query("SELECT * FROM family_supervisors WHERE userId = :userId LIMIT 1")
    fun getSupervisor(userId: String): Flow<SupervisorEntity?>

    @Query("SELECT * FROM family_supervisors WHERE userId = :userId LIMIT 1")
    suspend fun getSupervisorSync(userId: String): SupervisorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSupervisor(supervisor: SupervisorEntity): Long

    @Update
    suspend fun updateSupervisor(supervisor: SupervisorEntity)

    @Query("DELETE FROM family_supervisors WHERE id = :id AND userId = :userId")
    suspend fun deleteSupervisor(id: Long, userId: String): Int
}
