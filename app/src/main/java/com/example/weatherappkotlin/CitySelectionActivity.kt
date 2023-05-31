package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherappkotlin.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets

data class City(val name: String, val id: String)

class CitySelectionActivity : AppCompatActivity() {

    private lateinit var cityList: ListView
    private lateinit var searchView: SearchView
    private var cities: ArrayList<City> = ArrayList()
    private lateinit var cityJsonArray: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_selection)

        cityList = findViewById(R.id.cityList)
        searchView = findViewById(R.id.searchCity)

        val cityListJson = loadJSONFromAsset()
        if (cityListJson != null) {
            try {
                cityJsonArray = JSONArray(cityListJson)
                for (i in 0 until cityJsonArray.length()) {
                    val cityObject = cityJsonArray.getJSONObject(i)
                    val cityName = cityObject.getString("name")
                    val cityId = cityObject.getString("id")
                    cities.add(City(cityName, cityId))
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities.map { it.name })
                cityList.adapter = adapter

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val filteredCities = ArrayList<City>()
                for (city in cities) {
                    if (city.name.toLowerCase().contains(newText.toLowerCase())) {
                        filteredCities.add(city)
                    }
                }
                val adapter = ArrayAdapter(this@CitySelectionActivity, android.R.layout.simple_list_item_1, filteredCities.map { it.name })
                cityList.adapter = adapter

                cityList.setOnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@CitySelectionActivity, WeatherActivity::class.java)
                    intent.putExtra("cityID", filteredCities[position].id)
                    startActivity(intent)
                }

                return false
            }
        })
    }

    private fun loadJSONFromAsset(): String? {
        var json: String? = null
        try {
            val `is` = assets.open("city.list.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return json
    }
}
