package com.allattentionhere.fabulousfiltersample

import android.app.Activity
import android.graphics.Bitmap

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.squareup.picasso.Picasso

import java.util.ArrayList

/**
 * Created by krupenghetiya on 27/06/17.
 */

class MoviesAdapter(list_urls: List<SingleMovie>, internal var picasso: Picasso, internal var _activity: Activity) : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

    private var mList: List<SingleMovie> = ArrayList()

    init {
        this.mList = list_urls
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.single_movie, parent, false)
        return MovieViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (position == 0) {
            layoutParams.setMargins(_activity.resources.getDimension(R.dimen.card_margin).toInt(), _activity.resources.getDimension(R.dimen.card_margin).toInt(), _activity.resources.getDimension(R.dimen.card_margin).toInt(), _activity.resources.getDimension(R.dimen.card_margin).toInt())
        } else {
            layoutParams.setMargins(_activity.resources.getDimension(R.dimen.card_margin).toInt(), 0, _activity.resources.getDimension(R.dimen.card_margin).toInt(), _activity.resources.getDimension(R.dimen.card_margin).toInt())
        }
        holder.card_view.layoutParams = layoutParams

        picasso.load(mList[position].medium_cover_image).placeholder(android.R.color.darker_gray).config(Bitmap.Config.RGB_565).into(holder.iv_cover)
        holder.tv_title.text = mList[position].title
        holder.tv_genre.text = "Genre: " + mList[position].genre
        holder.tv_rating.text = "Rating: " + mList[position].rating
        holder.tv_year.text = "Year: " + mList[position].year
        holder.tv_quality.text = "Quality: " + mList[position].quality
        holder.card_view.setOnClickListener { Log.d("k9", "clicked") }

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class MovieViewHolder(x: View) : RecyclerView.ViewHolder(x) {
        val iv_cover: ImageView
        val tv_title: TextView
        val tv_genre: TextView
        val tv_rating: TextView
        val tv_year: TextView
        val tv_quality: TextView
        val card_view: CardView

        init {
            iv_cover = x.findViewById<View>(R.id.iv_cover) as ImageView
            tv_title = x.findViewById<View>(R.id.tv_title) as TextView
            tv_genre = x.findViewById<View>(R.id.tv_genre) as TextView
            tv_rating = x.findViewById<View>(R.id.tv_rating) as TextView
            tv_year = x.findViewById<View>(R.id.tv_year) as TextView
            tv_quality = x.findViewById<View>(R.id.tv_quality) as TextView
            card_view = x.findViewById<View>(R.id.card_view) as CardView
        }

    }


}
