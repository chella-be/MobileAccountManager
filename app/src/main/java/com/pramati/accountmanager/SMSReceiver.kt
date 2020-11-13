package com.pramati.accountmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import android.widget.Toast
import com.pramati.accountmanager.data.db.TransactionDB
import com.pramati.accountmanager.data.model.Transaction
import com.pramati.accountmanager.data.utils.AppExecutors
import java.math.RoundingMode
import java.math.RoundingMode.*
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern


class SMSReceiver : BroadcastReceiver() {

    private var bundle: Bundle? = null
    private var currentSMS: SmsMessage? = null
    private var message: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            bundle = intent.extras
            if (bundle != null) {
                val pduObjects = bundle!!["pdus"] as Array<*>?
                if (pduObjects != null) {
                    for (aObject in pduObjects) {
                        currentSMS = aObject?.let { getIncomingMessage(it, bundle!!) }
                        val senderNo: String = currentSMS!!.displayOriginatingAddress
                        message = currentSMS!!.displayMessageBody
                        println("===================== Received messages $message")
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        processMessage(message!!, context)
                    }
                    abortBroadcast()
                }
            }
        }
    }

    private fun getIncomingMessage(aObject: Any, bundle: Bundle): SmsMessage {
        val currentSMS: SmsMessage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val format = bundle.getString("format")
            currentSMS = SmsMessage.createFromPdu(aObject as ByteArray?, format)
        } else {
            currentSMS = SmsMessage.createFromPdu(aObject as ByteArray)
        }

        return currentSMS
    }

    private fun processMessage(message: String, context: Context?) {
        val subKeywords = arrayOf("Payment", "Debit")
        val addKeywords = arrayOf("Credit")

        val database: TransactionDB = TransactionDB.getInstance(context!!)

        //Credit
        for (keyword in addKeywords) {
            if (message.toLowerCase(Locale.getDefault())
                    .contains(keyword.toLowerCase(Locale.getDefault()))
            ) {
                val pattern = Pattern.compile("(INR|Rs)[\\d,]+.[\\d]+")
                val matcher = pattern.matcher(message)
                var amountToBeAdded = 0.0F
                while (matcher.find()) {
                    var amount = matcher.group()
                    if (amount.contains("INR")) {
                        amount = amount.replace("INR", "").replace(",", "")
                        amountToBeAdded = roundOffDecimal(amount.toFloat())
                    } else if (amount.contains("Rs")) {
                        amount = amount.replace("Rs", "").replace(",", "")
                        amountToBeAdded = roundOffDecimal(amount.toFloat())
                    }
                    val transaction =
                        Transaction(amountToBeAdded, "Banking", true, Date());
                    AppExecutors.getInstance()?.diskIO()?.execute {
                        database.getTransactionDao().save(transaction)
                    }
                    break
                }
            }
        }

        //Debit
        for (keyword in subKeywords) {
            if (message.toLowerCase(Locale.getDefault())
                    .contains(keyword.toLowerCase(Locale.getDefault()))
            ) {
                val pattern = Pattern.compile("(INR|Rs)[\\d,]+.[\\d]+")
                val matcher = pattern.matcher(message)
                var amountToBeSubtract = 0.0F
                while (matcher.find()) {
                    var amount = matcher.group()
                    if (amount.contains("INR")) {
                        amount = amount.replace("INR", "").replace(",", "")
                        amountToBeSubtract = roundOffDecimal(amount.toFloat())
                    } else if (amount.contains("Rs")) {
                        amount = amount.replace("Rs", "").replace(",", "")
                        amountToBeSubtract = roundOffDecimal(amount.toFloat())
                    }
                    val transaction =
                        Transaction(amountToBeSubtract, "Banking", false, Date());
                    AppExecutors.getInstance()?.diskIO()?.execute {
                        database.getTransactionDao().save(transaction)
                    }
                    break
                }
            }
        }
    }

    private fun roundOffDecimal(number: Float): Float {
        val df = DecimalFormat("#.##")
        df.roundingMode = CEILING
        return df.format(number).toFloat()
    }

}
