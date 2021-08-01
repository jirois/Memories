package com.example.helloapplication.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.LinearLayoutBindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloapplication.R
import com.example.helloapplication.adapter.HappyPlacesAdapter
import com.example.helloapplication.database.DatabaseHandler
import com.example.helloapplication.databinding.ActivityMainBinding
import com.example.helloapplication.model.HappyPlaceModel

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlaces::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getHappyPlacesListFromLocalDB()
    }

    // Call Back method  to get the Message form other Activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (requestCode == Activity.RESULT_OK) {
                getHappyPlacesListFromLocalDB()
            } else {
                Log.e("Activity", "Cancelled or BackPressed")
            }
        }
    }


    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)

        val getHappyPlacesList = dbHandler.getHappyPlaceList()

        if(getHappyPlacesList.size > 0){
           binding.rvHappyPlaces.visibility = View.VISIBLE
           binding.tvNoRecordAvailable.visibility = View.GONE

           setupHappyPlacesRecyclerview(getHappyPlacesList)
        } else {
            binding.rvHappyPlaces.visibility = View.GONE
            binding.tvNoRecordAvailable.visibility = View.VISIBLE
        }
    }

    private fun setupHappyPlacesRecyclerview(happyPlacesList: ArrayList<HappyPlaceModel>){
        binding.rvHappyPlaces.layoutManager = LinearLayoutManager(this)
        binding.rvHappyPlaces.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        binding.rvHappyPlaces.adapter = placesAdapter

        placesAdapter.setOnClickListener(object:
            HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetail::class.java )

                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }

        })
    }

    companion object{
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1

        //Create a constants which will be used to put and get the data using intent from one activity to another
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }


}