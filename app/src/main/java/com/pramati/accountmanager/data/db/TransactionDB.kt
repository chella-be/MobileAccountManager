package com.pramati.accountmanager.data.db

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pramati.accountmanager.data.model.Transaction
import com.pramati.accountmanager.data.utils.DateConverter

@Database(entities = [Transaction::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class TransactionDB : RoomDatabase() {
    abstract fun getTransactionDao(): TransactionDao

    companion object {
        private val lock = Any()
        private const val DB_NAME = "Transaction.db"
        private var INSTANCE: TransactionDB? = null

        fun getInstance(application: Context): TransactionDB {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE =
                        Room.databaseBuilder(
                            application,
                            TransactionDB::class.java, DB_NAME
                        )
                            .allowMainThreadQueries()
                            .build()
                }
                return INSTANCE!!
            }
        }
    }
}