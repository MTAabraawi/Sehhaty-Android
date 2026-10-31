package com.mtaapps.sehhaty.data.local.dao

import androidx.room.*
import com.mtaapps.sehhaty.data.local.entity.DrugReferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DrugReferenceDao {

    @Query("SELECT * FROM drug_reference ORDER BY tradeNameAr ASC")
    fun getAllDrugs(): Flow<List<DrugReferenceEntity>>

    @Query("SELECT COUNT(*) FROM drug_reference")
    suspend fun countDrugs(): Int

    @Query("SELECT * FROM drug_reference WHERE id = :id LIMIT 1")
    suspend fun getDrugById(id: Long): DrugReferenceEntity?

    @Query("""
        SELECT * FROM drug_reference
        WHERE tradeNameAr LIKE '%' || :query || '%'
           OR tradeNameEn LIKE '%' || :query || '%'
           OR scientificName LIKE '%' || :query || '%'
           OR activeIngredient LIKE '%' || :query || '%'
        ORDER BY tradeNameAr ASC
    """)
    fun searchDrugs(query: String): Flow<List<DrugReferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDrugs(drugs: List<DrugReferenceEntity>)
}
