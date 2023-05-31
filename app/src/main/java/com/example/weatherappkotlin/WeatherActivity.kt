package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.weatherappkotlin.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

open class WeatherActivity : AppCompatActivity() {
    private lateinit var cityName: TextView
    private lateinit var currentTemperature: TextView
    private lateinit var feelsLike: TextView
    private lateinit var highestTemperature: TextView
    private lateinit var lowestTemperature: TextView
    private lateinit var humidity: TextView
    private lateinit var wind: TextView
    private lateinit var seaLevel: TextView
    private lateinit var coordinates: TextView
    private lateinit var weatherDescription: TextView
    private var cityID: String? = null
    private var apiKey = "fe6bc44b49bcd11d908d81cb9d34eb7b"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        cityName = findViewById(R.id.cityName)
        currentTemperature = findViewById(R.id.currentTemperature)
        feelsLike = findViewById(R.id.feelsLikeTemperature)
        highestTemperature = findViewById(R.id.highestTemperature)
        lowestTemperature = findViewById(R.id.lowestTemperature)
        humidity = findViewById(R.id.humidity)
        wind = findViewById(R.id.wind)
        seaLevel = findViewById(R.id.seaLevel)
        coordinates = findViewById(R.id.coordinates)
        weatherDescription = findViewById(R.id.weatherDescription)

        cityID = intent.getStringExtra("cityID")

        cityID?.let {
            val urlString = "https://api.openweathermap.org/data/2.5/weather?APPID=$apiKey&units=metric&id=$it"
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    getWeather(urlString)
                }
                processResult(result)
                //println("processResult(result):  $result")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("cityID", cityID)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        cityID = savedInstanceState.getString("cityID")

        cityID?.let {
            lifecycleScope.launch {
                val result = getWeather(it)
                processResult(result)
            }
        }
    }

    private fun getWeather(urlString: String): String {
        var response = ""
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connect()

            val inStream = Scanner(conn.inputStream)

            while (inStream.hasNextLine()) {
                response += (inStream.nextLine())
            }
        } catch (e: Exception) {
            println("HATA: ${e.javaClass.simpleName}, ${e.message}")
            e.printStackTrace()
        } finally {
            conn?.disconnect()
        }

        return response
    }


    private suspend fun processResult(result: String) {
            withContext(Dispatchers.Main) {
                try {
                    val jsonObject = JSONObject(result)
                    val mainObject = jsonObject.getJSONObject("main")
                    val windObject = jsonObject.getJSONObject("wind")
                    val coordObject = jsonObject.getJSONObject("coord")

                    val city = jsonObject.getString("name")
                    val temp = mainObject.getString("temp")
                    val feelsLikeTemp = mainObject.getString("feels_like")
                    val tempMax = mainObject.getString("temp_max")
                    val tempMin = mainObject.getString("temp_min")
                    val humidityValue = mainObject.getString("humidity")
                    val windSpeed = windObject.getString("speed")

                    val seaLevelValue = if (mainObject.has("sea_level")) mainObject.getString("sea_level") else "N/A"

                    val coordLat = coordObject.getString("lat")
                    val coordLon = coordObject.getString("lon")

                    val weatherArray = jsonObject.getJSONArray("weather")
                    val weatherObject = weatherArray.getJSONObject(0)
                    val weatherDesc = weatherObject.getString("description")

                    cityName.text = city
                    currentTemperature.text = "Current: $temp°C"
                    feelsLike.text = "Feels Like: $feelsLikeTemp°C"
                    highestTemperature.text = "High: $tempMax°C"
                    lowestTemperature.text = "Low: $tempMin°C"
                    humidity.text = "Humidity: $humidityValue%"
                    wind.text = "Wind Speed: $windSpeed m/s"
                    seaLevel.text = "Sea Level: $seaLevelValue m"
                    coordinates.text = "Coordinates: $coordLat, $coordLon"
                    weatherDescription.setText("Weather Description: " + weatherDesc)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("JSON Okuma Hatası", "Error: ", e)
                }
            }
        }
    }
