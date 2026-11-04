package com.mtaapps.sehhaty.data.repository

import com.mtaapps.sehhaty.data.local.dao.SupervisorDao
import com.mtaapps.sehhaty.data.local.entity.SupervisorEntity
import kotlinx.coroutines.flow.Flow

class SupervisorRepository(private val supervisorDao: SupervisorDao) {
    fun getSupervisor(userId: String): Flow<SupervisorEntity?> =
        supervisorDao.getSupervisor(userId)

    suspend fun getSupervisorSync(userId: String): SupervisorEntity? =
        supervisorDao.getSupervisorSync(userId)

    suspend fun saveSupervisor(supervisor: SupervisorEntity): Long =
        supervisorDao.saveSupervisor(supervisor)

    suspend fun deleteSupervisor(supervisor: SupervisorEntity) =
        supervisorDao.deleteSupervisor(supervisor)
}
