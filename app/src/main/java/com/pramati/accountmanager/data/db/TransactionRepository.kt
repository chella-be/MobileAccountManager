package com.pramati.accountmanager.data.db

import android.app.Application
import android.content.Context
import com.pramati.accountmanager.data.model.Transaction

class TransactionRepository(context: Context) {

    private val transactionDao: TransactionDao

    init {
        val transactionDB = TransactionDB.getInstance(context)
        transactionDao = transactionDB.getTransactionDao()
    }

    fun getAllTransaction(): List<Transaction> {
        return transactionDao.getAll()
    }

    fun getTransaction(id: Int): Transaction {
        return transactionDao.find(id)
    }
}