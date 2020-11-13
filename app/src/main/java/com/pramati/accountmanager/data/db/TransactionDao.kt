package com.pramati.accountmanager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pramati.accountmanager.data.model.Transaction
import java.util.*

@Dao
interface TransactionDao {

    @Query("SELECT * FROM `Transaction` ORDER BY date DESC")
    fun getAll(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(transaction: Transaction)

    @Query("DELETE FROM `Transaction`")
    fun deleteAll()

    @Query("SELECT * FROM `Transaction` WHERE id = :id")
    fun find(id: Int): Transaction

    @Query("SELECT * FROM `Transaction` WHERE date BETWEEN :startDate AND :endDate")
    fun getFromTable(startDate: Date, endDate: Date): List<Transaction>
}