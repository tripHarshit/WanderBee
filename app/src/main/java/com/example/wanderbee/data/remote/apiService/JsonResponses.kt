package com.example.wanderbee.data.remote.apiService

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.wanderbee.data.remote.models.destinations.Destination
import com.example.wanderbee.data.remote.models.destinations.IndianDestination

class JsonResponses {

    fun popularDestinations(context: Context): List<Destination> {
        val jsonString = context.assets.open("destinations.json")
            .bufferedReader()
            .use { it.readText() }

        val listType = object : TypeToken<List<Destination>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }

    fun indianDestinations(context: Context): List<IndianDestination> {
        val jsonString = context.assets.open("indian_destinations.json")
            .bufferedReader()
            .use { it.readText() }

        val listType = object: TypeToken<List<IndianDestination>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }
}