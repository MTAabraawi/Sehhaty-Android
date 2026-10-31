package com.mtaapps.sehhaty.data.repository

import com.mtaapps.sehhaty.data.local.dao.SupervisorDao
import com.mtaapps.sehhaty.data.local.entity.SupervisorEntity
import kotlinx.coroutines.flow.Flow

class SupervisorRepository(private val supervisorDao: SupervisorDao) {
    fun getSupervisor(userId: String): Flow<SupervisorEntity?> =
        supervisorDao.getSupervisor(userId)

    suspend fun getSupervisorSync(userId: String): SupervisorEntity? =
        supervisorDao.getSupervisorSync(userId)

    suspend fun saveSupervisor(supervisor: SupervisorEntity, activeUserId: String): Long {
        require(activeUserId.isNotBlank()) { "No active user session" }
        require(supervisor.userId == activeUserId) { "Supervisor does not belong to the active user" }
        return supervisorDao.saveSupervisor(supervisor)
    }

    suspend fun deleteSupervisor(supervisor: SupervisorEntity, activeUserId: String): Int {
        require(activeUserId.isNotBlank()) { "No active user session" }
        require(supervisor.userId == activeUserId) { "Supervisor does not belong to the active user" }
        return supervisorDao.deleteSupervisor(supervisor.id, activeUserId)
    }
}
