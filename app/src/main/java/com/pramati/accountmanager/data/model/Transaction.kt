package com.pramati.accountmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Transaction(
    var amount: Float = 0.0F,
    var category: String = "",
    var isCredit: Boolean = false,
    var date: Date
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}