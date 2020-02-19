package io.gnosis.kouban.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pm.gnosis.model.Solidity

@Dao
interface TokenInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: TokenInfoDb)

    @Query("SELECT * FROM ${TokenInfoDb.TABLE_NAME} WHERE ${TokenInfoDb.COL_ADDRESS} = :address")
    suspend fun load(address: Solidity.Address): TokenInfoDb?

    @Query("DELETE FROM ${TokenInfoDb.TABLE_NAME} WHERE ${TokenInfoDb.COL_ADDRESS} = :address")
    fun delete(address: Solidity.Address)
}
