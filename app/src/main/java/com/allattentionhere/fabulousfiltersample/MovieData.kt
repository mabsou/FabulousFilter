package com.allattentionhere.fabulousfiltersample

import java.util.ArrayList
import java.util.Collections

/**
 * Created by krupenghetiya on 28/06/17.
 */

class MovieData(mList: List<SingleMovie>) {
    var allMovies: List<SingleMovie> = ArrayList()
        private set

    val uniqueGenreKeys: List<String>
        get() {
            val genres = ArrayList<String>()
            for (movie in allMovies) {
                if (!genres.contains(movie.genre)) {
                    genres.add(movie.genre)
                }
            }
            Collections.sort(genres)
            return genres
        }

    val uniqueYearKeys: List<String>
        get() {
            val years = ArrayList<String>()
            for (movie in allMovies) {
                if (!years.contains(movie.year.toString() + "")) {
                    years.add(movie.year.toString() + "")
                }
            }
            Collections.sort(years)
            return years
        }

    val uniqueQualityKeys: List<String>
        get() {
            val qualities = ArrayList<String>()
            for (movie in allMovies) {
                if (!qualities.contains(movie.quality)) {
                    qualities.add(movie.quality)
                }
            }
            Collections.sort(qualities)
            return qualities
        }

    val uniqueRatingKeys: List<String>
        get() {
            val ratings = ArrayList<String>()
            for (movie in allMovies) {
                val rating = Math.floor(movie.rating.toDouble()).toInt()
                val rate = "> $rating"
                if (!ratings.contains(rate)) {
                    ratings.add(rate)
                }
            }
            Collections.sort(ratings)
            return ratings
        }

    init {
        this.allMovies = mList
    }

    fun setmList(mList: List<SingleMovie>) {
        this.allMovies = mList
    }

    fun getGenreFilteredMovies(genre: List<String>, mList: List<SingleMovie>): List<SingleMovie> {
        val tempList = ArrayList<SingleMovie>()
        for (movie in mList) {
            for (g in genre) {
                if (movie.genre.equals(g, ignoreCase = true)) {
                    tempList.add(movie)
                }
            }

        }
        return tempList
    }

    fun getYearFilteredMovies(yearstr: List<String>, mList: List<SingleMovie>): List<SingleMovie> {
        val tempList = ArrayList<SingleMovie>()
        for (movie in mList) {
            for (y in yearstr) {
                if (movie.year == Integer.parseInt(y)) {
                    tempList.add(movie)
                }
            }
        }
        return tempList
    }

    fun getQualityFilteredMovies(quality: List<String>, mList: List<SingleMovie>): List<SingleMovie> {
        val tempList = ArrayList<SingleMovie>()
        for (movie in mList) {
            for (q in quality) {
                if (movie.quality.equals(q, ignoreCase = true)) {
                    tempList.add(movie)
                }
            }
        }
        return tempList
    }

    fun getRatingFilteredMovies(rating: List<String>, mList: List<SingleMovie>): List<SingleMovie> {
        val tempList = ArrayList<SingleMovie>()
        for (movie in mList) {
            for (r in rating) {
                if (movie.rating >= java.lang.Float.parseFloat(r.replace(">", ""))) {
                    tempList.add(movie)
                }
            }
        }
        return tempList
    }


}
