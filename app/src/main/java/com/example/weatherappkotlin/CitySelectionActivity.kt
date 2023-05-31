package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
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

class CitySelectionActivity : AppCompatActivity() {

    private lateinit var cityList: ListView
    private lateinit var searchView: SearchView
    private var cities: ArrayList<String> = ArrayList()
    private lateinit var cityJsonArray: JSONArray
    private lateinit var adapter: ArrayAdapter<String>
    private var cityIdMap: HashMap<String, String> = HashMap()

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
                    cities.add(cityName)
                    cityIdMap[cityName] = cityId
                }
                adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities)
                cityList.adapter = adapter

                cityList.setOnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@CitySelectionActivity, WeatherActivity::class.java)
                    val selectedCityName = adapter.getItem(position)
                    if (selectedCityName != null) {
                        intent.putExtra("cityID", cityIdMap[selectedCityName])
                        startActivity(intent)
                    }
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val filteredCities = ArrayList<String>()
                for (city in cities) {
                    if (city.toLowerCase().contains(newText.toLowerCase())) {
                        filteredCities.add(city)
                    }
                }
                adapter = ArrayAdapter(this@CitySelectionActivity, android.R.layout.simple_list_item_1, filteredCities)
                cityList.adapter = adapter
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
