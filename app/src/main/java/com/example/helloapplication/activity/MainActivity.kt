package com.example.helloapplication.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.helloapplication.R
import com.example.helloapplication.database.DatabaseHandler
import com.example.helloapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlaces::class.java)
            startActivity(intent)
        }

        getHappyPlacesListFromLocalDB()
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)

        val getHappyPlacesList = dbHandler.getHappyPlaceList()

        if(getHappyPlacesList.size > 0){
            for (i in getHappyPlacesList) {
                Log.e("Title", i.title)
                Log.e("Date", i.date)
                Log.e("Description", i.description)

            }
        }
    }
}