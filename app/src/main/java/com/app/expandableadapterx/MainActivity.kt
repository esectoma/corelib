package com.app.expandableadapterx

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.app.expandableadapterx.databinding.ActivityMainBinding
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

        LOG.i(Util.replaceChar("1234567",2,2,10,"*"))
        LOG.i(Util.replaceChar("1234567",0,2,10,"*"))
        LOG.i(Util.replaceChar(null,0,2,10,"*"))
    }
}