package com.example.helloapplication.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.helloapplication.R
import com.example.helloapplication.activity.AddHappyPlaces
import com.example.helloapplication.activity.MainActivity
import com.example.helloapplication.model.HappyPlaceModel

open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
     return MyViewHolder(
         LayoutInflater.from(context).inflate(
             R.layout.item_happy_place,
             parent,
             false
         )
     )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder){
            val image: ImageView = holder.itemView.findViewById(R.id.iv_place_image)
            image.setImageURI(Uri.parse(model.image))
            val title = holder.itemView.findViewById<TextView>(R.id.tvTitle)
            title.text = model.title
            val description = holder.itemView.findViewById<TextView>(R.id.tvDescription)
            description.text = model.description
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null){
                onClickListener!!.onClick(position, model)
            }
        }
    }

    /**
     * A function to edit the added happy place detail and pass the existing details through intent.
     */
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddHappyPlaces::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(
            intent,
            requestCode
        ) // Activity is started with requestCode

        notifyItemChanged(position)
    }
    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }


    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }
    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}