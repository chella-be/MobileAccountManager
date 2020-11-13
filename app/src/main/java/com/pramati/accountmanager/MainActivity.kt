package com.pramati.accountmanager

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.pramati.accountmanager.data.db.TransactionDB
import com.pramati.accountmanager.data.model.Transaction
import com.pramati.accountmanager.data.utils.AppExecutors
import com.pramati.accountmanager.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_READ_SMS_PERMISSION = 1000

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding?.tvMonth?.text = getCurrentMonth()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isPermissionGranted()) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS),
                REQUEST_READ_SMS_PERMISSION
            )
            return
        } else {
            initValues()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (REQUEST_READ_SMS_PERMISSION == requestCode) {
                Log.i("SMS", "REQUEST_READ_SMS_PERMISSION Permission Granted")
            }
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && REQUEST_READ_SMS_PERMISSION == requestCode) {

            val i = Intent()
            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:$packageName")
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(i)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initValues() {
        AppExecutors.getInstance()?.diskIO()?.execute {
            val database: TransactionDB = TransactionDB.getInstance(this)
            val dates = getDates()
            val listOfTransactions: List<Transaction> =
                database.getTransactionDao().getFromTable(dates[0], dates[1]);
            if (listOfTransactions.isNotEmpty()) {
                var incomeAmount = 0.0
                var expenseAmount = 0.0
                listOfTransactions.forEach {
                    if (it.isCredit) {
                        incomeAmount += it.amount
                    } else {
                        expenseAmount += it.amount
                    }
                }
                runOnUiThread {
                    binding?.tvIncomeVal?.text = "INR $incomeAmount"
                    binding?.tvExpenseVal?.text = "INR $expenseAmount"
                }
            }
        }
    }

    private fun getDates(): Array<Date> {
        val calendar = Calendar.getInstance()
        val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)
        println("day of month =========== $dayOfMonth")
        calendar.add(Calendar.DAY_OF_YEAR, -dayOfMonth - 1)
        return arrayOf<Date>(calendar.time, Calendar.getInstance().time)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentMonth(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("MMMM").format(cal.time)
    }
}