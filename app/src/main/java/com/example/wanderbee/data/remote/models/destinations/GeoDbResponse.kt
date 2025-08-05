package com.example.wanderbee.data.remote.models.destinations

data class GeoDbResponse(
    val data: List<City>
)

data class City(
    val id: Int,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

data class CountryResponse(
    val data: List<Country>
)

data class Country(
    val id: Int,
    val name: String,
    val code: String,
    val currencyCode: String?,
    val currencyName: String?,
    val currencySymbol: String?,
    val timezone: String?,
    val languages: List<Language>?
)

data class Language(
    val name: String,
    val code: String
)

data class CurrencyResponse(
    val data: List<Currency>
)

data class Currency(
    val code: String,
    val name: String,
    val symbol: String
)
