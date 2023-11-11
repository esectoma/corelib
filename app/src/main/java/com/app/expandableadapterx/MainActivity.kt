package com.app.expandableadapterx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.app.expandableadapterx.databinding.ActivityMainBinding
import com.core.encryption.AESUtil
import com.google.gson.reflect.TypeToken
import com.core.log.Log
import com.core.log.Log.Companion.log
import com.core.util.Util

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Log.log.isEnabled = true
        Log.log.i("aabc")

        log.i(Util.replaceChar("1234567", 2, 2, 10, "*"))
        log.i(Util.replaceChar("1234567", 0, 2, 10, "*"))
        log.i(Util.replaceChar(null, 0, 2, 10, "*"))

        val type = object : TypeToken<TestModel>() {}.type
        testHttp()
        testCrypt()
    }


    private fun testHttp() {

    }

    private fun testCrypt() {
        val pwd = "1234567890123456"
        val e = AESUtil.encryptBase64(pwd, "31b6d9135b8053b367e72f03439e4e0757e8ffd8a06b3f7f57f02e1e35e6c889")
        log.e(e)
        val d = AESUtil.decryptBase64(pwd, e)
        log.e(d)
    }
}