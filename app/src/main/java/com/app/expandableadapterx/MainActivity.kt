package com.app.expandableadapterx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.lifecycle.MutableLiveData
import com.app.expandableadapterx.databinding.ActivityMainBinding
import com.google.gson.reflect.TypeToken
import com.nanako.http.HttpTask2
import com.nanako.log.Log
import com.nanako.log.Log.Companion.LOG
import com.nanako.util.Util

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Log.LOG.isEnabled = true
        Log.LOG.i("aabc")

        LOG.i(Util.replaceChar("1234567", 2, 2, 10, "*"))
        LOG.i(Util.replaceChar("1234567", 0, 2, 10, "*"))
        LOG.i(Util.replaceChar(null, 0, 2, 10, "*"))

        val type = object :TypeToken<TestModel>(){}.type
        HttpTask2.log.isEnabled=true
        testHttp()
    }


    private fun testHttp() {

    }
}