package com.final_project.poop_bags.dao.station

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.final_project.poop_bags.models.Station
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {
    @Query("SELECT * FROM stations")
    fun getAllStations(): Flow<List<Station>>

    @Query("SELECT * FROM stations")
    suspend fun getAllStationsAsList(): List<Station>
    
    @Query("SELECT * FROM stations WHERE id = :stationId")
    suspend fun getStationById(stationId: String): Station?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: Station)
    
    @Update
    suspend fun updateStation(station: Station)
    
    @Query("DELETE FROM stations WHERE id = :stationId")
    suspend fun deleteStation(stationId: String)
    
    @Query("SELECT * FROM stations WHERE owner = :ownerId")
    fun getUserStations(ownerId: String): Flow<List<Station>>
    
    @Query("UPDATE stations SET isFavorite = :isFavorite WHERE id = :stationId")
    suspend fun updateFavoriteStatus(stationId: String, isFavorite: Boolean)

    @Query("SELECT * FROM stations WHERE id IN (:stationIds)")
    suspend fun getStationsByIds(stationIds: List<String>): List<Station>
} 