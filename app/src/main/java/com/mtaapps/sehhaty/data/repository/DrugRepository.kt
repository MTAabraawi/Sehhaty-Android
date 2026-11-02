package com.mtaapps.sehhaty.data.repository

import com.mtaapps.sehhaty.data.local.dao.DrugReferenceDao
import com.mtaapps.sehhaty.data.local.entity.DrugReferenceEntity
import kotlinx.coroutines.flow.Flow

class DrugRepository(private val drugReferenceDao: DrugReferenceDao) {
    fun getAllDrugs(): Flow<List<DrugReferenceEntity>> =
        drugReferenceDao.getAllDrugs()

    fun search(query: String): Flow<List<DrugReferenceEntity>> =
        drugReferenceDao.searchDrugs(query)

    suspend fun getDrugById(id: Long): DrugReferenceEntity? =
        drugReferenceDao.getDrugById(id)
}
