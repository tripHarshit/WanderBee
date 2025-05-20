package com.example.wanderbee.data.remote.models.destinations

data class Destination(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,

    val currency: String,
    val language: String,
    val region: String,
    val timezone: String,
    val isoCode: String,
    val tags: List<String>
)

data class IndianDestination(
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val currency: String,
    val language: String,
    val timezone: String,
    val isoCode: String,
    val tags: List<String>
)


