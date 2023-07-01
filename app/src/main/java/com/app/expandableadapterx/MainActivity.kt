package com.app.expandableadapterx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.app.expandableadapterx.databinding.ActivityMainBinding
import com.google.gson.reflect.TypeToken
import com.nanako.log.Log
import com.nanako.log.Log.Companion.log
import com.nanako.util.Util

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

        val type = object :TypeToken<TestModel>(){}.type
        testHttp()
    }


    private fun testHttp() {

    }
}